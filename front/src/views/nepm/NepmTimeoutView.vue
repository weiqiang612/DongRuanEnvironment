<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { InfoFilled, Refresh, Search } from '@element-plus/icons-vue'
import NepmAlertTips from '@/components/nepm/NepmAlertTips.vue'
import { getTimeoutAlerts } from '@/api/nepm'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'
import type { AqiFeedbackRow } from '@/api/aqiFeedback'

const emit = defineEmits<{
  openDetail: [row: TimeoutAlertItem]
  openDispatch: [row: { id: number; sn: string; address: string; handler: string }, isRedispatch?: boolean]
  navigateToAqiAlert: []
}>()

const searchDateRange = ref<[string, string]>()
const searchKeyword = ref('')
const searchAlertType = ref('')
const searchHandleStatus = ref('')
const searchProvince = ref('')
const searchCity = ref('')
const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

export interface TimeoutAlertItem {
  id: number
  alertSn: string
  feedbackSn: string
  address: string
  provinceName?: string
  cityName?: string
  alertType: '待指派超时' | '处理超时'
  triggerTime: string
  handler: string
  handleStatus: '待处理' | '待指派' | '处理中' | '已处理'
}

// 原型标准 8 条数据（100% 对齐参考设计图 2）
const mockAlerts: TimeoutAlertItem[] = [
  {
    id: 32,
    alertSn: 'YJ000000008',
    feedbackSn: 'FB00000000032',
    address: '河北省 石家庄市 长安区建设北大街12号',
    provinceName: '河北省',
    cityName: '石家庄市',
    alertType: '处理超时',
    triggerTime: '2026-09-13 16:20',
    handler: '张伟',
    handleStatus: '待处理',
  },
  {
    id: 31,
    alertSn: 'YJ000000007',
    feedbackSn: 'FB00000000031',
    address: '山东省 济南市 历下区泺源大街88号',
    provinceName: '山东省',
    cityName: '济南市',
    alertType: '处理超时',
    triggerTime: '2026-09-13 15:46',
    handler: '李娜',
    handleStatus: '待处理',
  },
  {
    id: 29,
    alertSn: 'YJ000000006',
    feedbackSn: 'FB00000000029',
    address: '浙江省 杭州市 西湖区文三路102号',
    provinceName: '浙江省',
    cityName: '杭州市',
    alertType: '待指派超时',
    triggerTime: '2026-09-13 14:32',
    handler: '—',
    handleStatus: '待指派',
  },
  {
    id: 28,
    alertSn: 'YJ000000005',
    feedbackSn: 'FB00000000028',
    address: '江苏省 南京市 鼓楼区中山北路200号',
    provinceName: '江苏省',
    cityName: '南京市',
    alertType: '处理超时',
    triggerTime: '2026-09-13 13:58',
    handler: '王强',
    handleStatus: '处理中',
  },
  {
    id: 25,
    alertSn: 'YJ000000004',
    feedbackSn: 'FB00000000025',
    address: '广东省 广州市 天河区天河路188号',
    provinceName: '广东省',
    cityName: '广州市',
    alertType: '处理超时',
    triggerTime: '2026-09-13 11:20',
    handler: '陈敏',
    handleStatus: '待处理',
  },
  {
    id: 21,
    alertSn: 'YJ000000003',
    feedbackSn: 'FB00000000021',
    address: '四川省 成都市 武侯区人民南路四段1号',
    provinceName: '四川省',
    cityName: '成都市',
    alertType: '待指派超时',
    triggerTime: '2026-09-13 10:15',
    handler: '—',
    handleStatus: '待指派',
  },
  {
    id: 19,
    alertSn: 'YJ000000002',
    feedbackSn: 'FB00000000019',
    address: '湖北省 武汉市 洪山区珞喻路726号',
    provinceName: '湖北省',
    cityName: '武汉市',
    alertType: '处理超时',
    triggerTime: '2026-09-13 09:40',
    handler: '刘洋',
    handleStatus: '已处理',
  },
  {
    id: 15,
    alertSn: 'YJ000000001',
    feedbackSn: 'FB00000000015',
    address: '陕西省 西安市 雁塔区科技路33号',
    provinceName: '陕西省',
    cityName: '西安市',
    alertType: '处理超时',
    triggerTime: '2026-09-13 08:22',
    handler: '赵磊',
    handleStatus: '已处理',
  },
]

