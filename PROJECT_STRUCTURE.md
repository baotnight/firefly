# Firefly 项目完整文件树

> 生成日期：2026-07-13 | 版本：6.13.8 | 技术栈：Astro 7 + Svelte 5 + Tailwind CSS v4

---

## 📁 根目录（配置文件层）

```
Firefly/
├── astro.config.mjs            # ⚙ Astro 核心配置（集成 Svelte/Tailwind/MDX/Cloudflare/图标等）
├── biome.json                  # 🧹 Biome 代码规范 & 格式化配置（tab 缩进、双引号）
├── package.json                # 📦 项目依赖 & npm/pnpm 脚本命令
├── pnpm-lock.yaml              # 🔒 pnpm 依赖锁文件（版本冻结）
├── tsconfig.json               # 📘 TypeScript 编译配置 & 路径别名（@components, @utils 等）
├── svelte.config.js            # 🟠 Svelte 编译器配置
├── postcss.config.mjs          # 🎨 PostCSS 配置（Tailwind 插件、嵌套语法等）
├── pagefind.yml                # 🔍 Pagefind 静态搜索索引配置
├── wrangler.jsonc              # ☁ Cloudflare Workers 部署配置 [← 你刚刚打开的文件]
├── vercel.json                 # ▲ Vercel 部署配置
├── .npmrc                      # npm 镜像/行为配置
├── .gitignore                  # Git 忽略规则
├── .gitattributes              # Git 行尾 & 文件属性
├── LICENSE                     # 开源许可证
├── CLAUDE.md                   # 🤖 Claude Code AI 助手指引
├── AGENTS.md                   # 🤖 通用 AI 代理配置指引
├── README.md                   # 📖 项目中文说明文档
├── README.en.md                # 📖 项目英文说明文档
├── CONTRIBUTING.md             # 🤝 贡献指南
├── _frontmatter.json           # 📋 Frontmatter 自动补全定义（VSCode/IDE 用）
├── test_upload.txt             # 🧪 上传测试文件
├── PROJECT_STRUCTURE.md        # 📋 本文件 — 项目结构说明
│
├── 📂 public/                  # 🌐 静态资源（直接复制到 dist，不经构建处理）
│   ├── favicon/                #   网站图标（亮/暗模式各 4 种尺寸 + .ico）
│   ├── assets/
│   │   ├── css/
│   │   │   ├── highlight-github-dark.min.css  # 代码高亮主题
│   │   │   └── twikoo-custom.css              # Twikoo 评论自定义样式
│   │   ├── fonts/
│   │   │   └── GreatVibes-Regular-2.otf       # 花体英文字体
│   │   ├── images/
│   │   │   ├── ad/ad1.webp                    # 广告图片
│   │   │   ├── effects/sakura.png             # 樱花飘落特效素材
│   │   │   └── sponsor/                       # 打赏二维码（支付宝/微信）
│   │   ├── js/
│   │   │   ├── highlight.min.js               # 代码高亮 JS 库
│   │   │   └── marked.min.js                   # Markdown 渲染 JS 库
│   │   └── music/cover/                       # 音乐播放器封面图
│   ├── anime-list.json        # 🎬 追番/动画数据（JSON 数据集）
│   ├── gallery/               #   相册图片资源（按相册分目录，urls.txt 列举图片）
│   ├── pio/                   #   看板娘模型资源
│   │   ├── models/
│   │   │   ├── live2d/snow_miku/              # Live2D 模型：雪初音
│   │   │   └── spine/firefly/                 # Spine 动画模型：Firefly 角色
│   │   └── static/
│   │       ├── spine-player.min.js            # Spine 动画播放器
│   │       └── spine-player.min.css           # Spine 播放器样式
│
├── 📂 scripts/                # 🔧 构建 & 辅助脚本
│   ├── generate-icons.js      #   从 Iconify 图标集生成 src/constants/icons.ts
│   ├── generate-lqips.ts      #   生成文章封面的低质量图片占位符（LQIP）
│   ├── subset-fonts.ts        #   对字体进行子集化，减小文件体积
│   ├── new-post.js            #   快速创建新文章脚手架（pnpm new-post <filename>）
│   └── quarantine-bad-posts.mjs #  隔离有问题的文章到临时目录
│
└── 📂 src/                    # 🧠 核心源码（详见下方）
```

---

## 📁 src/ — 核心源码

### 🎛 src/config/ — 配置驱动层（最常修改）

