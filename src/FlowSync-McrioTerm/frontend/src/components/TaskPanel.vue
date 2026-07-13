<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:12px">
        <h2>任务管理</h2>
        <el-button v-if="(isLeader || isAdmin) && selectedIds.length > 0"
                   type="danger" @click="handleBatchDelete" :loading="batchDeleting">
          批量删除（{{ selectedIds.length }}）
        </el-button>
      </div>
      <div style="display:flex;gap:12px">
        <el-select v-model="filterProjectId" placeholder="筛选项目" clearable style="width:200px" @change="fetchTasks">
          <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
        </el-select>
        <el-button v-if="isLeader || isAdmin" type="primary" @click="openDialog(null)">新建任务</el-button>
      </div>
    </div>

    <el-table :data="tasks" border stripe v-loading="loading" row-key="id"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="任务标题" />
      <el-table-column prop="projectName" label="所属项目" width="120" />
      <el-table-column prop="description" label="任务说明" show-overflow-tooltip />
      <el-table-column prop="assigneeName" label="负责人" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="dueDate" label="截止日期" width="110" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <!-- 负责人可全字段编辑 -->
          <el-button v-if="isLeader || isAdmin" size="small" @click="openDialog(row)">编辑</el-button>
          <!-- 组员只能更新自己任务的状态 -->
          <el-button v-if="canUpdateStatus(row)" size="small" type="warning" @click="openStatusDialog(row)">更新状态</el-button>
          <el-popconfirm v-if="isLeader || isAdmin" title="确认删除？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑弹窗（负责人专用，全字段） -->
    <el-dialog :title="form.id ? '编辑任务' : '新建任务'" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="任务标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-select v-model="form.projectId" style="width:100%">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="任务说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.assigneeId" style="width:100%">
            <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width:100%">
            <el-option v-for="p in priorities" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 组员状态更新弹窗 -->
    <el-dialog title="更新任务状态" v-model="statusDialogVisible" width="400px">
      <el-form :model="statusForm" label-width="80px">
        <el-form-item label="任务标题">
          <el-input :model-value="statusForm.title" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag :type="statusType(statusForm.oldStatus)">{{ statusForm.oldStatus }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态">
          <el-select v-model="statusForm.status" style="width:100%">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusUpdate">确认更新</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTasks, saveTask, updateTaskStatus, deleteTask, batchDeleteTasks, getProjects, getUsers } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({ currentUser: Object })
const isLeader = computed(() => props.currentUser?.role === '负责人')
const isAdmin = computed(() => props.currentUser?.role === '管理员')

const tasks = ref([])
const projects = ref([])
const users = ref([])
const loading = ref(false)
const batchDeleting = ref(false)
const selectedIds = ref([])
const filterProjectId = ref(null)
const dialogVisible = ref(false)
const statusDialogVisible = ref(false)
const form = ref({})
const statusForm = ref({})
const statuses = ['未开始', '进行中', '已完成']
const priorities = ['低', '中', '高']

onMounted(async () => {
  loading.value = true
  try {
    const [taskRes, projRes, userRes] = await Promise.all([getTasks(), getProjects(), getUsers()])
    if (taskRes.success) tasks.value = taskRes.data || []
    if (projRes.success) projects.value = projRes.data || []
    if (userRes.success) users.value = userRes.data || []
  } finally {
    loading.value = false
  }
})

async function fetchTasks() {
  loading.value = true
  try {
    const res = await getTasks(filterProjectId.value || undefined)
    if (res.success) tasks.value = res.data || []
  } finally { loading.value = false }
}

function canUpdateStatus(row) {
  // 组员只能更新分配给自己且不是已完成的任务
  return row.assigneeId === props.currentUser?.id && row.status !== '已完成'
}

function openDialog(row) {
  form.value = row ? { ...row } : {
    title: '', description: '', projectId: null, assigneeId: null,
    status: '未开始', priority: '中', dueDate: ''
  }
  dialogVisible.value = true
}

async function handleSave() {
  const res = await saveTask(form.value)
  if (res.success) {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchTasks()
  }
}

function openStatusDialog(row) {
  statusForm.value = { id: row.id, title: row.title, oldStatus: row.status, status: row.status }
  statusDialogVisible.value = true
}

async function handleStatusUpdate() {
  const res = await updateTaskStatus(statusForm.value.id, statusForm.value.status)
  if (res.success) {
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    fetchTasks()
  }
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个任务？`, '批量删除', { type: 'warning' })
  } catch { return }
  batchDeleting.value = true
  try {
    const res = await batchDeleteTasks(selectedIds.value)
    if (res.success) {
      ElMessage.success(res.message)
      selectedIds.value = []
      fetchTasks()
    }
  } finally { batchDeleting.value = false }
}

async function handleDelete(id) {
  await deleteTask(id)
  ElMessage.success('删除成功')
  fetchTasks()
}

function statusType(status) {
  return status === '已完成' ? 'success' : status === '进行中' ? 'warning' : 'info'
}
</script>
