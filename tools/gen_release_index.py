#!/usr/bin/env python3
"""生成应用内检查更新所需的 index.json (结构见 gkd-app 的 li.gkd.app.util.NewVersion)。

用法:
  python3 tools/gen_release_index.py \
      --apk out/gkd_pure-v0.1.1.apk \
      --tag v0.1.1 \
      --repo cookie-kangd/gkd_pure \
      --changelog CHANGELOG.md \
      --gradle-file gkd-app/build.gradle.kts \
      --out-index out/index.json \
      --out-notes out/release_notes.md

index.json 里的 downloadUrl 使用**原始的 github 直链**, 不写镜像前缀 ——
应用侧 (Upgrade.kt) 会在下载时自动优先走 gh-proxy 镜像、失败再回落直链,
这样即使以后换镜像也不必重新发版, 同时保证 index.json 本身是可移植的。
"""
import argparse
import hashlib
import json
import pathlib
import re
import sys

INSTALL_NOTES = """
### 安装说明

1. 下载下方 APK 安装（首次安装需在系统设置中允许该来源）
2. 本包与官方 GKD 签名不同，若已装官方版需先卸载，否则报签名冲突
3. 从本项目的 v0.1 起沿用同一密钥签名，可直接覆盖升级（OTA）
4. 应用内「检查更新」指向本仓库的 Release，下载会自动走 gh-proxy 镜像加速
"""


def parse_gradle_version(text: str) -> tuple[int, str]:
    """从 build.gradle.kts 的 defaultConfig 里取 versionCode / versionName。"""
    code_match = re.search(r"^\s*versionCode\s*=\s*(\d+)\s*$", text, re.M)
    name_match = re.search(r'^\s*versionName\s*=\s*"([^"]+)"\s*$', text, re.M)
    if not code_match or not name_match:
        raise SystemExit("无法从 build.gradle.kts 解析 versionCode / versionName")
    return int(code_match.group(1)), name_match.group(1)


def changelog_section(text: str, tag: str) -> str:
    """取 CHANGELOG.md 中 `## v0.1.1` 这一段, 到下一个 `## ` 为止。"""
    pattern = rf"^##\s+{re.escape(tag)}\s*$(.*?)(?=^##\s|\Z)"
    match = re.search(pattern, text, re.M | re.S)
    if not match:
        return "- 维护性更新"
    body = match.group(1).strip()
    return body or "- 维护性更新"


def check_last_release_version(about_file: pathlib.Path, prev_version: str) -> None:
    """「关于」页的 LAST_RELEASE_VERSION 必须等于上一个已发布版本, 传空则跳过。"""
    if not prev_version or not about_file.is_file():
        return
    text = about_file.read_text(encoding="utf-8")
    match = re.search(
        r'private const val LAST_RELEASE_VERSION\s*=\s*"([^"]+)"', text
    )
    if not match:
        raise SystemExit(f"在 {about_file} 里找不到 LAST_RELEASE_VERSION")
    actual = match.group(1)
    if actual != prev_version:
        raise SystemExit(
            f"「关于」页 LAST_RELEASE_VERSION={actual}, 但上一个 tag 是 {prev_version};"
            " 发版前请把它改成上一个版本号"
        )


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--apk", required=True)
    ap.add_argument("--tag", required=True)
    ap.add_argument("--repo", required=True, help="owner/name")
    ap.add_argument("--changelog", default="CHANGELOG.md")
    ap.add_argument("--gradle-file", default="gkd-app/build.gradle.kts")
    ap.add_argument("--out-index", required=True)
    ap.add_argument("--out-notes", required=True)
    ap.add_argument(
        "--prev-tag",
        default="",
        help="上一个 git tag, 用于校验「关于」页 LAST_RELEASE_VERSION 是否已同步",
    )
    ap.add_argument(
        "--about-file",
        default="gkd-app/src/main/kotlin/li/gkd/app/feature/settings/AboutPage.kt",
    )
    args = ap.parse_args()

    apk_path = pathlib.Path(args.apk)
    if not apk_path.is_file():
        raise SystemExit(f"APK 不存在: {apk_path}")
    apk_bytes = apk_path.read_bytes()

    version_code, version_name = parse_gradle_version(
        pathlib.Path(args.gradle_file).read_text(encoding="utf-8")
    )

    changelog_path = pathlib.Path(args.changelog)
    notes = (
        changelog_section(changelog_path.read_text(encoding="utf-8"), args.tag)
        if changelog_path.is_file()
        else "- 维护性更新"
    )

    # tag 与 gradle 里的 versionName 必须一致, 否则应用内比较和产物命名会对不上
    if args.tag.lstrip("v") != version_name:
        raise SystemExit(
            f"tag {args.tag} 与 build.gradle.kts 的 versionName {version_name} 不一致"
        )

    # 「关于」页的「上次更新」是手填常量, 最容易忘记同步 —— 有上一个 tag 就顺手校验
    check_last_release_version(
        pathlib.Path(args.about_file), args.prev_tag.lstrip("v")
    )

    download_url = (
        f"https://github.com/{args.repo}/releases/download/{args.tag}/{apk_path.name}"
    )

    index = {
        "versionCode": version_code,
        "versionName": version_name,
        "downloadUrl": download_url,
        "fileSize": len(apk_bytes),
        "versionLogs": [
            {"name": version_name, "code": version_code, "desc": notes}
        ],
    }

    out_index = pathlib.Path(args.out_index)
    out_index.parent.mkdir(parents=True, exist_ok=True)
    out_index.write_text(
        json.dumps(index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    out_notes = pathlib.Path(args.out_notes)
    out_notes.parent.mkdir(parents=True, exist_ok=True)
    out_notes.write_text(notes.rstrip() + "\n" + INSTALL_NOTES, encoding="utf-8")

    print(f"versionName = {version_name} (code {version_code})")
    print(f"apk         = {apk_path.name}  {len(apk_bytes)} bytes")
    print(f"sha256      = {hashlib.sha256(apk_bytes).hexdigest()}")
    print(f"downloadUrl = {download_url}")
    print(f"wrote       = {out_index}")
    print(f"wrote       = {out_notes}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