```
src/config/
├── index.ts                   # 📦 配置桶文件，统一导出所有配置供组件引用
├── README.md                  # 📖 配置指南
├── siteConfig.ts              # ⭐ 站点核心配置：标题、URL、主题色、分页、SEO、加密
├── profileConfig.ts           # 👤 个人资料：昵称、简介、头像、社交链接
├── sidebarConfig.ts           # 📐 侧边栏布局：左/右/双栏、组件排序、宽度
├── navBarConfig.ts            # 🧭 导航栏：菜单项、搜索开关、下拉菜单
├── backgroundWallpaper.ts     # 🖼 背景壁纸：桌面/移动端壁纸、模糊度、轮播
├── commentConfig.ts           # 💬 评论系统：Waline/Giscus/Twikoo/Artalk/Disqus 配置
├── musicConfig.ts             # 🎵 音乐播放器：网易云/Aplayer 配置
├── fontConfig.ts              # 🔤 字体配置：Google Fonts/自定义字体选择
├── footerConfig.ts            # 📄 页脚：ICP备案号、版权声明
├── announcementConfig.ts      # 📢 公告栏：内容、展示位置
├── analyticsConfig.ts         # 📊 数据统计：Google Analytics/Umami/Clarity/La51
├── friendsConfig.ts           # 🔗 友链页面：好友列表、分组、启用状态
├── galleryConfig.ts           # 🖼 相册配置：相册列表、加密、封面
├── effectsConfig.ts           # 🌸 特效：樱花飘落、粒子效果
├── pioConfig.ts               # 🎭 看板娘：Live2D/Spine 模型配置
├── sponsorConfig.ts           # 💰 打赏/赞助：支付宝/微信二维码
├── expressiveCodeConfig.ts    # 🎨 代码块高亮样式/插件配置
├── mermaidConfig.ts           # 📊 Mermaid 图表默认配置
├── plantumlConfig.ts          # 🏗 PlantUML 图表配置
├── coverImageConfig.ts        # 🖼 文章封面图默认设置
├── licenseConfig.ts           # 📜 内容许可协议（CC 等）
└── FooterConfig.html          # 📄 页脚 HTML 片段
```

### 🧩 src/types/ — TypeScript 类型定义

```
src/types/
├── config.ts                  # 🏷 汇总类型导出（所有 config 的 type 定义）
├── siteConfig.ts              #   SiteConfig 类型
├── profileConfig.ts           #   ProfileConfig 类型
├── sidebarConfig.ts           #   SidebarLayoutConfig / WidgetComponentConfig 类型
├── navBarConfig.ts            #   NavBarConfig 类型
├── backgroundWallpaper.ts     #   BackgroundWallpaperConfig 类型
├── commentConfig.ts           #   CommentConfig 类型
├── musicConfig.ts             #   MusicPlayerConfig 类型
├── fontConfig.ts              #   FontSelectionConfig / FontDefinition 类型
├── footerConfig.ts            #   FooterConfig 类型
├── announcementConfig.ts      #   AnnouncementConfig 类型
├── analyticsConfig.ts         #   AnalyticsConfig 类型
├── friendsConfig.ts           #   FriendsConfig 类型
├── galleryConfig.ts           #   GalleryConfig / GalleryAlbum 类型
├── effectsConfig.ts           #   SakuraConfig 类型
├── pioConfig.ts               #   Live2D/Spine 配置类型
├── sponsorConfig.ts           #   SponsorConfig 类型
├── expressiveCodeConfig.ts    #   ExpressiveCodeConfig 类型
├── mermaidConfig.ts           #   MermaidConfig 类型
├── plantumlConfig.ts          #   PlantUMLConfig 类型
├── coverImageConfig.ts        #   CoverImageConfig 类型
├── licenseConfig.ts           #   LicenseConfig 类型
├── anime.ts                   #   追番/动画数据类型
└── bangumi.ts                 #   番剧数据类型
```

### 📄 src/layouts/ — Astro 布局模板

```
src/layouts/
├── Layout.astro               # 🧱 基础 HTML 壳：head、meta、主题脚本、分析、Swup 过渡
└── MainGridLayout.astro       # 🏛 全页面栅格布局：侧边栏 + 导航栏 + 壁纸 + 页脚
```

### 📑 src/pages/ — Astro 路由页面

