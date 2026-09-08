<template>
  <h1>空气质量反馈列表</h1>

  <button type="button" @click="openCreateForm">新增反馈</button>

  <form v-if="formVisible" class="feedback-form" @submit.prevent="submitForm">
    <h2>{{ isEditing ? '修改反馈' : '新增反馈' }}</h2>

    <label>
      手机号
      <input v-model.trim="form.telId" required maxlength="20" />
    </label>
    <label>
      省份
      <select v-model.number="form.provinceId" required @change="handleProvinceChange">
        <option :value="null" disabled>请选择省份</option>
        <option v-for="province in provinceOptions" :key="province.id" :value="province.id">
          {{ province.name }}
        </option>
      </select>
    </label>
    <label>
      城市
      <select v-model.number="form.cityId" required :disabled="form.provinceId === null">
        <option :value="null" disabled>请选择城市</option>
        <option v-for="city in cityOptions" :key="city.id" :value="city.id">
          {{ city.name }}
        </option>
      </select>
    </label>
    <label>
      详细地址
      <input v-model.trim="form.address" required maxlength="255" />
    </label>
    <label>
      反馈内容
      <textarea v-model.trim="form.information" required maxlength="1000" />
    </label>
    <label>
      预估等级
      <select v-model.number="form.estimatedGrade" required>
        <option :value="null" disabled>请选择</option>
        <option v-for="grade in gradeOptions" :key="grade.value" :value="grade.value">
          {{ grade.label }}
        </option>
      </select>
    </label>
    <label>
      反馈日期
      <input v-model="form.afDate" type="date" required />
    </label>
    <label>
      反馈时间
      <input v-model="form.afTime" type="time" step="1" required />
    </label>

    <div class="form-actions">
      <button type="submit">保存</button>
      <button type="button" @click="closeForm">取消</button>
    </div>
  </form>

  <table border="1" width="100%">
    <tr>
      <th>ID</th>
      <th>provinceName</th>
      <th>cityName</th>
      <th>地址</th>
      <th>afDate</th>
      <th>afTime</th>
      <th>estimatedGrade</th>
      <th>state</th>
      <th>telId</th>
      <th>information</th>
      <th>操作</th>
    </tr>

    <tr v-for="item in list" :key="item.afId">
      <td>{{ item.afId }}</td>
      <td>{{ item.provinceName }}</td>
      <td>{{ item.cityName }}</td>
      <td>{{ item.address }}</td>
      <td>{{ item.afDate }}</td>
      <td>{{ item.afTime }}</td>
      <td>{{ getGradeText(item.estimatedGrade) }}</td>
      <td>{{ getStateText(item.state) }}</td>
      <td>{{ item.telId }}</td>
      <td>{{ item.information }}</td>
      <td>
        <button type="button" @click="openEditForm(item)">修改</button>
        <button @click="deleteItem(item.afId)">删除</button>
      </td>
    </tr>
  </table>
</template>

<script lang="ts">
import {
  deleteByAfid,
  getAqiFeedbackList,
  saveAqiFeedback,
  updateAqiFeedback,
  type AqiFeedbackPayload,
} from '../api/aqiFeedback'
import { getCityOptions, getProvinceOptions, type RegionOption } from '../api/region'

interface AqiFeedbackRow extends AqiFeedbackPayload {
  afId: number
  provinceName?: string
  cityName?: string
}

function createEmptyForm(): AqiFeedbackPayload {
  return {
    telId: '',
    provinceId: null,
    cityId: null,
    address: '',
    information: '',
    estimatedGrade: null,
    afDate: '',
    afTime: '',
    state: 0,
  }
}

export default {
  name: 'AqiFeedBackList',
  data() {
    return {
      list: [] as AqiFeedbackRow[],
      form: createEmptyForm(),
      formVisible: false,
      isEditing: false,
      provinceOptions: [] as RegionOption[],
      cityOptions: [] as RegionOption[],
      gradeOptions: [
        { value: 0, label: '未评级' },
        { value: 1, label: '一级' },
        { value: 2, label: '二级' },
        { value: 3, label: '三级' },
        { value: 4, label: '四级' },
        { value: 5, label: '五级' },
        { value: 6, label: '六级' },
      ],
    }
  },

  created() {
    void this.initializePage()
  },

  methods: {
    async initializePage() {
      await Promise.all([this.fetchList(), this.fetchProvinces()])
    },

    async fetchList() {
      const res = await getAqiFeedbackList()

      if (res.data.code === 200) {
        this.list = res.data.data as AqiFeedbackRow[]
      }
    },

    async fetchProvinces() {
      const res = await getProvinceOptions()
      if (res.data.code === 200) {
        this.provinceOptions = res.data.data as RegionOption[]
      }
    },

    async fetchCities(provinceId: number | null) {
      if (provinceId === null) {
        this.cityOptions = []
        return
      }

      const res = await getCityOptions(provinceId)
      if (res.data.code === 200) {
        this.cityOptions = res.data.data as RegionOption[]
      }
    },

    async handleProvinceChange() {
      this.form.cityId = null
      await this.fetchCities(this.form.provinceId)
    },

    openCreateForm() {
      this.form = createEmptyForm()
      this.isEditing = false
      this.formVisible = true
    },

    async openEditForm(item: AqiFeedbackRow) {
      this.form = {
        afId: item.afId,
        telId: item.telId,
        provinceId: item.provinceId,
        cityId: item.cityId,
        address: item.address,
        information: item.information,
        estimatedGrade: item.estimatedGrade,
        afDate: item.afDate,
        afTime: item.afTime,
        state: item.state,
      }
      await this.fetchCities(item.provinceId)
      this.isEditing = true
      this.formVisible = true
    },

    closeForm() {
      this.formVisible = false
      this.form = createEmptyForm()
    },

    async submitForm() {
      const res = this.isEditing
        ? await updateAqiFeedback(this.form)
        : await saveAqiFeedback(this.form)

      if (res.data.code === 200 && res.data.data) {
        alert(this.isEditing ? '修改成功' : '新增成功')
        this.closeForm()
        await this.fetchList()
      } else {
        alert(res.data.message || (this.isEditing ? '修改失败' : '新增失败'))
      }
    },

    async deleteItem(afId: number) {
      if (!confirm('确定删除吗？')) return

      const res = await deleteByAfid(afId)

      if (res.data.code === 200) {
        alert('删除成功')
        this.fetchList()
      } else {
        alert('删除失败')
      }
    },

    getGradeText(grade: number | null) {
      const map: Record<number, string> = {
        0: '未评级',
        1: '一级',
        2: '二级',
        3: '三级',
        4: '四级',
        5: '五级',
        6: '六级',
      }

      return grade === null ? '未知等级' : (map[grade] ?? '未知等级')
    },

    getStateText(state: number | null | undefined) {
      const map: Record<number, string> = {
        0: '未分配',
        1: '已分配',
        2: '处理中',
        3: '已处理',
      }

      return state === null || state === undefined ? '未知状态' : (map[state] ?? '未知状态')
    },
  },
}
</script>

<style scoped>
.feedback-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  max-width: 760px;
  margin: 16px 0;
  padding: 16px;
  border: 1px solid #ccc;
}

.feedback-form h2,
.feedback-form label:has(textarea),
.form-actions {
  grid-column: 1 / -1;
}

.feedback-form label {
  display: grid;
  gap: 4px;
}

.feedback-form input,
.feedback-form textarea,
.feedback-form select {
  box-sizing: border-box;
  width: 100%;
}

.feedback-form textarea {
  min-height: 80px;
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 8px;
}
</style>
