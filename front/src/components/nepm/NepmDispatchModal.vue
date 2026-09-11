<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { dispatchFeedback, getCandidates, type GridMember } from '@/api/nepm'

const props = defineProps<{
  visible: boolean
  taskData: {
    id: number
    sn: string
    address: string
    estimatedAqi?: string
    isRedispatch?: boolean
    handler?: string
  } | null
}>()

const emit = defineEmits<{
  'update:visible': [val: boolean]
  dispatched: []
}>()

const selectedMember = ref('')
const remarks = ref('')
const submitting = ref(false)
const candidates = ref<GridMember[]>([])
const loadingCandidates = ref(false)

watch(
  () => props.visible,
  async (val) => {
    if (val && props.taskData) {
      selectedMember.value = ''
      remarks.value = ''
      loadingCandidates.value = true
      try {
        const response = await getCandidates(props.taskData.id)
        candidates.value = response.data.data
      } catch {
        candidates.value = []
        ElMessage.error('候选网格员加载失败')
      } finally {
        loadingCandidates.value = false
      }
    }
  }
)

function handleClose() {
  emit('update:visible', false)
}

async function handleConfirm() {
  if (!selectedMember.value) {
    ElMessage.warning('请选择负责的网格巡查员')
    return
  }

  if (!props.taskData) return

  submitting.value = true
  try {
    await dispatchFeedback(props.taskData.id, selectedMember.value)
    const selected = candidates.value.find((member) => member.gmId === selectedMember.value)
    ElMessage.success(props.taskData.isRedispatch ? '任务重派成功' : '任务指派成功')
    if (selected) {
      ElMessage.success(`已指派给 ${selected.gmName}`)
    }
    emit('dispatched')
    emit('update:visible', false)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '任务指派失败，请刷新后重试')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="taskData?.isRedispatch ? '重新指派任务' : '指派网格巡查员'"
    width="540px"
    destroy-on-close
    append-to-body
    @close="handleClose"
  >
    <div v-if="taskData" class="dialog-body">
      <div class="task-summary">
        <div class="summary-item">
          <span class="label">反馈编号：</span>
          <span class="val font-mono">{{ taskData.sn }}</span>
        </div>
        <div class="summary-item">
          <span class="label">反馈地址：</span>
          <span class="val">{{ taskData.address }}</span>
        </div>
        <div v-if="taskData.estimatedAqi" class="summary-item">
          <span class="label">预估 AQI：</span>
          <span class="val">{{ taskData.estimatedAqi }}</span>
        </div>
        <div v-if="taskData.handler && taskData.handler !== '-'" class="summary-item">
          <span class="label">原负责人：</span>
          <span class="val text-amber">{{ taskData.handler }}</span>
        </div>
      </div>

      <el-form label-position="top" class="dispatch-form">
        <el-form-item label="选择网格巡查员" required>
          <el-select
            v-model="selectedMember"
            :loading="loadingCandidates"
            :disabled="loadingCandidates || candidates.length === 0"
            placeholder="请选择网格巡查员（按同城优先规则）"
            style="width: 100%"
          >
            <el-option
              v-for="gm in candidates"
              :key="gm.gmId"
              :label="`${gm.gmName}（${gm.cityName} · ${gm.sourceLevel === 'SAME_CITY' ? '同城优先' : '同省兜底'}）`"
              :value="gm.gmId"
            >
              <div class="gm-option-row">
                <span class="gm-name">{{ gm.gmName }}</span>
                <span class="gm-district">{{ gm.cityName }}</span>
                <span class="gm-load">{{ gm.sourceLevel === 'SAME_CITY' ? '同城优先' : '同省兜底' }}</span>
              </div>
            </el-option>
          </el-select>
          <span v-if="!loadingCandidates && candidates.length === 0" class="empty-candidates">
            当前地区没有可指派的网格巡查员
          </span>
        </el-form-item>

        <el-form-item label="分派备注 / 现场要求">
          <el-input
            v-model="remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入指派说明，例如：请携带便携式检测设备现场核实施工扬尘情况..."
          />
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleConfirm">
          确认{{ taskData?.isRedispatch ? '重派' : '指派' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.task-summary {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 18px;
  font-size: 13.5px;
}

.summary-item {
  display: flex;
  margin-bottom: 6px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-item .label {
  color: #64748b;
  width: 80px;
  flex-shrink: 0;
}

.summary-item .val {
  color: #1f2937;
  font-weight: 500;
}

.font-mono {
  font-family: monospace;
}

.text-amber {
  color: #d97706;
}

.gm-option-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
  font-size: 13px;
}

.gm-name {
  font-weight: 600;
  color: #1f2937;
}

.gm-district {
  color: #64748b;
}

.gm-load {
  color: #1677ff;
  font-size: 12px;
}

.empty-candidates {
  display: block;
  margin-top: 6px;
  color: #94a3b8;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