```
src/pages/
├── [...page].astro            # 🏠 首页（分页列表）：/、/2、/3 ...
├── posts/[...slug].astro      # 📝 文章详情页：/posts/xxxx
├── about.astro                # ℹ 关于页面：/about
├── archive.astro              # 🗄 文章归档页：/archive
├── tags/index.astro           # 🏷 标签聚合页：/tags
├── categories/index.astro     # 📂 分类页：/categories
├── friends.astro              # 🔗 友链页：/friends
├── guestbook.astro            # 📖 留言板：/guestbook
├── search.astro               # 🔍 高级搜索页：/search
├── sponsor.astro              # 💰 赞助页：/sponsor
├── gallery/index.astro        # 🖼 相册列表：/gallery
├── gallery/[album].astro      #   相册详情：/gallery/<album-name>
├── anime.astro                # 🎬 追番列表页：/anime
├── bangumi.astro              # 📺 番剧展示页：/bangumi
├── 404.astro                  # ❌ 自定义 404 页面
├── rss.astro                  # 📡 RSS 订阅页面（HTML 版）
├── rss.xml.ts                 # 📡 RSS XML 生成端点
├── robots.txt.ts              # 🤖 robots.txt 生成
├── og/[...slug].ts            # 🖼 Open Graph / 社交分享图片动态生成（Satori）
└── api/allPostMeta.json.ts    # 🔌 API 端点：返回所有文章元数据 JSON
```

### 🧱 src/components/ — UI 组件

