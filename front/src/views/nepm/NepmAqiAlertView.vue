<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { Check, Refresh, Search, View, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAqiAlerts,
  handleAqiAlert,
  type AqiAlertItem,
} from '@/api/nepm'
import { getCityOptions, getProvinceOptions, type RegionOption } from '@/api/region'

const emit = defineEmits<{
  openDetail: [row: AqiAlertItem]
}>()

const searchStatus = ref<'' | 'PENDING' | 'HANDLED'>('')
const searchProvince = ref('')
const searchCity = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const handlingId = ref<number | null>(null)

const provinces = ref<RegionOption[]>([])
const cities = ref<RegionOption[]>([])
const list = ref<AqiAlertItem[]>([])

// 预警详情弹窗
const detailVisible = ref(false)
const activeAlert = ref<AqiAlertItem | null>(null)

const aqiGradeMap: Record<number, { name: string; tagClass: string; color: string; desc: string }> = {
  1: { name: '一级（优）', tagClass: 'aqi-grade-1', color: '#1b6a3e', desc: '优' },
  2: { name: '二级（良）', tagClass: 'aqi-grade-2', color: '#1b6a3e', desc: '良' },
  3: { name: '三级（轻度污染）', tagClass: 'aqi-grade-3', color: '#7d5514', desc: '轻度污染' },
  4: { name: '四级（中度污染）', tagClass: 'aqi-grade-4', color: '#914216', desc: '中度污染' },
  5: { name: '五级（重度污染）', tagClass: 'aqi-grade-5', color: '#962828', desc: '重度污染' },
  6: { name: '六级（严重污染）', tagClass: 'aqi-grade-6', color: '#59258a', desc: '严重污染' },
}

function getAqiMeta(grade?: number) {
  if (!grade || !aqiGradeMap[grade]) {
    return { name: '未知等级', tagClass: '', color: '#94a3b8', desc: '-' }
  }
  return aqiGradeMap[grade]
}

function formatSn(prefix: string, id: number, padLen = 8): string {
  return `${prefix}${String(id).padStart(padLen, '0')}`
}

function formatTime(val?: string | null): string {
  if (!val) return '-'
  return val.replace('T', ' ').slice(0, 19)
}

async function loadProvinces() {
  try {
    const res = await getProvinceOptions()
    provinces.value = res.data.data
  } catch {
    ElMessage.error('加载省份列表失败')
  }
}

async function onProvinceChange() {
  searchCity.value = ''
  if (!searchProvince.value) {
    cities.value = []
    return
  }
  try {
    const res = await getCityOptions(Number(searchProvince.value))
    cities.value = res.data.data
  } catch {
    ElMessage.error('加载城市选项失败')
  }
}

