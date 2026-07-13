<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <div style="display:flex;align-items:center;gap:12px">
        <h2>项目管理</h2>
        <el-button v-if="(isLeader || isAdmin) && selectedIds.length > 0"
                   type="danger" @click="handleBatchDelete" :loading="batchDeleting">
          批量删除（{{ selectedIds.length }}）
        </el-button>
      </div>
      <el-button v-if="isLeader || isAdmin" type="primary" @click="openDialog(null)">新建项目</el-button>
    </div>

    <el-table :data="projects" border stripe v-loading="loading"
              @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="项目名称" />
      <el-table-column prop="description" label="项目说明" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="80" />
      <el-table-column prop="ownerName" label="负责人" width="100" />
      <el-table-column prop="startDate" label="开始" width="110" />
      <el-table-column prop="endDate" label="结束" width="110" />
      <el-table-column v-if="isLeader || isAdmin" label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-popconfirm title="确认删除此项目？" @confirm="handleDelete(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑弹窗 -->
    <el-dialog :title="form.id ? '编辑项目' : '新建项目'" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="项目说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="form.status" style="width:100%">
            <el-option v-for="s in statuses" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority" style="width:100%">
            <el-option v-for="p in priorities" :key="p" :label="p" :value="p" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" style="width:100%" value-format="YYYY-MM-DD" />
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
import { ref, computed, onMounted } from 'vue'
import { getProjects, saveProject, deleteProject, batchDeleteProjects } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({ currentUser: Object })
const isLeader = computed(() => props.currentUser?.role === '负责人')
const isAdmin = computed(() => props.currentUser?.role === '管理员')

const projects = ref([])
const loading = ref(false)
const batchDeleting = ref(false)
const selectedIds = ref([])
const dialogVisible = ref(false)
const form = ref({})
const statuses = ['未开始', '进行中', '已完成']
const priorities = ['低', '中', '高']

onMounted(fetchProjects)

async function fetchProjects() {
  loading.value = true
  try {
    const res = await getProjects()
    if (res.success) projects.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  form.value = row ? { ...row } : { name: '', description: '', status: '未开始', priority: '中', startDate: '', endDate: '' }
  dialogVisible.value = true
}

async function handleSave() {
  const res = await saveProject(form.value)
  if (res.success) {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchProjects()
  }
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个项目？此操作不可恢复。`, '批量删除', { type: 'warning' })
  } catch { return }
  batchDeleting.value = true
  try {
    const res = await batchDeleteProjects(selectedIds.value)
    if (res.success) {
      ElMessage.success(res.message)
      selectedIds.value = []
      fetchProjects()
    }
  } finally { batchDeleting.value = false }
}

async function handleDelete(id) {
  const res = await deleteProject(id)
  if (res.success) {
    ElMessage.success('删除成功')
    fetchProjects()
  }
}

function statusType(status) {
  return status === '已完成' ? 'success' : status === '进行中' ? 'warning' : 'info'
}
</script>