```
src/components/
├── README.md                  # 📖 组件说明文档
│
├── 📂 common/                 # 🔷 通用基础组件
│   ├── ButtonLink.astro       #   链接按钮（静态）
│   ├── ButtonTag.astro        #   标签按钮（静态）
│   ├── ClientPagination.svelte #  客户端分页器（交互式）
│   ├── CoverImage.astro       #   文章封面图渲染
│   ├── DropdownItem.astro     #   下拉菜单项（静态版）
│   ├── DropdownItem.svelte    #   下拉菜单项（交互版）
│   ├── DropdownPanel.astro    #   下拉面板容器（静态版）
│   ├── DropdownPanel.svelte   #   下拉面板容器（交互版）
│   ├── FloatingButton.astro   #   浮动按钮基类
│   ├── Icon.svelte            #   图标组件（Svelte 交互版）
│   ├── ImageWrapper.astro     #   图片容器（支持 Lightbox）
│   ├── Markdown.astro         #   Markdown 渲染壳（插件管线载体）
│   ├── Pagination.astro       #   分页导航（上一页/下一页）
│   ├── PioMessageBox.astro    #   看板娘对话框
│   └── WidgetLayout.astro     #   侧边栏小组件布局壳
│
├── 📂 layout/                 # 🏗 布局核心组件
│   ├── CategoryBar.astro      #   分类横向导航条
│   ├── ConfigCarrier.astro    #   配置传递桥（服务端→客户端，通过 data 属性）
│   ├── DropdownMenu.astro     #   导航栏下拉菜单
│   ├── Footer.astro           #   页脚渲染
│   ├── Navbar.astro           #   顶部导航栏主体
│   ├── NavMenuPanel.astro     #   移动端导航菜单面板
│   ├── PostCard.astro         #   文章卡片（首页列表）
│   ├── PostMeta.astro         #   文章元信息行（日期/标签/分类）
│   ├── PostPage.astro         #   文章详情页布局
│   ├── PostStats.astro        #   文章统计栏（阅读量/评论数）
│   └── SideBar.astro          #   侧边栏容器
│
├── 📂 controls/               # 🎮 交互控件
│   ├── ArchivePanel.astro     #   归档面板
│   ├── BackToComment.astro    #   跳转到评论区按钮
│   ├── BackToHome.astro       #   返回首页浮动按钮
│   ├── BackToTop.astro        #   回到顶部浮动按钮
│   ├── DisplaySettings.svelte #   显示设置面板（亮暗/字体大小/宽度切换）
│   ├── DisplaySettingsIntegrated.svelte # 集成版显示设置
│   ├── FloatingControls.astro #   浮动控件容器（组合 TOC + 返回按钮等）
│   ├── FloatingTOC.astro      #   浮动目录（文章内右侧）
│   ├── LayoutSwitchButton.svelte # 列表/网格视图切换按钮
│   ├── LightDarkSwitch.svelte #   亮色/暗色模式切换
│   ├── ScrollDownIndicator.astro # 向下滚动指示箭头
│   ├── Search.svelte          #   搜索弹窗（客户端全文搜索）
│   └── WallpaperSwitch.svelte #   壁纸开关按钮
│
├── 📂 features/               # ✨ 功能特性组件
│   ├── BackgroundPlayer.astro #   背景音乐播放器（全局）
│   ├── EncryptedContent.astro #   加密内容渲染器
│   ├── EncryptedPost.astro    #   加密文章入口（密码输入）
│   ├── FancyboxManager.astro  #   图片灯箱管理器（Fancybox）
│   ├── FontSetup.astro        #   字体加载与设置
│   ├── KatexManager.astro     #   KaTeX 数学公式渲染管理器
│   ├── Live2DWidget.astro     #   Live2D 看板娘组件
│   ├── MusicManager.astro     #   音乐播放器总控
│   ├── MusicPlayer.astro      #   音乐播放器 UI
│   ├── SakuraEffect.astro     #   樱花飘落特效
│   ├── SpineModel.astro       #   Spine 动画模型组件
│   └── TypewriterText.astro   #   打字机文字效果
│
├── 📂 widget/                 # 🧩 侧边栏小组件
│   ├── Advertisement.astro    #   广告展示组件
│   ├── Announcement.astro     #   公告组件
│   ├── Calendar.astro         #   日历小组件
│   ├── Categories.astro       #   分类列表小组件
│   ├── Music.astro            #   音乐小组件
│   ├── Profile.astro          #   个人信息小组件
│   ├── SidebarTOC.astro       #   侧边栏目录（文章内）
│   ├── SiteInfo.astro         #   站点信息小组件（图标说明）
│   ├── SiteStats.astro        #   站点统计（文章数/分类数/标签数）
│   ├── SpineModel.astro       #   Spine 模型小组件
│   └── Tags.astro             #   标签云小组件
│
├── 📂 comment/                # 💬 评论系统
│   ├── index.astro            #   评论系统路由（根据配置选择后端）
│   ├── Artalk.astro           #   Artalk 评论集成
│   ├── Disqus.astro           #   Disqus 评论集成
│   ├── Giscus.astro           #   Giscus（GitHub Discussions）评论集成
│   ├── Twikoo.astro           #   Twikoo 评论集成
│   └── Waline.astro           #   Waline 评论集成
│
├── 📂 analytics/              # 📊 统计分析
│   ├── GoogleAnalytics.astro  #   Google Analytics (GA4)
│   ├── La51Analytics.astro    #   51.La 统计
│   ├── MicrosoftClarity.astro #   Microsoft Clarity 热力图
│   └── UmamiAnalytics.astro   #   Umami 自托管统计
│
├── 📂 pages/                  # 📄 页面级复杂组件
│   ├── AdvancedSearch.svelte  #   高级搜索页（全功能）
│   ├── 📂 anime/              #   追番页面组件
│   │   ├── AnimeCard.svelte       # 动漫卡片
│   │   ├── AnimeDetailModal.svelte # 动漫详情弹窗
│   │   └── AnimeGrid.svelte       # 动漫卡片网格
│   ├── 📂 bangumi/            #   番剧页面组件
│   │   ├── BangumiGrid.svelte     # 番剧卡片网格
│   │   ├── BangumiSection.svelte  # 番剧分区（在看/想看/已看）
│   │   ├── Card.svelte            # 番剧卡片
│   │   ├── FilterControls.svelte  # 筛选控件
│   │   └── TabNav.svelte          # Tab 切换导航
│   └── 📂 gallery/            #   相册页面组件
│       ├── AlbumCard.astro        # 相册封面卡片
│       └── PhotoCard.astro        # 照片缩略图卡片
│
└── 📂 misc/                   # 🧩 杂项组件
    ├── License.astro          #   文章许可协议展示
    ├── RecommendedPost.astro  #   相关/推荐文章
    └── SharePoster.svelte     #   分享海报生成器
```

### 🔌 src/plugins/ — 15 个 Remark/Rehype 插件

