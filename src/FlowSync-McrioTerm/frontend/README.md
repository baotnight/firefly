# 前端占位文件 — 目录说明

## 目录结构

```
frontend/
├── package.json           # npm 依赖配置
├── vue.config.js          # Vue CLI 配置（devServer 代理）
├── public/
└── src/
    ├── main.js            # 入口文件
    ├── App.vue            # 根组件
    ├── router/
    │   └── index.js       # 路由配置
    ├── api/
    │   └── index.js       # Axios 封装 + API 方法
    ├── assets/            # 静态资源
    ├── views/
    │   └── HomeView.vue   # 主容器（登录 + 侧边导航 + 面板切换）
    └── components/        # 8 个功能面板组件
        ├── DashboardPanel.vue
        ├── ProjectPanel.vue
        ├── AiTaskPlanPanel.vue
        ├── TaskPanel.vue
        ├── TaskLogPanel.vue
        ├── SummaryPanel.vue
        ├── MemberListPanel.vue
        └── ProfilePanel.vue
```

## 核心依赖

- `vue` 3.x
- `vue-router` 4.x
- `element-plus`
- `axios`

## 关键配置

`vue.config.js` devServer 代理：
```js
module.exports = {
  devServer: {
    port: 8081,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
}
```
