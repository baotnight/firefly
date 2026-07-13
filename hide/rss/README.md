# RSS 订阅功能 — 归档与恢复指南

> 所有 RSS 相关代码已从项目中剥离，整合归档于此。按以下步骤可完整恢复 RSS 功能。

## 文件清单

```
hide/rss/
├── rss.astro          # RSS 展示页面（复制自 src/pages/rss.astro）
├── rss.xml.ts         # RSS XML Feed 生成端点（复制自 src/pages/rss.xml.ts）
└── README.md          # 本文件
```

---

## 一、恢复步骤

### 第一步：还原页面文件

```bash
cp hide/rss/rss.astro src/pages/rss.astro
cp hide/rss/rss.xml.ts src/pages/rss.xml.ts
```

RSS 页面路由将自动恢复：
- `/rss` → 展示页面
- `/rss.xml` → XML Feed 端点

### 第二步：恢复 Layout.astro 中的 RSS `<link>` 标签

文件：`src/layouts/Layout.astro`

在 `<slot name="head" />` 之后、`<FontSetup />` 之前，插入以下代码：

```astro
    <link
      rel="alternate"
      type="application/rss+xml"
      title={profileConfig.name}
      href={`${Astro.site}rss.xml`}
    />
```

还原后片段示例：

```astro
    <slot name="head" />

    <link
      rel="alternate"
      type="application/rss+xml"
      title={profileConfig.name}
      href={`${Astro.site}rss.xml`}
    />

    <!-- Font Setup (Astro Font API) -->
    <FontSetup />
```

### 第三步：恢复 Footer.astro 中的 RSS 链接

文件：`src/components/layout/Footer.astro`

在 `{profileConfig.name}. All Rights Reserved.` 行下方，`<span aria-hidden="true">/</span>` 和 Sitemap 链接之前，插入：

```astro
      <span aria-hidden="true">/</span>
      <a
        class="transition link text-(--primary) font-medium"
        target="_blank"
        href={url("rss.xml")}>RSS</a
      >
```

还原后片段示例：

```astro
      {profileConfig.name}. All Rights Reserved.
      <span aria-hidden="true">/</span>
      <a
        class="transition link text-(--primary) font-medium"
        target="_blank"
        href={url("rss.xml")}>RSS</a
      >
      <span aria-hidden="true">/</span>
      <a
        class="transition link text-(--primary) font-medium"
        target="_blank"
        href={url("sitemap-index.xml")}>Sitemap</a
      >
```

### 第四步：恢复 profileConfig.ts 中的 RSS 图标链接

文件：`src/config/profileConfig.ts`

在 `links` 数组中添加以下条目（放在 RSS 图标对应的位置）：

```ts
			{
				name: "RSS",
				icon: "fa7-solid:rss",
				url: "/rss/",
				showName: false,
			},
```

---

## 二、依赖确认

确保 `package.json` 中保留了以下依赖（项目中已保留，无需额外安装）：

```json
"@astrojs/rss": "^4.0.19"
```

如果在恢复前意外移除了该依赖，运行：

```bash
pnpm add @astrojs/rss
```

---

## 三、i18n 翻译键说明

以下 i18n 键已在所有 6 个语言文件中保留（`src/i18n/languages/*.ts` + `src/i18n/i18nKey.ts`），恢复页面后无需额外处理翻译：

| Key | 用途 |
|---|---|
| `rss` | 页面标题「RSS 订阅」 |
| `rssDescription` | 页面描述 |
| `rssSubtitle` | 订阅副标题 |
| `rssLink` | RSS 链接标签 |
| `rssCopyToReader` | 复制到阅读器提示 |
| `rssCopyLink` | 复制链接按钮 |
| `rssLatestPosts` | 最新文章标题 |
| `rssWhatIsRSS` | 什么是 RSS 标题 |
| `rssWhatIsRSSDescription` | RSS 说明介绍 |
| `rssBenefit1~4` | RSS 四个优点 |
| `rssHowToUse` | 使用推荐 |
| `rssCopied` | 复制成功提示 |
| `rssCopyFailed` | 复制失败提示 |
| `passwordProtectedRss` | 加密文章的 RSS 替代文本 |

---

## 四、功能说明

| 文件 | 功能 |
|---|---|
| `rss.astro` | `/rss` 页面 — RSS 订阅入口页，显示订阅链接、复制按钮、最新文章预览和 RSS 科普说明 |
| `rss.xml.ts` | `/rss.xml` 端点 — 生成标准 RSS 2.0 XML Feed，自动渲染文章内容为 HTML，过滤加密文章 |

### RSS XML 特性

- 自动渲染 Markdown/MDX 文章内容为 HTML
- 加密文章仅显示替代文本，不泄露内容
- 清理无效 XML 字符，保证 Feed 合法性
- 自定义 RSS 模板元数据（主题名、版本号、构建时间）