```
src/plugins/                       # Markdown → HTML 处理管线
├── remark-reading-time.mjs        # 📖 阅读时间估算
├── remark-excerpt.js              # ✂ 文章摘要提取
├── remark-image-grid.js           # 🖼 图片网格布局
├── remark-mermaid.js              # 📊 Mermaid 代码块标记
├── remark-plantuml.js             # 🏗 PlantUML 代码块标记
├── remark-directive-rehype.js     # 🔀 自定义指令转换（::note::/::warning:: 等）
├── rehype-mermaid.mjs             # 📊 Mermaid 图渲染（客户端）
├── rehype-plantuml.mjs            # 🏗 PlantUML 图渲染（客户端）
├── rehype-figure.mjs              # 🖼 图片自动包裹 figure + figcaption
├── rehype-external-links.mjs      # 🔗 外部链接 target="_blank" 处理
├── rehype-email-protection.mjs    # 📧 邮箱地址混淆保护
├── rehype-image-referrerpolicy.mjs # 🛡 图片 referrerpolicy 属性
├── rehype-component-github-card.mjs # 🐙 GitHub 仓库卡片嵌入
├── rehype-diagram-panzoom.mjs     # 🔍 图表缩放/拖拽（Mermaid & PlantUML）
│
├── diagram-panzoom-script.js      #   图表缩放客户端 JS
├── plantuml-encoder.js            #   PlantUML 文本编码器
├── plantuml-render-script.js      #   PlantUML 渲染客户端 JS
├── plantuml-theme-switch.js       #   PlantUML 主题切换 JS
│
└── utils/                         #   插件工具函数
    ├── diagramConstants.js        #   图表库常量 & CDN 地址
    └── extractText.js             #   纯文本提取工具
```

### 🌐 src/i18n/ — 国际化

```
src/i18n/
├── i18nKey.ts                # 🔑 所有翻译键定义（枚举）
├── translation.ts            # 🔄 翻译查找函数（根据语言获取文本）
└── languages/
    ├── zh_CN.ts              #   简体中文
    ├── zh_TW.ts              #   繁體中文
    ├── en.ts                 #   English
    ├── ja.ts                 #   日本語
    ├── ko.ts                 #   한국어
    └── ru.ts                 #   Русский
```

### 🛠 src/utils/ — 工具函数库

```
src/utils/
├── build-platform.ts         # 🏗 构建平台检测（Vercel / Cloudflare Workers）
├── content-utils.ts          # 📄 内容集合工具：获取文章、排序、过滤草稿
├── crypto-utils.ts           # 🔐 文章加密/解密工具（AES）
├── date-utils.ts             # 📅 日期格式化工具（dayjs 封装）
├── fontHelper.ts             # 🔤 字体加载 & 回退辅助
├── gallery-utils.ts          # 🖼 相册资源加载与解析
├── icon-loader.ts            # 🎯 图标动态加载
├── image-utils.ts            # 🖼 图片处理工具
├── language-utils.ts         # 🌐 语言检测（浏览器语言匹配）
├── layout-utils.ts           # 📐 布局模式（列表/网格/瀑布流）工具
├── lqip-utils.ts             # 🏞 低质量图片占位符加载
├── navigation-utils.ts       # 🧭 页面导航辅助
├── responsive-utils.ts       # 📱 响应式断点检测
├── sakura-manager.ts         # 🌸 樱花特效管理器
├── setting-utils.ts          # ⚙ 用户设置持久化（localStorage）
├── toc-shared.ts             # 📑 目录生成共享逻辑
├── toc-utils.ts              # 📑 文章目录提取与构建
└── url-utils.ts              # 🔗 URL 解析与处理
```

### 🎨 src/styles/ — 样式文件

```
src/styles/
├── main.css                  # 🎯 全局样式入口（Tailwind 指令 + 自定义层）
├── markdown.css              # 📝 文章内容排版样式
├── markdown-extend.styl      # 📝 Markdown 扩展样式（Stylus）
├── variables.styl            # 🎨 全局 CSS 变量（主题色、间距等）
├── anime-bangumi.css         # 🎬 追番/番剧页样式
├── banner-title.css          # 🎪 横幅标题效果
├── categories.css            # 📂 分类页样式
├── custom-scrollbar.css      # 📜 自定义滚动条
├── expressive-code.css       # 💻 代码高亮颜色主题
├── fancybox-custom.css       # 🖼 灯箱自定义样式
├── gallery.css               # 🖼 相册页样式
├── layout-styles.css         # 📐 页面布局样式
├── navbar.css                # 🧭 导航栏样式
├── photoswipe.css            # 🖼 Photoswipe 灯箱样式
├── scrollbar.css             # 📜 滚动条基础样式
├── tags.css                  # 🏷 标签页样式
├── toc.css                   # 📑 目录组件样式
├── transition.css            # 🔄 页面过渡动画（Swup）
├── waves.css                 # 🌊 波浪动画效果
└── widget-responsive.css     # 📱 小组件响应式适配
```

