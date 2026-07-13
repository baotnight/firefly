<template>
  <div>
    <h2 style="margin-bottom:16px">AI 任务拆解</h2>

    <!-- 第一步：输入项目信息 -->
    <el-card v-if="!planResult" style="margin-bottom:20px">
      <template #header>输入项目信息</template>
      <el-form :model="form" label-width="80px">
        <el-form-item label="项目名称">
          <el-select v-model="form.projectId" placeholder="选择项目" style="width:100%" filterable
                     @change="onProjectChange">
            <el-option v-for="p in projects" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目目标">
          <el-input v-model="form.goal" type="textarea" :rows="3"
                    placeholder="描述项目要达成的目标，例如：开发一个学生选课系统，支持学生选课、退课、查看课表" />
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="form.description" type="textarea" :rows="2"
                    placeholder="补充约束条件、技术偏好等（可选）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGenerate" :loading="generating" :disabled="!form.projectId || !form.goal">
            🤖 AI 拆解任务
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 第二步：展示拆解结果 -->
    <el-card v-if="planResult" style="margin-bottom:20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>AI 拆解结果</span>
          <el-button @click="resetPlan">重新生成</el-button>
        </div>
      </template>

      <el-alert :title="planResult.summary" type="info" :closable="false" style="margin-bottom:16px" />

      <el-table :data="planResult.items" border stripe>
        <el-table-column type="index" width="50" />
        <el-table-column prop="title" label="任务标题" />
        <el-table-column prop="description" label="任务说明" show-overflow-tooltip />
        <el-table-column prop="priority" label="优先级" width="80">
          <template #default="{ row }">
            <el-tag :type="row.priority === '高' ? 'danger' : row.priority === '中' ? 'warning' : 'info'">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestedDays" label="预计天数" width="90" />
        <el-table-column label="负责人" width="120">
          <template #default="{ row }">
            <el-select v-model="row.assigneeId" placeholder="选择" size="small">
              <el-option v-for="u in users" :key="u.id" :label="u.realName" :value="u.id" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-button @click="resetPlan">取消</el-button>
        <el-button type="success" @click="handleImport" :loading="importing">
          确认导入（{{ planResult.items.length }} 个任务）
        </el-button>
      </div>
    </el-card>

    <!-- 提示信息 -->
    <el-card v-if="!planResult">
      <template #header>使用说明</template>
      <ul style="line-height:2;color:#606266">
        <li>选择目标项目，填写项目目标，点击「AI 拆解任务」</li>
        <li>AI 会根据项目目标自动拆分任务并推荐负责人</li>
        <li>确认前可以调整每个任务的负责人</li>
        <li>确认后一键导入到任务管理中</li>
        <li v-if="!apiConfigured" style="color:#E6A23C">
          ⚠️ 未检测到 AI API Key，将使用降级方案（固定模板）
        </li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProjects, getUsers, aiTaskPlan, aiImportPlan } from '../api'
import { ElMessage } from 'element-plus'

defineProps({ currentUser: Object })

const projects = ref([])
const users = ref([])
const apiConfigured = ref(true)
const generating = ref(false)
const importing = ref(false)
const planResult = ref(null)

const form = ref({ projectId: null, goal: '', description: '' })

onMounted(async () => {
  const [pRes, uRes] = await Promise.all([getProjects(), getUsers()])
  if (pRes.success) projects.value = pRes.data || []
  if (uRes.success) users.value = uRes.data || []
})

function onProjectChange(val) {
  const p = projects.value.find(x => x.id === val)
  if (p && !form.value.goal) {
    form.value.goal = p.description || p.name
  }
}

async function handleGenerate() {
  generating.value = true
  try {
    const p = projects.value.find(x => x.id === form.value.projectId)
    const res = await aiTaskPlan({
      projectId: form.value.projectId,
      projectName: p?.name || '',
      goal: form.value.goal,
      description: form.value.description
    })
    if (res.success) {
      planResult.value = res.data
      // 如果 summary 中包含"降级"，提示用户
      if (res.data.summary && res.data.summary.includes('降级')) {
        apiConfigured.value = false
        ElMessage.warning('AI 服务不可用，使用默认模板拆解')
      } else {
        ElMessage.success('AI 拆解完成，请确认后导入')
      }
    }
  } finally {
    generating.value = false
  }
}

async function handleImport() {
  importing.value = true
  try {
    const res = await aiImportPlan({
      projectId: form.value.projectId,
      items: planResult.value.items
    })
    if (res.success) {
      ElMessage.success(res.message)
      resetPlan()
    }
  } finally {
    importing.value = false
  }
}

function resetPlan() {
  planResult.value = null
  form.value = { projectId: null, goal: '', description: '' }
}
</script>
