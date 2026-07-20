# Wordbook - 英语单词本

基于 Android 原生 Kotlin + Jetpack Compose 的英语单词学习应用。

## 功能

- **闪卡学习** — 正面单词+音标 / 反面释义+例句，5级评分（Again/Hard/Good/Easy/完美）
- **SM-2 间隔复习** — 基于遗忘曲线的智能复习算法，自动计算下次复习时间
- **例句练习** — 将当前批次的例句随机排列展示，目标单词红色标注
- **学习历史** — 所有学过单词列表，按添加时间排序，背景色反映掌握程度
- **学习记录** — 当月日历视图，绿色标记有学习的日期，查看每日学习量

## 技术栈

- Kotlin + Jetpack Compose (Material 3)
- Room Database (SQLite)
- Navigation Compose
- MVVM 架构 (ViewModel + StateFlow)
- Gson (JSON 解析)
- DataStore Preferences

## 数据

单词数据取自高中 3500 英语词汇表（实际 3933 词），包含音标、释义、例句及翻译。

## 构建

1. 用 Android Studio 打开本项目
2. 等待 Gradle 同步完成
3. 连接 Android 设备或启动模拟器
4. 点击 Run

最低 SDK: Android 8.0 (API 26)
目标 SDK: Android 14 (API 34)

## 项目结构

```
app/src/main/java/com/wordbook/app/
├── data/                  # 数据层
│   ├── entity/            # Room 实体
│   ├── dao/               # DAO 接口
│   ├── database/          # 数据库
│   └── repository/        # 仓库
├── review/                # SM-2 算法
├── ui/                    # UI 层
│   ├── theme/             # 主题
│   ├── navigation/        # 导航
│   ├── home/              # 首页
│   ├── study/             # 闪卡学习
│   ├── example/           # 例句练习
│   ├── history/           # 学习历史
│   ├── detail/            # 单词详情
│   └── record/            # 学习记录
└── util/                  # 工具类
```
