package li.gkd.app.util

/**
 * 受保护应用（资金交易类）：支付宝 / 微信 / 国内银行类等。
 *
 * 无论订阅规则（外部引入）或本地规则如何配置，规则引擎一律不对这些应用生效：
 * - AppStore.checkAppBlockMatch: 命中后 ActivityRule 的 appRules/globalRules 直接清空（不匹配、不轮询、不点击）
 * - A11yRuleEngine.queryAction: 循环内二次校验当前窗口包名，兜底任何绕过路径
 * 审计：SubscriptionRepository 导入/保存订阅时统计受保护应用并提示。
 */
object ProtectedApps {

    // 精确包名 → 展示名（安全审查列表 UI 使用）
    val exactAppNames = linkedMapOf(
        "com.eg.android.AlipayGphone" to "支付宝",
        "com.tencent.mm" to "微信（含支付与小程序）",
        "com.unionpay" to "云闪付",
        "com.apps.dcep" to "数字人民币",
    )
    private val exactApps: Set<String> get() = exactAppNames.keys

    // 包名关键词（大小写不敏感） → 展示说明：银行类包名繁杂，用关键词兜底。保持保守，避免误伤非金融应用
    val keywordDescs = listOf(
        "bank" to "绝大多数银行应用（工/建/农/中/交/招/邮储/浦发/民生/兴业/华夏等）",
        "unionpay" to "银联系应用",
        "alipay" to "支付宝系应用",
        "chinamworld" to "建设银行/中国银行部分版本",
        "bocmbci" to "中国银行",
        "cmbchina" to "招商银行",
        "cgbchina" to "广发银行",
        "dcep" to "数字人民币",
    )
    private val keywords: List<String> get() = keywordDescs.map { it.first }

    fun isProtected(appId: String?): Boolean {
        if (appId.isNullOrEmpty()) return false
        if (exactApps.contains(appId)) return true
        val lower = appId.lowercase()
        return keywords.any { lower.contains(it) }
    }

    /** 从包名集合里筛出受保护应用, 用于订阅导入审查 */
    fun filterProtected(appIds: Collection<String>): List<String> {
        return appIds.filter { isProtected(it) }
    }
}
