<template>
  <main class="page-shell">
    <header class="system-header">
      <div><p>环境空气质量业务管理系统</p><h1>空气质量反馈维护</h1></div>
      <span>反馈管理</span>
    </header>
    <section class="content-card">
      <div class="section-heading"><div><p class="breadcrumb">业务管理 / 公众反馈</p><h2>反馈列表</h2></div><button class="primary" type="button" @click="openCreateForm">新增反馈</button></div>
      <p v-if="message" class="message" :class="messageType" role="status">{{ message }}</p>
      <form v-if="formVisible" class="feedback-form" @submit.prevent="submitForm">
        <div class="form-heading full"><h3>{{ isEditing ? '修改反馈' : '新增反馈' }}</h3><button type="button" @click="closeForm">关闭</button></div>
        <label>手机号<input v-model.trim="form.telId" required maxlength="20" /></label>
        <label>省份<select v-model.number="form.provinceId" required @change="handleProvinceChange"><option :value="null" disabled>请选择省份</option><option v-for="option in provinceOptions" :key="option.id" :value="option.id">{{ option.name }}</option></select></label>
        <label>城市<select v-model.number="form.cityId" required :disabled="form.provinceId === null"><option :value="null" disabled>请选择城市</option><option v-for="option in cityOptions" :key="option.id" :value="option.id">{{ option.name }}</option></select></label>
        <label>详细地址<input v-model.trim="form.address" required maxlength="255" /></label>
        <label class="full">反馈内容<textarea v-model.trim="form.information" required maxlength="1000" /></label>
        <label>预估等级<select v-model.number="form.estimatedGrade" required><option :value="null" disabled>请选择</option><option v-for="grade in gradeOptions" :key="grade.value" :value="grade.value">{{ grade.label }}</option></select></label>
        <label>反馈日期<input v-model="form.afDate" type="date" required /></label><label>反馈时间<input v-model="form.afTime" type="time" step="1" required /></label>
        <div class="form-actions full"><button class="primary" type="submit" :disabled="submitting">{{ submitting ? '保存中…' : '保存' }}</button><button type="button" @click="closeForm">取消</button></div>
      </form>
      <div class="table-wrapper"><table><thead><tr><th>ID</th><th>省市</th><th>详细地址</th><th>反馈时间</th><th>预估等级</th><th>状态</th><th>反馈内容</th><th>操作</th></tr></thead><tbody>
        <tr v-if="!loading && list.length === 0"><td colspan="8" class="empty">暂无反馈数据</td></tr>
        <tr v-for="item in list" :key="item.afId"><td>{{ item.afId }}</td><td>{{ item.provinceName || '-' }} / {{ item.cityName || '-' }}</td><td>{{ item.address }}</td><td>{{ item.afDate }} {{ item.afTime }}</td><td>{{ getGradeText(item.estimatedGrade) }}</td><td><span class="state" :class="getStateClass(item.state, item.timeoutFlag)">{{ getStateText(item.state, item.timeoutFlag) }}</span></td><td>{{ item.information }}</td><td class="actions"><button type="button" @click="openEditForm(item)">修改</button><button class="danger" type="button" @click="deleteItem(item.afId)">删除</button></td></tr>
      </tbody></table></div>
    </section>
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { onMounted, ref } from 'vue'
import { deleteByAfid, getAqiFeedbackList, saveAqiFeedback, updateAqiFeedback, type AqiFeedbackPayload, type AqiFeedbackRow } from '../api/aqiFeedback'
import { getCityOptions, getProvinceOptions, type RegionOption } from '../api/region'

const list = ref<AqiFeedbackRow[]>([])
const form = ref<AqiFeedbackPayload>(emptyForm())
const formVisible = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const loading = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')
const provinceOptions = ref<RegionOption[]>([])
const cityOptions = ref<RegionOption[]>([])
const gradeOptions = [{ value: 0, label: '未评级' }, { value: 1, label: '优' }, { value: 2, label: '良' }, { value: 3, label: '轻度污染' }, { value: 4, label: '中度污染' }, { value: 5, label: '重度污染' }, { value: 6, label: '严重污染' }]

