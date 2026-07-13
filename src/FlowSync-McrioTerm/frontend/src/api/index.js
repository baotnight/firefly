import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器：自动附加 JWT Token
api.interceptors.request.use(config => {
  const token = sessionStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}, error => Promise.reject(error))

// 响应拦截器：处理 401 和通用错误
api.interceptors.response.use(response => {
  const res = response.data
  if (res.success === false) {
    ElMessage.error(res.message || '操作失败')
  }
  return res
}, error => {
  if (error.response && error.response.status === 401) {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('currentUser')
    ElMessage.error('登录已过期，请重新登录')
    // 延迟刷新，让用户看到提示
    setTimeout(() => { window.location.reload() }, 1500)
    return Promise.reject(error)
  }
  ElMessage.error('网络错误，请稍后重试')
  return Promise.reject(error)
})

// ==================== API 方法 ====================

// 认证
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)
export const updatePassword = (data) => api.post('/users/update-password', data)
export const updateProfile = (data) => api.post('/users/update-profile', data)

// 项目
export const getProjects = () => api.get('/projects')
export const saveProject = (data) => api.post('/projects', data)
export const deleteProject = (id) => api.delete(`/projects/${id}`)
export const batchDeleteProjects = (ids) => api.post('/projects/batch-delete', { ids })

// 任务
export const getTasks = (projectId) => api.get('/tasks', { params: { projectId } })
export const saveTask = (data) => api.post('/tasks', data)
export const updateTaskStatus = (taskId, status) =>
  api.post(`/tasks/${taskId}/status`, { status })
export const deleteTask = (id) => api.delete(`/tasks/${id}`)
export const batchDeleteTasks = (ids) => api.post('/tasks/batch-delete', { ids })

// 进度记录
export const getTaskLogs = (taskId) => api.get('/task-logs', { params: { taskId } })
export const saveTaskLog = (data) => api.post('/task-logs', data)

// 总结
export const getSummaries = () => api.get('/summaries')
export const saveSummary = (data) => api.post('/summaries', data)

// 概览
export const getOverview = () => api.get('/overview')

// 用户
export const getUsers = () => api.get('/users')

// AI
export const aiTaskSuggestion = (data) => api.post('/ai/task-suggestion', data)
export const aiTaskPlan = (data) => api.post('/ai/task-plan', data)
export const aiImportPlan = (data) => api.post('/ai/task-plan/import', data)

// 管理员
export const getAdminUsers = () => api.get('/admin/users')
export const genInviteCode = () => api.post('/admin/invite-code')
export const changeUserRole = (data) => api.post('/admin/change-role', data)
export const getTransferCandidates = () => api.get('/admin/transfer-candidates')

export default api
