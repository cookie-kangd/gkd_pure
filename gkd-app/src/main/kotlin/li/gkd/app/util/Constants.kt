package li.gkd.app.util

const val FILE_SHORT_URL = "https://f.gkd.li/"
const val IMPORT_SHORT_URL = "https://i.gkd.li/i/"

const val SERVER_SCRIPT_URL =
    "https://registry.npmmirror.com/@gkd-kit/config/latest/files/dist/server.js"

const val REPOSITORY_URL = "https://github.com/cookie-kangd/gkd_pure"
const val ISSUES_URL = "${REPOSITORY_URL}/issues"

const val GITHUB_URL_PREFIX = "https://github.com/"

// 国内直连 github 的 release 下载不稳定, 统一走 gh-proxy 镜像; 镜像失败时自动回落直链
const val GITHUB_PROXY_PREFIX = "https://v4.gh-proxy.org/"

// 每次发版由 CI 生成并作为 Release 资产上传, 结构见 Upgrade.kt 的 NewVersion
const val RELEASE_INDEX_URL = "${REPOSITORY_URL}/releases/latest/download/index.json"

const val EMPTY_RULE_TIP = "暂无规则"

object ShortUrlSet {
    const val URL1 = "https://gkd.li?r=1"
    const val URL2 = "https://gkd.li?r=2"
    const val URL5 = "https://gkd.li?r=5"
    const val URL6 = "https://gkd.li?r=6"
    const val URL11 = "https://gkd.li?r=11"
    const val URL12 = "https://gkd.li?r=12"
    const val URL15 = "https://gkd.li?r=15"
}

const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=li.songe.gkd"

const val systemUiAppId = "com.android.systemui"