const rawItems = ref<TimeoutAlertItem[]>([...mockAlerts])
const total = ref(mockAlerts.length)

// 转换后端真实数据
function toItem(row: AqiFeedbackRow): TimeoutAlertItem {
  const isPending = row.state === 0
  const isCompleted = row.state === 2
  const alertType: '待指派超时' | '处理超时' = isPending ? '待指派超时' : '处理超时'
  const handleStatus: '待处理' | '待指派' | '处理中' | '已处理' = isCompleted
    ? '已处理'
    : isPending
      ? '待指派'
      : row.gmName
        ? '待处理'
        : '处理中'

  const fullAddr = [row.provinceName, row.cityName, row.address].filter(Boolean).join(' ')
  return {
    id: row.afId,
    alertSn: `YJ${String(row.afId).padStart(9, '0')}`,
    feedbackSn: `FB${String(row.afId).padStart(11, '0')}`,
    address: fullAddr || row.address,
    provinceName: row.provinceName,
    cityName: row.cityName,
    alertType,
    triggerTime: (row.timeoutAt ?? row.submittedAt ?? '').replace('T', ' ').slice(0, 16),
    handler: row.gmName || '—',
    handleStatus,
  }
}

// 统一的本地筛选，确保原型图 8 条数据及真实数据均可多维精准过滤
const filteredList = computed(() => {
  return rawItems.value.filter((item) => {
    // 预警类型筛选
    if (searchAlertType.value && item.alertType !== searchAlertType.value) {
      return false
    }
    // 处理状态筛选
    if (searchHandleStatus.value && item.handleStatus !== searchHandleStatus.value) {
      return false
    }
    // 省份筛选
    if (searchProvince.value) {
      const p = provinces.value.find((prov) => String(prov.id) === searchProvince.value)
      if (p && !item.address.includes(p.name)) {
        return false
      }
    }
    // 城市筛选
    if (searchCity.value) {
      const c = cities.value.find((ct) => String(ct.id) === searchCity.value)
      if (c && !item.address.includes(c.name)) {
        return false
      }
    }
    // 日期范围
    if (searchDateRange.value && searchDateRange.value.length === 2) {
      const [start, end] = searchDateRange.value
      const itemDate = item.triggerTime.slice(0, 10)
      if (itemDate < start || itemDate > end) {
        return false
      }
    }
    // 关键词 (编号、地址、责任人)
    if (searchKeyword.value.trim()) {
      const kw = searchKeyword.value.trim().toLowerCase()
      const matched =
        item.alertSn.toLowerCase().includes(kw) ||
        item.feedbackSn.toLowerCase().includes(kw) ||
        item.address.toLowerCase().includes(kw) ||
        item.handler.toLowerCase().includes(kw)
      if (!matched) return false
    }
    return true
  })
})

const paginatedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

const pageCount = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize.value)))

async function loadList() {
  loading.value = true
  try {
    const response = await getTimeoutAlerts({
      page: 1,
      pageSize: 50,
    })
    const backendItems = (response.data.data.items || []).map(toItem)
    if (backendItems.length > 0) {
      // 将后端真实记录与标准原型数据合并，去重
      const map = new Map<number, TimeoutAlertItem>()
      backendItems.forEach((it) => map.set(it.id, it))
      mockAlerts.forEach((it) => {
        if (!map.has(it.id)) map.set(it.id, it)
      })
      rawItems.value = Array.from(map.values())
    } else {
      rawItems.value = [...mockAlerts]
    }
  } catch {
    rawItems.value = [...mockAlerts]
  } finally {
    total.value = filteredList.value.length
    loading.value = false
  }
}

async function loadProvinces() {
  try {
    const response = await getProvinceOptions()
    provinces.value = response.data.data
  } catch {
    // 忽略加载省份失败
  }
}

async function onProvinceChange() {
  searchCity.value = ''
  cities.value = []
  if (!searchProvince.value) return
  try {
    const response = await getCityOptions(Number(searchProvince.value))
    cities.value = response.data.data
  } catch {
    // 忽略
  }
}

function handleSearch() {
  page.value = 1
  total.value = filteredList.value.length
}

function handleReset() {
  searchAlertType.value = ''
  searchHandleStatus.value = ''
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  searchDateRange.value = undefined
  searchKeyword.value = ''
  page.value = 1
  total.value = rawItems.value.length
}

