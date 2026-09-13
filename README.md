<div align="center">

# 🧊 gkd_pure

**干净、不联网上报的自用版 GKD**

基于「高级选择器 + 订阅规则」的安卓屏幕自动化点击工具 —— 去统计、去埋点、去外部入口，只保留功能本身

[![Release](https://img.shields.io/github/v/release/cookie-kangd/gkd_pure?style=flat-square&label=%E6%9C%80%E6%96%B0%E7%89%88%E6%9C%AC)](https://github.com/cookie-kangd/gkd_pure/releases/latest)
[![CI](https://img.shields.io/github/actions/workflow/status/cookie-kangd/gkd_pure/Build-Release.yml?style=flat-square&label=Release%20CI)](https://github.com/cookie-kangd/gkd_pure/actions/workflows/Build-Release.yml)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen?style=flat-square)]()
[![License](https://img.shields.io/badge/License-GPL--3.0--only-lightgrey?style=flat-square)](./LICENSE)

[下载安装](#-下载安装) · [隐私与权限](#-隐私与权限) · [本版改动](#-本版改动) · [功能一览](#-功能一览) · [省电与性能](#-省电与性能) · [常见问题](#-常见问题) · [自行构建](#-自行构建)

基于 [gkd-kit/gkd](https://github.com/gkd-kit/gkd) 的个人自用构建，**功能与上游保持一致**，只调整发布链路、界面外观与少量默认行为。

</div>

> 上游项目：[gkd-kit/gkd](https://github.com/gkd-kit/gkd) · 相关项目：[listenDTV](https://github.com/cookie-kangd/listenDTV) · [dtv_mx](https://github.com/cookie-kangd/dtv_mx)

---

## 📥 下载安装

前往 [**Releases（最新版）**](https://github.com/cookie-kangd/gkd_pure/releases/latest) 下载 APK 安装即可：

| 设备 | 架构 | 文件 |
|---|---|---|
| 手机 / 平板 / 模拟器 | arm64-v8a、x86_64（同一个包） | `gkd_pure-v0.1.10.apk` |

国内直连 GitHub 下载慢？把 APK 链接前面拼上加速镜像前缀即可：

| 来源 | 链接前缀 |
|---|---|
| GitHub 直连 | 无 |
| **Cloudflare (v4 推荐)** ⭐ | `https://v4.gh-proxy.org/` |
| Cloudflare (v4/v6) | `https://v6.gh-proxy.org/` |
| Fastly (v4) | `https://cdn.gh-proxy.org/` |

示例：`https://v4.gh-proxy.org/https://github.com/cookie-kangd/gkd_pure/releases/download/v0.1.10/gkd_pure-v0.1.10.apk`

- **系统要求**：Android 8.0（API 26）及以上
- **应用内更新**：设置 → 关于 → 检查更新（已内置镜像加速 + 直链兜底，见下文）
- **安装前必读**：本包使用本项目自己的签名密钥与独立包名（v0.1.8 起为 `io.github.cookiekangd.gkdpure`），**与官方 GKD 签名、包名均不同**，不能互相覆盖安装。
  若已装官方版，或本项目 v0.1.7 及更早版本（旧包名 `li.songe.gkd`），需先卸载再装本版；v0.1.8 起的版本之间可正常覆盖升级。

---

## 🔒 隐私与权限

**本项目不包含任何统计、埋点或崩溃上报 SDK**（无 Firebase / Sentry / Bugly / 友盟 / 广告 SDK）。
崩溃记录只写在本机应用私有目录，不上传；「关于」页不再有捐赠、问题反馈、导出日志等外部入口。

应用的全部对外网络请求如下（均可在代码中检索到出处）：

| 请求目标 | 用途 | 触发时机 |
|---|---|---|
| 本仓库 `releases/latest/download/index.json` | 检查更新 | 你主动点「检查更新」 |
| GitHub Release / `gh-proxy` 镜像 | 下载新版 APK | 你确认下载后 |
| 你添加的订阅链接 | 拉取规则订阅 | 你添加订阅后 / 你设置的自动更新周期（最短 1 天） |
| `f.gkd.li` / `i.gkd.li` 短链服务 | 快照分享与快照导入链接 | 你主动生成/分享快照时 |
| `registry.npmmirror.com/@gkd-kit/config` | HTTP 调试页加载的网页脚本 | 你开启 HTTP 服务并用浏览器访问时 |
| 用户主动发布的快照 / 日志链接 | 生成分享链接（需自备网络环境） | 你主动点「生成链接」时 |

**受保护应用（v0.1.9 起）**：支付宝、微信、云闪付、数字人民币及国内银行类应用（包名含 `bank` 等特征）
属于资金交易类应用，本版在规则引擎层对其**硬禁止**——无论订阅规则（外部引入）或本地规则怎么写，
都不会在这些应用内匹配、轮询或点击；导入包含这类规则的订阅时会明确提示拦截。
完整名单可在 设置 → 其他 → 安全审查列表 查看（v0.1.10 起），规则列表中受保护应用的开关会显示为关闭。
该名单与任何设置无关，无法绕过。

**权限清单**（每一项都是功能必需，没有多余权限）：

| 权限 | 用途 |
|---|---|
| 无障碍服务（`canRetrieveWindowContent` / `canTakeScreenshot`） | 读取界面节点并按规则点击，这是 GKD 的核心能力；只监听窗口内容变化与窗口切换两类事件 |
| `INTERNET` | 拉取订阅、检查更新 |
| `QUERY_ALL_PACKAGES` | 列出已安装应用以便按应用配置规则 |
| `POST_NOTIFICATIONS` / `FOREGROUND_SERVICE(_SPECIAL_USE)` | 常驻状态通知与服务保活 |
| `SYSTEM_ALERT_WINDOW` | 悬浮按钮 / 悬浮事件窗（可关） |
| `WRITE_SECURE_SETTINGS`、`GET_APP_OPS_STATS` | 仅在通过 Shizuku 授予特权服务后使用 |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | 由你在工作模式页主动申请，降低后台被系统杀掉概率 |
| `WRITE_EXTERNAL_STORAGE`（仅 Android 9 及以下） | 保存截图/文件到公共存储 |

> ⚠️ **一项需要你知情的能力**：内置的「HTTP 服务」（设置 → 高级）用于在电脑浏览器上调试规则，
> 开启后会在所有网卡上监听且**无鉴权**，同一局域网内的设备可以访问快照 / 截图接口。
> 它默认关闭、需要手动开启，用完请及时关闭；本版没有改动它的行为，以免破坏你现有的调试流程。

---

## ✨ 本版改动（相对官方）

| 项目 | 官方版 | 本版 |
| --- | --- | --- |
| 应用名 | GKD | `gkd_pure` |
| 包名 | `li.songe.gkd`（与商店官方版相同） | `io.github.cookiekangd.gkdpure`（v0.1.8 起，商店不再误报更新） |
| 产物名 | `gkd-v<版本>.apk` | `gkd_pure-v<版本>.apk` |
| 检查更新 | 官方 npmmirror 通道 | 本仓库的 GitHub Release |
| 下载加速 | — | 自动优先走 `gh-proxy` 镜像，失败回落直链 |
| 更新渠道选择 | 稳定版 / 测试版 | 已移除（只有一条发布线） |
| 底栏 | 系统默认导航栏 | 毛玻璃浮岛 |
| 应用图标 | GKD 原版 | 纯净风格重绘（大圆角菱形镂空三棒） |
| 站内文档跳转 | 常见问题 / 帮助等指向 gkd.li | 已全部移除（不内嵌网页查看器） |
| 「关于」页 | 多项外部入口 | 仅 简介 / 检查更新 / 上次更新 |
| 首页 | 有「了解 GKD」入口 | 已移除 |
| 数据备份通道 | `allowBackup=true` | 已关闭（私有数据不可被备份提取） |

**v0.1.10 具体内容**见 [CHANGELOG.md](./CHANGELOG.md)。自 v0.1.8 起使用独立包名不再与官方商店互相干扰；v0.1.9 起内置支付宝/微信/银行类等资金交易应用的强制保护名单；v0.1.10 起提供安全审查可视化（规则弹窗安全审查栏目 + 设置-其他安全审查列表）。

---

## 🧭 功能一览

### 🎯 自动化点击
- **自定义规则**：用[高级选择器](https://gkd.li/guide/selector)定位控件（文字、id、位置、关系），满足条件时自动点击
- **订阅规则**：一次订阅即可批量管理各类应用的规则；支持多订阅、分组开关、订阅内搜索
- **全局规则**：不绑定具体应用，跨应用生效
- **规则分类**：按 类别 / 应用 / 订阅 三种维度配置与排查
- **故障排查**：命中计数、待触发规则、慢选择器提示、规则全量搜索

### 📸 快照与调试
- **一键快照**：抓取当前界面节点树 + 截图，用于编写/验证选择器
- **快照审查**：导出文件后在 [gkd-kit/inspect](https://github.com/gkd-kit/inspect) 网页端审查
- **内置 HTTP 服务**：连电脑浏览器实时查看节点树、执行选择器（默认关闭）

### 📋 记录与状态
- **触发记录 / 活动记录 / 无障碍事件日志**：出问题可快速定位是哪个规则动作
- **常驻状态通知**：显示服务运行状态与当前应用
- **快捷磁贴**：通知栏快捷开关（暂停/恢复、快照、HTTP 服务、规则匹配、记录等）

### 🎨 外观
- **毛玻璃浮岛底栏**：滚动内容从浮岛背后透出并实时模糊（Android 12 以下自动退回半透明底）
- **主题配色**：多套配色可选，跟随系统深浅色
- **应用图标 / 名称可替换**：便于区分不同用途的安装实例

---

## ⚡ 省电与性能

v0.1.3 做过的优化（均为不改行为的低风险改动）：

- **去掉每秒权限轮询**：原来「工作模式」页停留期间每秒执行一次全部权限探测
  （含十几次 binder 调用与一次特权服务跨进程 IPC），改为进入页面 / 回到前台时刷新一次
- **修复规则匹配重复执行**：`querying` 标志原先在协程内部才置位，事件线程与动作线程可能
  同时通过检查，导致一整轮「遍历规则 + 匹配节点树 + 写动作日志」被执行两次；现改为进入同步块即置位
- **修复时间格式化并发崩溃**：全进程共享的非线程安全 `SimpleDateFormat` 改为每线程一份，
  消除多线程并发格式化导致的错乱时间串与 `ArrayIndexOutOfBoundsException`
- **降低列表排序复杂度**：应用配置页「按动作时间」排序原先在比较函数里做线性查找
  （O(分组数 × 日志数)），每次规则命中都会触发整轮重排，改为先建索引（O(分组数 + 日志数)）

---

## ❓ 常见问题

<details>
<summary><b>装不上 / 提示签名冲突？</b></summary>

本包与官方 GKD 签名不同，且自 v0.1.8 起包名也不同（`io.github.cookiekangd.gkdpure`）。
若手机上已有官方版（或其它人构建的版本），必须先卸载再安装本版；
本项目 v0.1.7 及更早版本（旧包名 `li.songe.gkd`）也无法被 v0.1.8 覆盖，同样需先卸载（旧版订阅需重新导入）。从 v0.1.8 起签名与包名一致，之后可以正常覆盖升级。
</details>

<details>
<summary><b>支付宝 / 微信 / 银行 app 里会生效吗？</b></summary>

不会。v0.1.9 起内置「受保护应用」名单（资金交易类：支付宝、微信、云闪付、数字人民币、银行类），
无论订阅规则（外部引入）或本地规则如何配置，引擎都不会在这些应用内执行任何匹配或点击，
导入含此类规则的订阅时也会明确提示。这是引擎层强制，不是可开关的设置。
</details>

<details>
<summary><b>应用内「检查更新」没反应？</b></summary>

检查更新读取的是固定地址 `releases/latest/download/index.json`，会优先走 `gh-proxy` 镜像、
失败后自动回落 GitHub 直链。如果两个通道都不通，请确认网络能访问 GitHub 或其镜像。
</details>

<details>
<summary><b>为什么不用系统自带的「检查更新」渠道了？</b></summary>

官方渠道指向官方 GKD 的发布地址，签名不同装不上；本版改为指向本仓库的 Release，才能真正用来自我更替。
</details>

<details>
<summary><b>GKD 需要 root 吗？</b></summary>

不需要。核心功能基于系统无障碍服务。若要使用「特权服务」（更高权限的自动化、免确认授予权限等），
可以通过 Shizuku 授权，无需 root。
</details>

<details>
<summary><b>默认有规则吗？</b></summary>

GKD **默认不提供规则**。需要自行添加本地规则，或通过订阅链接获取远程规则。
第三方订阅列表见 <https://github.com/topics/gkd-subscription>。
</details>

<details>
<summary><b>和官方版能共存吗？会互相干扰吗？</b></summary>

v0.1.8 起本版使用独立包名 `io.github.cookiekangd.gkdpure`，与官方版（`li.songe.gkd`）完全独立，
两者可以共存、互不干扰，应用商店也不会再把官方版更新误报到本包上。
GKD 规则匹配的是**目标应用**的包名（如微信、抖音），与本 app 自身的包名无关，
因此 v0.1.8 更换包名不影响任何规则与跳广告能力。
</details>

---

## 🛠 自行构建

**环境要求**

- JDK 21
- Android SDK（compileSdk 37）
- 构建链：Gradle 9.7.x + AGP 9.x + Kotlin 2.4.x

**构建命令**

```bash
# Debug
./gradlew :gkd-app:assembleDebug

# Release（需签名）
./gradlew :gkd-app:assembleRelease
```

**签名**：Release 构建从仓库 Secrets / 环境变量读取 keystore（`GKD_STORE_FILE_BASE64` 等），
缺失时回落 debug 签名 —— 能出包但每次签名不同，会导致无法覆盖升级，仅用于冒烟验证。

> ⚠️ 签名密钥请离线备份。密钥丢失后，已安装的用户只能卸载重装。

**发版流程**

1. 改 `gkd-app/build.gradle.kts` 里的 `versionCode`（+1）与 `versionName`
2. 在 `CHANGELOG.md` 补一段 `## v<新版本>`（内容会进 Release 说明和更新弹窗）
3. 同步 `AboutPage.kt` 的 `LAST_RELEASE_VERSION` 为上一个版本号（漏改 CI 会直接失败）
4. 推送 `main`，再推 tag：`git tag v0.1.10 && git push origin v0.1.10`

`Build-Release.yml` 会自动构建、生成 `index.json`、创建 Release；
`Build-Apk.yml` 只在**功能分支**上跑编译冒烟（已刻意排除 `main`，避免同一次发版并排跑两个构建）。

---

## 🧱 技术栈

| 类别 | 选型 |
|---|---|
| 语言 | Kotlin 2.4 |
| UI | Jetpack Compose 1.12（Material 3）、Navigation 3 |
| 毛玻璃 | chrisbanes/haze |
| 数据库 | Room 3 + SQLite（多平台） |
| 网络 | Ktor Client / Server（OkHttp 引擎） |
| 图片 | Coil 3、telephoto |
| 选择器引擎 | 自研 `gkd-selector`（Kotlin Multiplatform） |
| 特权能力 | Shizuku、priv-kit |
| CI/CD | GitHub Actions，自动构建 + Release 发布 |

---

## 📄 说明

- 本项目基于 [gkd-kit/gkd](https://github.com/gkd-kit/gkd) 二次构建，遵循 **GPL-3.0-only**，仅供学习与技术交流
- 原始项目版权归 [@lisonge](https://github.com/lisonge) 与 [gkd-kit](https://github.com/gkd-kit) 所有
- 如果 GKD 对你有用，可以支持原作者：<https://github.com/lisonge/sponsor>

---

## 🔍 关键词

GKD · 安卓自动化点击 · 屏幕自动化 · 无障碍服务 · 高级选择器 · 订阅规则 · 跳过开屏广告 · 自动确认 · 无埋点 · 无统计 · 开源安卓工具 · 毛玻璃底栏 · gh-proxy 加速下载 · OTA 自更新