function emptyForm(): AqiFeedbackPayload { return { telId: '', provinceId: null, cityId: null, address: '', information: '', estimatedGrade: null, afDate: '', afTime: '' } }
function showMessage(value: string, type: 'success' | 'error') { message.value = value; messageType.value = type }
function errorMessage(error: unknown) { return axios.isAxiosError(error) ? error.response?.data?.message || '请求失败，请稍后重试' : '请求失败，请稍后重试' }
async function fetchList() { loading.value = true; try { list.value = (await getAqiFeedbackList()).data.data || [] } catch (error) { showMessage(errorMessage(error), 'error') } finally { loading.value = false } }
async function fetchProvinces() { try { provinceOptions.value = (await getProvinceOptions()).data.data || [] } catch (error) { showMessage(errorMessage(error), 'error') } }
async function fetchCities(provinceId: number | null) { if (provinceId === null) { cityOptions.value = []; return }; try { cityOptions.value = (await getCityOptions(provinceId)).data.data || [] } catch (error) { showMessage(errorMessage(error), 'error') } }
async function handleProvinceChange() { form.value.cityId = null; await fetchCities(form.value.provinceId) }
function openCreateForm() { form.value = emptyForm(); isEditing.value = false; formVisible.value = true; message.value = '' }
async function openEditForm(item: AqiFeedbackRow) { form.value = { ...item }; await fetchCities(item.provinceId); isEditing.value = true; formVisible.value = true; message.value = '' }
function closeForm() { formVisible.value = false; form.value = emptyForm() }
async function submitForm() { submitting.value = true; try { const response = isEditing.value ? await updateAqiFeedback(form.value) : await saveAqiFeedback(form.value); if (!response.data.data) { showMessage(response.data.message || '保存失败', 'error'); return }; showMessage(isEditing.value ? '反馈已修改' : '反馈已保存', 'success'); closeForm(); await fetchList() } catch (error) { showMessage(errorMessage(error), 'error') } finally { submitting.value = false } }
async function deleteItem(afId: number) { if (!window.confirm('确定删除该反馈吗？')) return; try { const response = await deleteByAfid(afId); showMessage(response.data.data ? '反馈已删除' : response.data.message, response.data.data ? 'success' : 'error'); if (response.data.data) await fetchList() } catch (error) { showMessage(errorMessage(error), 'error') } }
function getGradeText(grade: number | null) { return gradeOptions.find((item) => item.value === grade)?.label || '未知等级' }
function getStateText(state?: number, timeoutFlag?: boolean) { if (timeoutFlag) return '已超时'; return ({ 0: '待指派', 1: '已指派', 2: '已完成' } as Record<number, string>)[state ?? -1] || '未知状态' }
function getStateClass(state?: number, timeoutFlag?: boolean) { return timeoutFlag ? 'timeout' : `state-${state ?? -1}` }
onMounted(() => { void Promise.all([fetchList(), fetchProvinces()]) })
</script>

<style scoped>
.page-shell{min-height:100vh;padding:28px;background:#f5f7fa;color:#1f2d3d;font-family:'Microsoft YaHei',sans-serif}.system-header,.content-card{max-width:1280px;margin:0 auto}.system-header{display:flex;justify-content:space-between;align-items:center;padding:20px 24px;color:#fff;background:#1f5a94;border-radius:6px 6px 0 0}.system-header p,.breadcrumb{margin:0;font-size:13px}.system-header h1,h2,h3{margin:6px 0}.system-header span{padding:6px 10px;border:1px solid #a9c6e4;border-radius:4px}.content-card{padding:24px;background:#fff;box-shadow:0 2px 8px rgb(31 90 148 / 10%)}.section-heading,.form-heading,.form-actions,.actions{display:flex;align-items:center}.section-heading,.form-heading{justify-content:space-between}.breadcrumb{color:#606266}.primary{padding:7px 13px;color:#fff;background:#1f5a94;border:1px solid #1f5a94;border-radius:4px;cursor:pointer}.primary:hover{background:#174773}.message{padding:10px 12px;border-radius:4px}.success{color:#276749;background:#edf7f0}.error{color:#b42318;background:#fff1f0}.feedback-form{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;margin:20px 0;padding:20px;background:#f8fafc;border:1px solid #d9e1ea}.feedback-form label{display:grid;gap:6px;font-size:14px}.feedback-form input,.feedback-form textarea,.feedback-form select{box-sizing:border-box;width:100%;padding:8px;border:1px solid #b9c7d5;border-radius:4px;font:inherit}.feedback-form textarea{min-height:84px;resize:vertical}.full{grid-column:1/-1}.form-actions{gap:10px}.form-actions button:not(.primary),.actions button{padding:4px 7px;color:#1f5a94;background:none;border:0;cursor:pointer}.actions .danger{color:#b42318}.table-wrapper{overflow-x:auto}table{width:100%;min-width:920px;border-collapse:collapse;font-size:14px}th,td{padding:11px 10px;text-align:left;border-bottom:1px solid #d9e1ea}th{color:#334e68;background:#edf2f7;white-space:nowrap}.actions{gap:4px;white-space:nowrap}.empty{text-align:center;color:#606266}.state{display:inline-block;padding:3px 7px;border-radius:3px}.state-0{color:#1f5a94;background:#e8f1fa}.state-1{color:#174773;background:#dce8f4}.state-2{color:#276749;background:#e5f5e9}.timeout{color:#b42318;background:#fff1f0}@media(max-width:720px){.page-shell{padding:12px}.system-header{padding:16px}.system-header h1{font-size:20px}.system-header span{display:none}.content-card{padding:16px}.feedback-form{grid-template-columns:1fr;padding:14px}.full{grid-column:auto}}
</style>