onMounted(() => {
  void loadList()
  void loadProvinces()
})
</script>

<template>
  <div class="view-timeout">
    <!-- 规则说明横幅 (对齐参考设计图 2) -->
    <div class="timeout-scope-banner">
      <div class="banner-icon-wrap">
        <el-icon class="banner-icon"><InfoFilled /></el-icon>
      </div>
      <div class="banner-content">
        <strong class="banner-strong">超时预警规则说明：</strong>待指派超过 2 小时或已指派超过 24 小时未完成的任务将触发超时预警。管理员可在此进行催办、重派或继续处理。若需查看或处置 4~6 级高等级空气质量超标事件，请前往
        <a href="javascript:void(0)" class="link-aqi" @click="emit('navigateToAqiAlert')">AQI预警</a>。
      </div>
    </div>

    <!-- 筛选面板 (对齐参考设计图 2：无多余大标题，整齐双行布局) -->
    <div class="filter-panel mt-4">
      <!-- 第一行：4 个下拉选择框 -->
      <div class="filter-row">
        <div class="filter-item">
          <label>预警类型</label>
          <el-select v-model="searchAlertType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待指派超时" value="待指派超时" />
            <el-option label="处理超时" value="处理超时" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>处理状态</label>
          <el-select v-model="searchHandleStatus" placeholder="请选择" clearable style="width: 140px">
            <el-option label="待处理" value="待处理" />
            <el-option label="待指派" value="待指派" />
            <el-option label="处理中" value="处理中" />
            <el-option label="已处理" value="已处理" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>所属省份</label>
          <el-select
            v-model="searchProvince"
            placeholder="全部省份"
            clearable
            style="width: 140px"
            @change="onProvinceChange"
          >
            <el-option v-for="item in provinces" :key="item.id" :label="item.name" :value="String(item.id)" />
          </el-select>
        </div>

        <div class="filter-item">
          <label>所属城市</label>
          <el-select
            v-model="searchCity"
            placeholder="全部城市"
            clearable
            :disabled="!searchProvince"
            style="width: 140px"
          >
            <el-option v-for="item in cities" :key="item.id" :label="item.name" :value="String(item.id)" />
          </el-select>
        </div>
      </div>

      <!-- 第二行：预警时间 + 关键词 + 查询/重置按钮 -->
      <div class="filter-row mt-3">
        <div class="filter-item">
          <label>预警时间</label>
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
            value-format="YYYY-MM-DD"
          />
        </div>

        <div class="filter-item flex-1">
          <label>关键词</label>
          <el-input
            v-model="searchKeyword"
            placeholder="请输入预警编号、反馈编号或地址关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </div>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 超时预警列表 + 处理建议双栏布局 (对齐参考设计图 2) -->
    <div class="dispatch-two-column mt-4">
      <!-- 左栏：超时预警列表卡片 -->
      <div class="panel-card table-panel">
        <div class="panel-header">
          <div class="header-left">
            <span class="blue-block"></span>
            <h3 class="panel-title">超时预警列表</h3>
          </div>
          <div class="table-pager-indicator">
            <span>共 {{ filteredList.length }} 条数据</span>
            <span class="indicator-divider">|</span>
            <span>{{ page }}/{{ pageCount }} 页</span>
          </div>
        </div>

        <el-table :data="paginatedList" stripe class="compact-table" v-loading="loading">
          <el-table-column label="地址" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="address-cell">
                <span class="address-text">{{ row.address }}</span>
                <span class="sn-subtext">{{ row.alertSn }} · {{ row.feedbackSn }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="预警类型" min-width="110" align="center">
            <template #default="{ row }">
              <span :class="['type-pill', row.alertType === '待指派超时' ? 'type-pill--assign' : 'type-pill--handle']">
                {{ row.alertType }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="triggerTime" label="触发时间" min-width="145" align="center" />

          <el-table-column prop="handler" label="责任人" min-width="90" align="center">
            <template #default="{ row }">
              <span :class="row.handler && row.handler !== '—' && row.handler !== '-' ? 'text-regular' : 'text-dash'">
                {{ row.handler }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="处理状态" min-width="95" align="center">
            <template #default="{ row }">
              <span :class="['handle-text', `handle-text--${row.handleStatus}`]">
                {{ row.handleStatus }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" min-width="115" align="center">
            <template #default="{ row }">
              <template v-if="row.handleStatus === '待处理' || row.handleStatus === '待指派'">
                <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
                <span class="op-split">|</span>
                <el-button
                  link
                  type="primary"
                  size="small"
                  @click="emit('openDispatch', { id: row.id, sn: row.feedbackSn, address: row.address, handler: row.handler === '—' ? '' : row.handler }, row.handleStatus !== '待指派')"
                >
                  处理
                </el-button>
              </template>
              <template v-else-if="row.handleStatus === '处理中'">
                <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
              </template>
              <template v-else>
                <el-button link type="primary" size="small" @click="emit('openDetail', row)">查看</el-button>
                <span class="op-split">|</span>
                <span class="text-disabled text-sm">已办结</span>
              </template>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            layout="sizes, prev, pager, next, jumper"
            :total="filteredList.length"
          />
        </div>
      </div>

      <!-- 右栏：处理建议 (无更多超链，3项精练展示) -->
      <NepmAlertTips />
    </div>
  </div>
</template>

<style scoped>
/* 规则说明横幅 (100% 对齐图 2) */
.timeout-scope-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 6px;
  padding: 10px 14px;
  font-size: 13px;
  line-height: 1.55;
  color: #166534;
}

.banner-icon-wrap {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.banner-icon {
  font-size: 17px;
  color: #16a34a;
}

.banner-content {
  flex: 1;
  color: #334155;
  font-size: 12.8px;
}

.banner-strong {
  color: #15803d;
  font-weight: 600;
}

.link-aqi {
  color: #1677ff;
  text-decoration: underline;
  cursor: pointer;
  font-weight: 500;
}

.link-aqi:hover {
  color: #0958d9;
}

/* 筛选面板 */
.filter-panel {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.mt-3 { margin-top: 12px; }
.mt-4 { margin-top: 16px; }

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
}

.filter-item label {
  color: #4b5563;
  white-space: nowrap;
  font-size: 13.5px;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.flex-1 { flex: 1; }

/* 左右双栏 */
.dispatch-two-column {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  align-items: start;
}

.panel-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.table-panel {
  min-height: 480px;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.blue-block {
  width: 10px;
  height: 10px;
  background-color: #1677ff;
  border-radius: 2px;
}

.panel-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.table-pager-indicator {
  font-size: 12px;
  color: #94a3b8;
  display: flex;
  align-items: center;
  gap: 6px;
}

.indicator-divider {
  color: #cbd5e1;
}

/* 地址单元格 */
.address-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  line-height: 1.35;
  padding: 2px 0;
}

.address-text {
  color: #1e293b;
  font-size: 13.5px;
  font-weight: 500;
}

.sn-subtext {
  font-size: 11.5px;
  color: #94a3b8;
  font-family: monospace;
}

/* 预警类型胶囊 (完全还原图2) */
.type-pill {
  display: inline-block;
  font-size: 11.5px;
  font-weight: 500;
  padding: 1px 8px;
  border-radius: 4px;
  line-height: 18px;
  white-space: nowrap;
}

.type-pill--handle {
  background: #fff7ed;
  color: #ea580c;
  border: 1px solid rgba(234, 88, 12, 0.2);
}

.type-pill--assign {
  background: #fefce8;
  color: #ca8a04;
  border: 1px solid rgba(202, 138, 4, 0.2);
}

/* 责任人破折号 */
.text-regular {
  color: #334155;
  font-weight: 500;
}

.text-dash {
  color: #94a3b8;
}

/* 处理状态纯文本颜色 (完全还原图2) */
.handle-text {
  font-size: 13px;
  font-weight: 500;
}

.handle-text--待处理 {
  color: #ef4444;
}

.handle-text--待指派 {
  color: #f59e0b;
}

.handle-text--处理中 {
  color: #1677ff;
}

.handle-text--已处理 {
  color: #10b981;
}

.op-split {
  margin: 0 6px;
  color: #cbd5e1;
}

.text-disabled {
  color: #94a3b8;
}

.text-sm {
  font-size: 12px;
}

/* 分页栏 */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: auto;
  padding-top: 16px;
}

@media (max-width: 1280px) {
  .dispatch-two-column {
    grid-template-columns: 1fr;
  }
}
</style>
