<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2>总结管理</h2>
      <el-button type="primary" @click="openDialog">新增总结</el-button>
    </div>

    <el-table :data="summaries" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="projectName" label="所属项目" width="120" />
      <el-table-column prop="taskTitle" label="关联任务" width="120" />
      <el-table-column prop="summaryType" label="总结类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.summaryType === '最终总结' ? 'success' : 'warning'">{{ row.summaryType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="creatorName" label="创建人" width="100" />
      <el-table-column prop="createTime" label="时间" width="160" />
    </el-table>

    <!-- 新增总结弹窗 -->
    <el-dialog title="新增总结" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属项目">
          <el-select v-model="form.projectId" style="width:100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联任务">
          <el-select v-model="form.taskId" style="width:100%" clearable placeholder="可选">
            <el-option v-for="t in tasks" :key="t.id" :label="t.title" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="总结类型">
          <el-select v-model="form.summaryType" style="width:100%">
            <el-option v-for="s in summaryTypes" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="总结内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请撰写总结内容" />
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
import { getSummaries, saveSummary, getProjects, getTasks } from '../api'
import { ElMessage } from 'element-plus'

defineProps({ currentUser: Object })

const summaries = ref([])
const projects = ref([])
const tasks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ projectId: null, taskId: null, summaryType: '阶段总结', content: '' })
const summaryTypes = ['阶段总结', '最终总结']

onMounted(async () => {
  loading.value = true
  try {
    const [sRes, pRes, tRes] = await Promise.all([getSummaries(), getProjects(), getTasks()])
    if (sRes.success) summaries.value = sRes.data || []
    if (pRes.success) projects.value = pRes.data || []
    if (tRes.success) tasks.value = tRes.data || []
  } finally { loading.value = false }
})

async function fetchSummaries() {
  const res = await getSummaries()
  if (res.success) summaries.value = res.data || []
}

function openDialog() {
  form.value = { projectId: null, taskId: null, summaryType: '阶段总结', content: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const res = await saveSummary(form.value)
  if (res.success) {
    ElMessage.success('总结已保存')
    dialogVisible.value = false
    fetchSummaries()
  }
}
</script>
