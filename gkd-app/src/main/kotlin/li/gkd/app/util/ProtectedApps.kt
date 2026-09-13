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

    // 精确包名
    private val exactApps = setOf(
        "com.eg.android.AlipayGphone", // 支付宝
        "com.tencent.mm", // 微信（含支付与小程序）
        "com.unionpay", // 云闪付
        "com.apps.dcep", // 数字人民币
    )

    // 包名关键词（大小写不敏感）：银行类包名繁杂，用关键词兜底。保持保守，避免误伤非金融应用
    private val keywords = listOf(
        "bank", // 绝大多数银行应用包名含 bank（icbc/bocmbci/bankcomm/mbank/psbc 等）
        "unionpay",
        "alipay",
        "chinamworld", // 建行/中行部分版本
        "bocmbci", // 中国银行
        "cmbchina", // 招商银行
        "cgbchina", // 广发银行
        "dcep", // 数字人民币
    )

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
