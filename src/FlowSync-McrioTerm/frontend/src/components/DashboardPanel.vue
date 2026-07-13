<template>
  <div>
    <h2 style="margin-bottom:20px">控制台</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in stats" :key="item.label">
        <el-card shadow="hover">
          <div style="text-align:center">
            <div style="font-size:14px;color:#909399">{{ item.label }}</div>
            <div style="font-size:36px;font-weight:bold;color:#409EFF;margin-top:8px">
              {{ item.value }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getOverview } from '../api'

const stats = ref([
  { label: '系统用户', value: 0 },
  { label: '项目总数', value: 0 },
  { label: '任务总数', value: 0 },
  { label: '总结总数', value: 0 }
])

onMounted(async () => {
  try {
    const res = await getOverview()
    if (res.success) {
      stats.value[0].value = res.data.userCount || 0
      stats.value[1].value = res.data.projectCount || 0
      stats.value[2].value = res.data.taskCount || 0
      stats.value[3].value = res.data.summaryCount || 0
    }
  } catch (e) { /* ignore */ }
})
</script>
