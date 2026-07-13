<template>
  <!-- ==================== 登录/注册页面 ==================== -->
  <div v-if="!currentUser" class="login-container">
    <div class="login-card">
      <h1 class="login-title">FlowSync</h1>
      <p class="login-subtitle">小组任务协同管理系统</p>

      <!-- 登录表单 -->
      <el-form v-if="!isRegisterMode" :model="loginForm" :rules="loginRules" ref="loginFormRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" style="width:100%" @click="handleLogin" :loading="submitting">
            登 录
          </el-button>
        </el-form-item>
        <div class="login-hint">
          <!--<p>预置账号：leader / member1 / member2，密码均为 123456</p> -->
          <p>
            没有账号？<el-button link type="primary" @click="switchToRegister">立即注册</el-button>
          </p>
        </div>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else :model="registerForm" :rules="registerRules" ref="registerFormRef" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="registerForm.username" placeholder="用户名" size="large" />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="registerForm.realName" placeholder="真实姓名" size="large" />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="registerForm.role" placeholder="选择角色" size="large" style="width:100%">
            <el-option label="组员（直接加入团队）" value="组员" />
            <el-option label="项目负责人（需邀请码）" value="负责人" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="registerForm.role === '负责人'" prop="inviteCode">
          <el-input v-model="registerForm.inviteCode" placeholder="请输入邀请码" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="密码（至少4位）" size="large" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="确认密码" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="success" size="large" style="width:100%" @click="handleRegister" :loading="submitting">
            注 册
          </el-button>
        </el-form-item>
        <div class="login-hint">
          <p style="color:#909399;font-size:12px">注册为项目负责人需要管理员提供的邀请码</p>
          <p>
            已有账号？<el-button link type="primary" @click="switchToLogin">返回登录</el-button>
          </p>
        </div>
      </el-form>
    </div>
  </div>

  <!-- ==================== 主界面 ==================== -->
  <div v-else class="main-layout">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>FlowSync</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        @select="handleMenuSelect"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <!-- 控制台 -->
        <el-menu-item index="dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>控制台</span>
        </el-menu-item>

        <!-- 业务功能 -->
        <el-menu-item index="projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item v-if="isLeader || isAdmin" index="ai-plan">
          <el-icon><MagicStick /></el-icon>
          <span>AI 任务拆解</span>
        </el-menu-item>
        <el-menu-item index="tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="task-logs">
          <el-icon><Timer /></el-icon>
          <span>进度更新</span>
        </el-menu-item>
        <el-menu-item index="summaries">
          <el-icon><Document /></el-icon>
          <span>总结管理</span>
        </el-menu-item>

        <!-- 管理员 -->
        <el-menu-item v-if="isAdmin" index="admin">
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </el-menu-item>

        <!-- 系统信息 -->
        <el-menu-item index="members">
          <el-icon><UserFilled /></el-icon>
          <span>成员列表</span>
        </el-menu-item>
        <el-menu-item index="profile">
          <el-icon><Setting /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-menu>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer">
        <span>{{ currentUser.realName }}</span>
        <el-tag :type="isLeader ? 'danger' : 'info'" size="small">{{ currentUser.role }}</el-tag>
        <el-popconfirm title="确认退出登录？" @confirm="handleLogout">
  <template #reference>
    <el-button text size="small" style="color:#bfcbd9;margin-left:8px">退出</el-button>
  </template>
</el-popconfirm>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="content">
      <component :is="currentPanel" :current-user="currentUser" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { login as apiLogin, register as apiRegister } from '../api'

// 面板组件
import DashboardPanel from '../components/DashboardPanel.vue'
import ProjectPanel from '../components/ProjectPanel.vue'
import AiTaskPlanPanel from '../components/AiTaskPlanPanel.vue'
import TaskPanel from '../components/TaskPanel.vue'
import TaskLogPanel from '../components/TaskLogPanel.vue'
import SummaryPanel from '../components/SummaryPanel.vue'
import MemberListPanel from '../components/MemberListPanel.vue'
import AdminPanel from '../components/AdminPanel.vue'
import ProfilePanel from '../components/ProfilePanel.vue'

const currentUser = ref(null)
const activeMenu = ref('dashboard')
const submitting = ref(false)
const isRegisterMode = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = ref({ username: '', password: '' })
const registerForm = ref({ username: '', realName: '', role: '组员', inviteCode: '', password: '', confirmPassword: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== registerForm.value.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码至少 4 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const isLeader = computed(() => currentUser.value?.role === '负责人')
const isAdmin = computed(() => currentUser.value?.role === '管理员')

// 菜单 → 面板映射
const panelMap = {
  'dashboard': DashboardPanel,
  'projects': ProjectPanel,
  'ai-plan': AiTaskPlanPanel,
  'tasks': TaskPanel,
  'task-logs': TaskLogPanel,
  'summaries': SummaryPanel,
  'members': MemberListPanel,
  'admin': AdminPanel,
  'profile': ProfilePanel
}

const currentPanel = computed(() => panelMap[activeMenu.value] || DashboardPanel)

onMounted(() => {
  const stored = sessionStorage.getItem('currentUser')
  const token = sessionStorage.getItem('token')
  if (stored && token) {
    currentUser.value = JSON.parse(stored)
  }
})

function switchToRegister() {
  isRegisterMode.value = true
}

function switchToLogin() {
  isRegisterMode.value = false
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await apiLogin(loginForm.value)
    if (res.success) {
      const { token, user } = res.data
      sessionStorage.setItem('token', token)
      sessionStorage.setItem('currentUser', JSON.stringify(user))
      currentUser.value = user
      ElMessage.success(`欢迎，${user.realName}`)
    }
  } finally {
    submitting.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await apiRegister({
      username: registerForm.value.username,
      password: registerForm.value.password,
      realName: registerForm.value.realName,
      role: registerForm.value.role,
      inviteCode: registerForm.value.inviteCode
    })
    if (res.success) {
      ElMessage.success('注册成功，请登录')
      switchToLogin()
      loginForm.value.username = registerForm.value.username
      // 清空注册表单
      registerForm.value = { username: '', realName: '', password: '', confirmPassword: '' }
    }
  } finally {
    submitting.value = false
  }
}

function handleMenuSelect(index) {
  activeMenu.value = index
}

function handleLogout() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('currentUser')
  currentUser.value = null
  activeMenu.value = 'dashboard'
}
</script>

<style>
/* 全局重置 */
* { margin: 0; padding: 0; box-sizing: border-box; }

/* ======== 登录页 ======== */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #1a2a6c, #b21f1f, #fdbb2d);
}
.login-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-title {
  text-align: center;
  font-size: 28px;
  color: #303133;
  margin-bottom: 4px;
}
.login-subtitle {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-bottom: 30px;
}
.login-hint {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  line-height: 1.8;
}

/* ======== 主界面 ======== */
.main-layout {
  display: flex;
  height: 100vh;
}

/* 侧边栏 */
.sidebar {
  width: 220px;
  background: #304156;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding: 20px;
  text-align: center;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-header h2 {
  font-size: 20px;
}
.sidebar .el-menu {
  flex: 1;
  border-right: none;
}
.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  align-items: center;
  color: #bfcbd9;
  font-size: 13px;
  gap: 6px;
  flex-wrap: wrap;
}

/* 内容区 */
.content {
  flex: 1;
  padding: 20px;
  background: #f0f2f5;
  overflow-y: auto;
}
</style>
