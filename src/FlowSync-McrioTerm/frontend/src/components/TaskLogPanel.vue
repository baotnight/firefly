<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>进度更新</h2>
      <div style="display:flex;gap:12px">
        <el-select v-model="filterTaskId" placeholder="筛选任务" clearable style="width:220px" @change="fetchLogs">
          <el-option v-for="t in tasks" :key="t.id" :label="t.title" :value="t.id" />
        </el-select>
        <el-button type="primary" @click="openDialog">新增进度</el-button>
      </div>
    </div>

    <el-table :data="logs" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="taskTitle" label="任务" width="150" show-overflow-tooltip />
      <el-table-column prop="progressPercent" label="进度" width="180">
        <template #default="{ row }">
          <div style="display:flex;align-items:center;gap:8px">
            <el-progress :percentage="row.progressPercent" :stroke-width="10" style="flex:1" />
            <span style="font-size:12px;width:40px">{{ row.progressPercent }}%</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="说明内容" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="记录人" width="100" />
      <el-table-column prop="createTime" label="时间" width="160" />
    </el-table>

    <!-- 新增进度弹窗 -->
    <el-dialog title="新增进度记录" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="选择任务">
          <el-select v-model="form.taskId" style="width:100%" filterable>
            <el-option v-for="t in tasks" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度百分比">
          <el-slider v-model="form.progressPercent" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="说明内容">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请描述本次进度更新内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTaskLogs, saveTaskLog, getTasks } from '../api'
import { ElMessage } from 'element-plus'

defineProps({ currentUser: Object })

const logs = ref([])
const tasks = ref([])
const loading = ref(false)
const filterTaskId = ref(null)
const dialogVisible = ref(false)
const form = ref({ taskId: null, progressPercent: 0, content: '' })

onMounted(async () => {
  loading.value = true
  try {
    const [logRes, taskRes] = await Promise.all([getTaskLogs(), getTasks()])
    if (logRes.success) logs.value = logRes.data || []
    if (taskRes.success) tasks.value = taskRes.data || []
  } finally { loading.value = false }
})

async function fetchLogs() {
  loading.value = true
  try {
    const res = await getTaskLogs(filterTaskId.value || undefined)
    if (res.success) logs.value = res.data || []
  } finally { loading.value = false }
}

function openDialog() {
  form.value = { taskId: null, progressPercent: 0, content: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const res = await saveTaskLog(form.value)
  if (res.success) {
    ElMessage.success('进度记录已保存')
    dialogVisible.value = false
    fetchLogs()
  }
}
</script>