### 📦 src/content/ — Astro 内容集合

```
src/content/
├── posts/                    # 📝 博客文章（.md / .mdx）
│   ├── guide/index.md        #   "从零搭建 Firefly" 系列
│   ├── firefly.md            #   Firefly 功能介绍
│   ├── firefly-layout-system.md # 布局系统说明
│   ├── markdown-tutorial.md  #   Markdown 基础语法教程
│   ├── markdown-extended.md  #   Markdown 扩展语法教程
│   ├── markdown-mermaid.md   #   Mermaid 图表示例
│   ├── markdown-plantuml.md  #   PlantUML 图表示例
│   ├── katex-math-example.md #   KaTeX 数学公式示例
│   ├── code-examples.md      #   代码块样式示例
│   ├── mdx-example.mdx       #   MDX 组件嵌入示例
│   ├── encrypted-demo.md     #   加密文章示例
│   ├── draft.md              #   草稿示例
│   ├── video.md              #   视频嵌入示例
│   └── images/               #   文章内嵌图片
│
├── spec/                     # 📋 特殊页面
│   ├── about.md              #   关于页面内容
│   ├── friends.mdx           #   友链页面内容
│   └── guestbook.md          #   留言板页面内容
│
└── src/content.config.ts     # ⚙ 内容集合定义（posts + spec 的 schema）
```

### 🧬 src/constants/ — 自动生成的常量数据

```
src/constants/
├── constants.ts              # 📊 站点常量（分页、路径等导出）
├── icons.ts                  # 🎯 从 Iconify 自动生成的图标映射（6 个图标集）
├── icon.ts                   # 🔹 单个图标常量
└── lqips.json                # 🖼 文章封面 LQIP 数据（构建脚本生成）
```

### 📋 src/ 其他核心文件

```
src/
├── content.config.ts         # ⚙ Astro 内容集合定义 & Schema
├── env.d.ts                  # 🌍 环境变量类型声明（Astro 注入类型）
└── global.d.ts               # 🌍 全局类型声明 & 模块扩充
```

---

## 📂 src/assets/ — 静态资源

```
src/assets/images/
├── avatar.avif               # 🧑 头像
├── cover.avif                # 🖼 默认社交分享封面图
├── firefly.png               # 🎯 Firefly 品牌 Logo
├── DesktopWallpaper/         # 🖥 桌面端壁纸（d1-d6.avif）
└── MobileWallpaper/          # 📱 移动端壁纸（m1-m6.avif）
```

---

## 🎯 快速导航：按场景查找要修改的文件

| 你想做什么？         | 修改这里                                                              |
| -------------------- | --------------------------------------------------------------------- |
| 改网站标题/描述/URL  | [src/config/siteConfig.ts](src/config/siteConfig.ts)                   |
| 换头像/昵称/社交链接 | [src/config/profileConfig.ts](src/config/profileConfig.ts)             |
| 调整导航菜单         | [src/config/navBarConfig.ts](src/config/navBarConfig.ts)               |
| 换壁纸               | [src/config/backgroundWallpaper.ts](src/config/backgroundWallpaper.ts) |
| 开关评论/换评论系统  | [src/config/commentConfig.ts](src/config/commentConfig.ts)             |
| 加音乐播放器         | [src/config/musicConfig.ts](src/config/musicConfig.ts)                 |
| 加统计分析           | [src/config/analyticsConfig.ts](src/config/analyticsConfig.ts)         |
| 侧边栏组件排序       | [src/config/sidebarConfig.ts](src/config/sidebarConfig.ts)             |
| 换字体               | [src/config/fontConfig.ts](src/config/fontConfig.ts)                   |
| 友链管理             | [src/config/friendsConfig.ts](src/config/friendsConfig.ts)             |
| 开关樱花特效         | [src/config/effectsConfig.ts](src/config/effectsConfig.ts)             |
| 看板娘设置           | [src/config/pioConfig.ts](src/config/pioConfig.ts)                     |
| 页脚 ICP 备案        | [src/config/footerConfig.ts](src/config/footerConfig.ts)               |
| 修改网站文案         | [src/i18n/languages/](src/i18n/languages/)                             |
| 写新文章             | `pnpm new-post <文件名>` → `src/content/posts/`                  |
| 自定义样式           | [src/styles/](src/styles/)                                             |
| 修改页面布局         | [src/layouts/](src/layouts/)                                           |
| 改组件行为           | [src/components/](src/components/)                                     |
