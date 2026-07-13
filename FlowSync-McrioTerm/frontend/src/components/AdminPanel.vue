<template>
  <div>
    <h2 style="margin-bottom:16px">系统管理</h2>

    <!-- 邀请码 -->
    <el-card style="margin-bottom:20px">
      <template #header>
        <span>邀请码管理</span>
      </template>
      <div style="display:flex;align-items:center;gap:12px">
        <el-button type="primary" @click="handleGenerateCode" :loading="genLoading">生成邀请码</el-button>
        <div v-if="inviteCode" style="display:flex;align-items:center;gap:8px">
          <el-tag type="success" size="large" style="font-size:18px;font-family:monospace;padding:8px 16px">
            {{ inviteCode }}
          </el-tag>
          <span style="color:#E6A23C;font-size:12px">2 分钟内有效</span>
        </div>
      </div>
    </el-card>

    <!-- 用户角色管理 -->
    <el-card>
      <template #header>
        <span>用户角色管理</span>
      </template>
      <el-table :data="users" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="当前角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === '管理员' ? 'danger' : row.role === '负责人' ? 'warning' : 'info'">
              {{ row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="row.role !== '管理员'">
              <el-button v-if="row.role === '负责人'" size="small" type="warning"
                         @click="handleChangeRole(row, '组员')">降为组员</el-button>
              <el-button v-if="row.role === '组员'" size="small" type="success"
                         @click="handleChangeRole(row, '负责人')">升为负责人</el-button>
            </template>
            <span v-else style="color:#909399">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 转让弹窗 -->
    <el-dialog title="转让项目所有权" v-model="transferVisible" width="500px">
      <p style="margin-bottom:12px;color:#E6A23C">
        ⚠️ 该用户拥有 {{ transferProjects.length }} 个项目，降级前需指定接手人：
      </p>
      <el-table :data="transferProjects" border size="small" max-height="200" style="margin-bottom:16px">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="项目名称" />
      </el-table>
      <el-form-item label="接手人" style="margin-top:8px">
        <el-select v-model="selectedNewOwner" placeholder="选择接手人" style="width:100%">
          <el-option v-for="u in transferCandidates" :key="u.id"
                     :label="u.realName + ' (' + u.role + ')'" :value="u.id"
                     :disabled="u.id === transferUserId" />
        </el-select>
      </el-form-item>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmTransfer" :disabled="!selectedNewOwner">
          确认转让并降级
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAdminUsers, genInviteCode, changeUserRole, getTransferCandidates } from '../api'
import { ElMessage } from 'element-plus'

defineProps({ currentUser: Object })

const users = ref([])
const inviteCode = ref('')
const loading = ref(false)
const genLoading = ref(false)

// 转让弹窗
const transferVisible = ref(false)
const transferUserId = ref(null)
const transferProjects = ref([])
const transferCandidates = ref([])
const selectedNewOwner = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const [uRes, cRes] = await Promise.all([getAdminUsers(), getTransferCandidates()])
    if (uRes.success) users.value = uRes.data || []
    if (cRes.success) transferCandidates.value = cRes.data || []
  } finally { loading.value = false }
})

async function handleGenerateCode() {
  genLoading.value = true
  try {
    const res = await genInviteCode()
    if (res.success) {
      inviteCode.value = res.data.code
      setTimeout(() => { inviteCode.value = '' }, 120000)
    }
  } finally { genLoading.value = false }
}

async function handleChangeRole(row, newRole) {
  try {
    const res = await changeUserRole({ userId: row.id, role: newRole })
    if (res.success) {
      // 检查是否需要转让项目
      if (res.data && res.data.needTransfer) {
        // 重新拉取候选人，确保已降级的用户不再出现
        const cRes = await getTransferCandidates()
        if (cRes.success) transferCandidates.value = cRes.data || []
        transferUserId.value = row.id
        transferProjects.value = res.data.projects
        selectedNewOwner.value = null
        transferVisible.value = true
        return
      }
      ElMessage.success(res.message)
      // 重新拉取列表，确保角色更新反映到视图
      const uRes = await getAdminUsers()
      if (uRes.success) users.value = uRes.data || []
      if (res.data && res.data.transferred > 0) {
        ElMessage.success(`已转让 ${res.data.transferred} 个项目`)
      }
    }
  } catch (e) { /* handled by interceptor */ }
}

async function handleConfirmTransfer() {
  if (!selectedNewOwner.value) {
    ElMessage.warning('请选择接手人')
    return
  }
  try {
    const res = await changeUserRole({
      userId: transferUserId.value,
      role: '组员',
      newOwnerId: selectedNewOwner.value
    })
    if (res.success) {
      ElMessage.success('降级成功，项目已转让')
      transferVisible.value = false
      const uRes = await getAdminUsers()
      if (uRes.success) users.value = uRes.data || []
    }
  } catch (e) { /* handled by interceptor */ }
}
</script>
