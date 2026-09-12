# 更新日志

> 本文件同时是应用内「检查更新」弹窗的版本说明来源：
> CI 会用 `tools/gen_release_index.py` 按 tag 抓取对应的 `## <tag>` 段落，
> 写进 Release 的 `index.json`。**每次发版前必须补上对应段落，标题与 tag 完全一致（含 `v`）。**

## v0.1.1

- 产物更名为 `gkd_pure-v<版本>.apk`，与官方 GKD 的 `gkd-v<版本>.apk` 区分开
- 「检查更新」改为指向本仓库（cookie-kangd/gkd_pure）的 Release，不再检查官方版本
- 下载自动优先走 gh-proxy 镜像加速国内网络，镜像不通时自动回落 GitHub 直链
- 「关于」页精简：去掉已无意义的更新渠道选择（本分支只有一条发布线）
- 「开源代码」与版本信息里的提交链接指向本仓库
- 安装提示：本包与官方 GKD 签名不同，若已装官方版需先卸载

## v0.1

- 基于 [gkd-kit/gkd](https://github.com/gkd-kit/gkd) v1.12.1 的首个纯净构建
- 使用本项目独立签名密钥，与官方版本不兼容，无法互相覆盖安装