async function loadAlerts() {
  loading.value = true
  try {
    const res = await getAqiAlerts({
      status: searchStatus.value || undefined,
      provinceId: searchProvince.value ? Number(searchProvince.value) : undefined,
      cityId: searchCity.value ? Number(searchCity.value) : undefined,
      page: page.value,
      pageSize: pageSize.value,
    })
    list.value = res.data.data.items ?? []
    total.value = res.data.data.total ?? 0
  } catch {
    ElMessage.error('查询AQI预警列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  void loadAlerts()
}

function handleReset() {
  searchStatus.value = ''
  searchProvince.value = ''
  searchCity.value = ''
  cities.value = []
  page.value = 1
  void loadAlerts()
}

function openDetail(row: AqiAlertItem) {
  activeAlert.value = row
  detailVisible.value = true
  emit('openDetail', row)
}

async function onHandleAlert(row: AqiAlertItem) {
  const alertMeta = getAqiMeta(row.alertLevel)
  const locationText = `${row.provinceName || ''} ${row.cityName || ''} ${row.address || ''}`.trim()

  try {
    await ElMessageBox.confirm(
      `您确定要处置该高等级 AQI 预警吗？\n\n【预警等级】：${alertMeta.name}\n【预警区域】：${locationText}\n【关联反馈】：${formatSn('FB', row.feedbackId, 12)}\n\n确认后系统将该预警状态原子更新为“已处置”，并记录处置留痕时间。`,
      '高等级 AQI 预警处置确认',
      {
        confirmButtonText: '确认处置',
        cancelButtonText: '取消',
        type: 'warning',
        dangerouslyUseHTMLString: false,
        customClass: 'aqi-confirm-box',
      },
    )
  } catch {
    // 用户点击取消或关闭
    return
  }

  handlingId.value = row.id
  try {
    const res = await handleAqiAlert(row.id)
    if (res.data.code === 200 || res.data.data) {
      ElMessage.success('预警处置成功，状态已更新')
      // 原位更新
      row.alertStatus = 'HANDLED'
      row.handledAt = res.data.data?.handledAt ?? new Date().toISOString()
      // 重新拉取保持分页数据同步
      void loadAlerts()
    }
  } catch (error: unknown) {
    if (axios.isAxiosError(error) && error.response?.status === 409) {
      ElMessage.error('预警已被处置或状态不符')
      void loadAlerts()
    } else if (axios.isAxiosError(error) && error.response?.data?.message) {
      ElMessage.error(String(error.response.data.message))
    } else {
      ElMessage.error('处置失败，请稍后重试')
    }
  } finally {
    handlingId.value = null
  }
}

onMounted(() => {
  void loadProvinces()
  void loadAlerts()
})
</script>

<template>
  <div class="view-aqi-alerts">
    <!-- 业务提示横幅 -->
    <div class="alert-info-banner">
      <el-icon class="banner-icon"><Warning /></el-icon>
      <div class="banner-text">
        当监测数据计算得出的系统最终 AQI 达到
        <span class="highlight-level">4级（中度污染）</span>、
        <span class="highlight-level">5级（重度污染）</span> 或
        <span class="highlight-level">6级（严重污染）</span>
        时，系统自动生成预警记录，请及时处置。
      </div>
    </div>

    <!-- 复合筛选卡片 -->
    <div class="filter-panel mt-3">
      <div class="filter-row">
        <div class="filter-item">
          <label>处置状态</label>
          <el-radio-group v-model="searchStatus" @change="handleSearch">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="PENDING">待处置</el-radio-button>
            <el-radio-button value="HANDLED">已处置</el-radio-button>
          </el-radio-group>
        </div>

        <div class="filter-item">
          <label>所属区域</label>
          <div class="region-selects">
            <el-select
              v-model="searchProvince"
              placeholder="全部省份"
              style="width: 130px"
              clearable
              @change="onProvinceChange"
            >
              <el-option
                v-for="item in provinces"
                :key="item.id"
                :label="item.name"
                :value="String(item.id)"
              />
            </el-select>
            <el-select
              v-model="searchCity"
              placeholder="全部城市"
              style="width: 130px"
              clearable
              :disabled="!searchProvince"
            >
              <el-option
                v-for="item in cities"
                :key="item.id"
                :label="item.name"
                :value="String(item.id)"
              />
            </el-select>
          </div>
        </div>

        <div class="filter-actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 预警数据表格 -->
    <div class="panel-card mt-4">
      <el-table
        v-loading="loading"
        :data="list"
        stripe
        class="main-data-table"
        empty-text="暂无高等级 AQI 预警数据"
      >
        <el-table-column label="预警区域与地址" min-width="210" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="address-cell">
              <div class="address-main">
                <span v-if="row.provinceName || row.cityName" class="region-prefix">{{ row.provinceName }} {{ row.cityName }}</span>
                <span class="address-text">{{ row.address }}</span>
              </div>
              <span class="sn-subtext">{{ formatSn('YJ', row.id, 8) }} · {{ formatSn('FB', row.feedbackId, 12) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="预警等级" min-width="125" align="center">
          <template #default="{ row }">
            <span :class="['aqi-pill', getAqiMeta(row.alertLevel).tagClass]">
              {{ getAqiMeta(row.alertLevel).name }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="gmName" label="检测网格员" min-width="105" align="center">
          <template #default="{ row }">
            <span class="text-regular">{{ row.gmName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预警时间" min-width="150" align="center">
          <template #default="{ row }">
            <span>{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="处置状态" min-width="95" align="center">
          <template #default="{ row }">
            <span v-if="row.alertStatus === 'PENDING'" class="status-pill status-pill--pending">
              待处置
            </span>
            <span v-else class="status-pill status-pill--handled">
              已处置
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="125" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.alertStatus === 'PENDING'"
              type="danger"
              size="small"
              plain
              :loading="handlingId === row.id"
              :icon="Check"
              @click="onHandleAlert(row)"
            >
              处置
            </el-button>
            <span v-else class="text-disabled text-sm">已归档</span>
            <span class="op-split">|</span>
            <el-button link type="primary" size="small" :icon="View" @click="openDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="pagination-bar">
        <span class="page-total">共 {{ total }} 条预警记录</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          layout="sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @current-change="loadAlerts"
          @size-change="loadAlerts"
        />
      </div>
    </div>

    <!-- 预警详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="AQI 高等级预警详情"
      width="640px"
      destroy-on-close
    >
      <div v-if="activeAlert" class="alert-dialog-body">
        <div class="alert-highlight-box">
          <div class="hl-label">预警严重程度</div>
          <div class="hl-level">
            <span :class="['aqi-pill', 'aqi-pill-large', getAqiMeta(activeAlert.alertLevel).tagClass]">
              {{ getAqiMeta(activeAlert.alertLevel).name }}
            </span>
          </div>
          <div class="hl-desc">
            实测最终 AQI 达到该等级，系统自动触发高等级环境污染预警。
          </div>
        </div>

        <div class="detail-info-list">
          <div class="info-row">
            <span class="key">预警编号：</span>
            <span class="val font-mono">{{ formatSn('YJ', activeAlert.id, 8) }}</span>
          </div>
          <div class="info-row">
            <span class="key">关联反馈：</span>
            <span class="val font-mono">{{ formatSn('FB', activeAlert.feedbackId, 12) }}</span>
          </div>
          <div class="info-row">
            <span class="key">关联检测结果：</span>
            <span class="val font-mono">{{ formatSn('JC', activeAlert.resultId, 8) }}</span>
          </div>
          <div class="info-row">
            <span class="key">预警发生区域：</span>
            <span class="val">{{ activeAlert.provinceName }} {{ activeAlert.cityName }}</span>
          </div>
          <div class="info-row">
            <span class="key">详细地址：</span>
            <span class="val">{{ activeAlert.address }}</span>
          </div>
          <div class="info-row">
            <span class="key">采样网格员：</span>
            <span class="val text-bold">{{ activeAlert.gmName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="key">预警生成时间：</span>
            <span class="val">{{ formatTime(activeAlert.createdAt) }}</span>
          </div>
          <div class="info-row">
            <span class="key">处置状态：</span>
            <span class="val">
              <span v-if="activeAlert.alertStatus === 'PENDING'" class="status-pill status-pill--pending">
                待处置
              </span>
              <span v-else class="status-pill status-pill--handled">
                已处置
              </span>
            </span>
          </div>
          <div class="info-row">
            <span class="key">处置完成时间：</span>
            <span class="val">{{ formatTime(activeAlert.handledAt) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="activeAlert?.alertStatus === 'PENDING'"
            type="danger"
            @click="onHandleAlert(activeAlert!)"
          >
            立即处置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.alert-info-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  padding: 12px 18px;
  color: #874d00;
  font-size: 13.5px;
  line-height: 1.6;
}
.banner-icon {
  font-size: 20px;
  color: #faad14;
  margin-top: 2px;
  flex-shrink: 0;
}
.highlight-level {
  font-weight: 600;
  color: #d4380d;
}
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
  flex-wrap: wrap;
  gap: 16px;
}
.mt-3 {
  margin-top: 12px;
}
.mt-4 {
  margin-top: 16px;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
}
.filter-item label {
  color: #4b5563;
  font-size: 13.5px;
  white-space: nowrap;
}
.region-selects {
  display: flex;
  gap: 6px;
}
.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}
.panel-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}
.font-mono {
  font-family: monospace;
}
.text-bold {
  font-weight: 600;
  color: #1f2937;
}
.text-regular {
  color: #374151;
  font-weight: 500;
}
.text-disabled {
  color: #94a3b8;
}
.address-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.35;
}
.address-main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}
.sn-subtext {
  font-size: 11px;
  color: #94a3b8;
  font-family: monospace;
}
.text-sm {
  font-size: 12px;
}
.region-prefix {
  display: inline-block;
  color: #1F5A94;
  background: #f0f7ff;
  border: 1px solid #d0e2ff;
  border-radius: 3px;
  padding: 1px 6px;
  font-size: 11.5px;
  white-space: nowrap;
}
.address-text {
  color: #334155;
  font-weight: 500;
}
.status-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.status-pill--pending {
  background: #fff7ed;
  color: #c2410c;
  border: 1px solid #fed7aa;
}
.status-pill--handled {
  background: #f0fdf4;
  color: #4C956C;
  border: 1px solid #bbf7d0;
}
.aqi-pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}
.aqi-pill-large {
  font-size: 14px;
  padding: 4px 12px;
  font-weight: 600;
}
.op-split {
  margin: 0 6px;
  color: #cbd5e1;
}
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}
.page-total {
  font-size: 13px;
  color: #64748b;
}

/* 详情弹窗 */
.alert-highlight-box {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  margin-bottom: 20px;
}
.hl-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 8px;
}
.hl-desc {
  margin-top: 8px;
  font-size: 12.5px;
  color: #64748b;
}
.detail-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #fafbfc;
  border: 1px solid #eaeff5;
  border-radius: 8px;
  padding: 16px;
}
.info-row {
  display: flex;
  align-items: flex-start;
  font-size: 13.5px;
}
.info-row .key {
  width: 120px;
  color: #64748b;
  flex-shrink: 0;
}
.info-row .val {
  color: #1e293b;
  flex: 1;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
