package li.gkd.app.util

import li.gkd.app.store.AppStore

/**
 * 受保护应用（资金交易类）：支付宝 / 微信 / 国内银行类等。
 *
 * 安全审查默认开启（设置 - 其他 - 安全审查）。开启时无论订阅规则（外部引入）或本地规则如何配置，
 * 规则引擎一律不对这些应用生效：
 * - AppStore.checkAppBlockMatch: 命中后 ActivityRule 的 appRules/globalRules 直接清空（不匹配、不轮询、不点击）
 * - A11yRuleEngine.queryAction: 循环内二次校验当前窗口包名，兜底任何绕过路径
 * 审计：SubscriptionRepository 导入/保存订阅时统计受保护应用并提示。
 *
 * 关闭开关后，上述引擎拦截、订阅审计提示及所有「受保护」UI 标记一并停用；
 * 名单本身仍保留，可在 设置 - 其他 - 安全审查列表 查看。
 */
object ProtectedApps {

    // 精确包名 → 展示名（安全审查列表 UI 使用）
    val exactAppNames = linkedMapOf(
        "com.eg.android.AlipayGphone" to "支付宝",
        "com.tencent.mm" to "微信（含支付与小程序）",
        "com.unionpay" to "云闪付",
        "com.apps.dcep" to "数字人民币",
        "com.icbc" to "工商银行",
        "cmb.pb" to "招商银行",
        "com.cib.cibmb" to "兴业银行",
    )
    private val exactApps: Set<String> get() = exactAppNames.keys

    // 包名关键词（大小写不敏感） → 展示说明：银行类包名繁杂，用关键词兜底。保持保守，避免误伤非金融应用
    val keywordDescs = listOf(
        "bank" to "绝大多数银行应用（建/农/中/交/邮储/浦发/民生/中信/光大/广发/平安等）",
        "unionpay" to "银联系应用",
        "alipay" to "支付宝系应用",
        "chinamworld" to "建设银行/中国银行部分版本",
        "bocmbci" to "中国银行",
        "cmbchina" to "招商银行（掌上生活）",
        "cgbchina" to "广发银行",
        "icbc" to "工商银行系应用",
        "cibmb" to "兴业银行系应用",
        "dcep" to "数字人民币",
    )
    private val keywords: List<String> get() = keywordDescs.map { it.first }

    /** 安全审查总开关状态, 默认开启 */
    fun isEnabled(): Boolean = AppStore.storeFlow.value.enableSecurityReview

    /** 名单判定: 纯函数, 与开关状态无关, 供名单展示与诊断使用 */
    fun isProtected(appId: String?): Boolean {
        if (appId.isNullOrEmpty()) return false
        if (exactApps.contains(appId)) return true
        val lower = appId.lowercase()
        return keywords.any { lower.contains(it) }
    }

    /** 生效判定: 关闭开关后不再拦截任何受保护应用 */
    fun shouldProtect(appId: String?, enabled: Boolean = isEnabled()): Boolean =
        enabled && isProtected(appId)

    /** 从包名集合里筛出受保护应用, 用于订阅导入审查与 UI 标记; 开关关闭时返回空列表 */
    fun filterProtected(appIds: Collection<String>, enabled: Boolean = isEnabled()): List<String> =
        if (!enabled) emptyList() else appIds.filter { isProtected(it) }
}
