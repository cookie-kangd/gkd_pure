# gkd_pure

基于 [gkd-kit/gkd](https://github.com/gkd-kit/gkd) 的个人自用构建，跟随上游代码，仅调整发布链路、界面与少量默认行为。

> **与官方版不兼容**：本包使用本项目自己的签名密钥，与官方 GKD 的签名不同，
> 两者**不能互相覆盖安装**。若已装官方版，必须先卸载再装本版。
> 反之，本版之后的版本之间可以正常覆盖升级。

## 与官方版的差异

| 项目 | 官方版 | 本版 |
| --- | --- | --- |
| 应用名 | GKD | gkd_pure |
| 产物名 | `gkd-v<版本>.apk` | `gkd_pure-v<版本>.apk` |
| 检查更新 | 官方 npmmirror 通道 | 本仓库的 GitHub Release |
| 下载加速 | — | 自动优先走 `gh-proxy` 镜像 |
| 更新渠道选择 | 稳定版 / 测试版 | 已移除（本分支只有一条发布线） |
| 底栏 | 系统默认导航栏 | 毛玻璃浮岛 |
| 「关于」页 | 多项外部入口 | 仅简介 / 检查更新 / 上次更新 |

其余功能与上游一致。

## 安装

下载最新 [Release](https://github.com/cookie-kangd/gkd_pure/releases/latest) 里的 `gkd_pure-v*.apk` 直接安装。

国内网络如果直连 GitHub 较慢，可用镜像前缀：

```
https://v4.gh-proxy.org/https://github.com/cookie-kangd/gkd_pure/releases/latest/download/gkd_pure-v0.1.2.apk
```

（应用内的「设置 → 关于 → 检查更新」已经内置了这个镜像，无需手动处理）

安装后需要在系统设置 / 无障碍设置中授予无障碍服务权限，GKD 才能工作。

## 更新机制

- 每次发版时，CI 会把构建产物与一份 `index.json` 一起发布到 Release。
- 应用内「检查更新」读取的是固定地址
  `https://github.com/cookie-kangd/gkd_pure/releases/latest/download/index.json`，
  因此永远指向**最新**的 Release，不需要随版本改代码。
- 下载 github 链接时会先尝试 `https://v4.gh-proxy.org/` 镜像，失败自动回落直链。
- 版本号比较用的是 `versionCode`，所以每次发版必须**递增**。

## 发版流程

1. 改 `gkd-app/build.gradle.kts` 里的 `versionCode`（+1）与 `versionName`。
2. 在 `CHANGELOG.md` 里补一段 `## v<新版本>`，内容会进 Release 说明和更新弹窗。
3. 提交到 `main`，然后打并推送 tag：

```bash
git tag v0.1.2 && git push origin v0.1.2
```

4. `.github/workflows/Build-Release.yml` 会自动构建、生成 `index.json`、创建 Release。
   签名密钥取自仓库 Secrets（`GKD_STORE_FILE_BASE64` 等），缺失时回落 debug 签名
   （能出包，但每次签名都不同，会导致无法覆盖升级）。

> ⚠️ 签名密钥请离线备份。密钥丢失后，已安装的用户只能卸载重装。

## 免责声明

本项目遵循 [GPL-3.0-only](/LICENSE) 开源，仅供学习交流，禁止用于商业或非法用途。
原始项目版权归 [gkd-kit](https://github.com/gkd-kit/gkd) 所有。

## 上游文档（仍然适用）

GKD 基于 [高级选择器](https://gkd.li/guide/selector) + [订阅规则](https://gkd.li/guide/subscription) + [快照审查](https://github.com/gkd-kit/inspect)，
通过自定义规则，在指定界面满足指定条件（如屏幕上存在特定文字）时，点击特定节点或位置。

- **快捷操作**：简化重复流程，如某些软件自动确认登录
- **跳过流程**：跳过某些软件启动时的烦人流程

GKD **默认不提供规则**，需自行添加本地规则，或通过订阅链接获取远程规则。
第三方订阅列表见 <https://github.com/topics/gkd-subscription>；
也可用 [subscription-template](https://github.com/gkd-kit/subscription-template) 构建自己的远程订阅。

如遇问题请先查看 [疑难解答](https://gkd.li/guide/faq)。选择器语法见 <https://gkd.li/guide/selector>。

## 相关项目

上游开发过程中的衍生项目：

- [kotlin-json5](https://github.com/lisonge/kotlin-json5)
- [kotlin-codeorigin](https://github.com/lisonge/kotlin-codeorigin)
- [android-api-diff](https://github.com/android-cs/android-api-diff)
- [remap](https://github.com/lisonge/remap)
- [priv-kit](https://github.com/priv-kit/priv-kit)

## 支持上游

如果 GKD 对你有用，可以支持原作者：<https://github.com/lisonge/sponsor>
