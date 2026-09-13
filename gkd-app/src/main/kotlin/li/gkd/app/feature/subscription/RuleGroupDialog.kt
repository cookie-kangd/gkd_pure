package li.gkd.app.feature.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import li.gkd.app.data.RawSubscription
import li.gkd.app.data.appinfo.AppInfoRepository
import li.gkd.app.ui.ImagePreviewItem
import li.gkd.app.ui.ImagePreviewRoute
import li.gkd.app.ui.component.AppAlertDialog
import li.gkd.app.ui.component.AppIcon
import li.gkd.app.ui.component.CopyableText
import li.gkd.app.ui.component.LazyCopyableText
import li.gkd.app.ui.component.PerfIcon
import li.gkd.app.ui.component.PerfIconButton
import li.gkd.app.ui.icon.ResetSettings
import li.gkd.app.ui.share.LocalDarkTheme
import li.gkd.app.ui.share.LocalMainViewModel
import li.gkd.app.ui.style.JSON5_LARGE_TEXT_THRESHOLD
import li.gkd.app.ui.style.getJson5AnnotatedString
import li.gkd.app.util.ProtectedApps
import li.gkd.app.util.throttle

@Composable
fun RuleGroupDialog(
    subs: RawSubscription,
    group: RawSubscription.RawGroupProps,
    appId: String?,
    onDismissRequest: () -> Unit,
    onClickEdit: (() -> Unit) = {},
    onClickEditExclude: () -> Unit,
    onClickResetSwitch: (() -> Unit)?,
    onClickDelete: () -> Unit = {}
) {
    val mainVm = LocalMainViewModel.current
    val source = group.cacheStr
    val darkTheme = LocalDarkTheme.current
    val annotatedText = remember(source, darkTheme) {
        getJson5AnnotatedString(source, darkTheme)
    }
    val targetRoute = remember(subs.id, appId, group.key) {
        if (group is RawSubscription.RawGlobalGroup) {
            SubsGlobalGroupListRoute(
                subsItemId = subs.id,
                focusGroupKey = group.key
            )
        } else {
            SubsAppGroupListRoute(
                subsItemId = subs.id,
                appId = appId.toString(),
                focusGroupKey = group.key
            )
        }
    }
    AppAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "规则详情") },
        text = {
            Column {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val maxHeight = 300.dp
                val textModifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = maxHeight)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                val contentPadding = PaddingValues(4.dp)
                if (source.length > JSON5_LARGE_TEXT_THRESHOLD) {
                    LazyCopyableText(
                        text = annotatedText,
                        modifier = textModifier,
                        contentPadding = contentPadding,
                        textStyle = MaterialTheme.typography.bodySmall,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        textContentDescription = "规则内容",
                    )
                } else {
                    CopyableText(
                        text = annotatedText,
                        textToCopy = source,
                        modifier = textModifier,
                        contentPadding = contentPadding,
                        textStyle = MaterialTheme.typography.bodySmall,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        textContentDescription = "规则内容",
                    )
                }
                Text(
                    text = source.length.toString(),
                    modifier = Modifier
                        .padding(end = 4.dp, bottom = 4.dp)
                        .align(Alignment.BottomEnd)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            ProtectedAppsSection(subs = subs, group = group, appId = appId)
            }
        },
        confirmButton = {
            Row {
                val currentRoute = mainVm.topRoute
                if (targetRoute::class != currentRoute::class) {
                    PerfIconButton(imageVector = PerfIcon.ArrowForward, onClick = throttle {
                        onDismissRequest()
                        mainVm.navigatePage(targetRoute)
                    })
                }
                if (group.allExampleUrls.isNotEmpty()) {
                    PerfIconButton(imageVector = PerfIcon.Image, onClick = throttle {
                        onDismissRequest()
                        mainVm.navigatePage(
                            ImagePreviewRoute(
                                title = group.name,
                                items = buildRuleGroupPreviewItems(group),
                            )
                        )
                    })
                }
                if (subs.isLocal) {
                    PerfIconButton(imageVector = PerfIcon.Edit, onClick = throttle(onClickEdit))
                }
                PerfIconButton(
                    imageVector = PerfIcon.Block,
                    onClickLabel = "编辑规则排除名单",
                    onClick = throttle(onClickEditExclude),
                )
                AnimatedVisibility(
                    visible = onClickResetSwitch != null,
                ) {
                    PerfIconButton(
                        imageVector = ResetSettings,
                        onClickLabel = "重置开关状态至默认值",
                        onClick = throttle(onClickResetSwitch ?: {}),
                    )
                }
                if (subs.isLocal) {
                    PerfIconButton(
                        imageVector = PerfIcon.Delete,
                        onClick = throttle(onClickDelete),
                    )
                }
            }
        },
    )
}

// 规则组示例图需要保留“图片属于哪个子规则”的上下文，预览页才能显示更具体的标题。
private fun buildRuleGroupPreviewItems(group: RawSubscription.RawGroupProps): List<ImagePreviewItem> {
    val uriTitlesMap = linkedMapOf<String, LinkedHashSet<String>>()

    fun addPreviewItem(uri: String, title: String?) {
        val titles = uriTitlesMap.getOrPut(uri) { linkedSetOf() }
        title?.takeIf { it.isNotBlank() }?.let(titles::add)
    }

    group.exampleUrls.orEmpty().forEach { uri ->
        addPreviewItem(
            uri = uri,
            title = group.name,
        )
    }
    group.rules.forEach { rule ->
        val ruleTitle = buildRulePreviewTitle(rule)
        rule.exampleUrls.orEmpty().forEach { uri ->
            addPreviewItem(
                uri = uri,
                title = ruleTitle,
            )
        }
    }

    return uriTitlesMap.map { (uri, titles) ->
        ImagePreviewItem(
            uri = uri,
            titles = titles.toList(),
        )
    }
}

private fun buildRulePreviewTitle(rule: RawSubscription.RawRuleProps): String? {
    return when {
        !rule.name.isNullOrBlank() -> rule.name
        rule.key != null -> "key=${rule.key}"
        !rule.preKeys.isNullOrEmpty() -> "preKeys=${(rule.preKeys as Iterable<Any?>).joinToString(",")}"
        else -> null
    }
}

@Composable
private fun ProtectedAppsSection(
    subs: RawSubscription,
    group: RawSubscription.RawGroupProps,
    appId: String?,
) {
    val appInfoMap by AppInfoRepository.appInfoMapFlow.collectAsStateWithLifecycle()
    val matchedAppIds = remember(subs.id, group.key, appId) {
        val ids: List<String> = when (group) {
            is RawSubscription.RawGlobalGroup -> group.appIdEnable.filterValues { it != false }.keys.toList()
            else -> listOfNotNull(appId)
        }
        ProtectedApps.filterProtected(ids)
    }
    if (matchedAppIds.isEmpty()) return
    Column {
        Text(
            text = "安全审查",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 12.dp),
        )
        matchedAppIds.forEach { id ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
            ) {
                AppIcon(appId = id)
                Column {
                    Text(
                        text = appInfoMap[id]?.name
                            ?: subs.apps.find { it.id == id }?.name
                            ?: ProtectedApps.exactAppNames[id]
                            ?: id,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                    )
                    Text(
                        text = id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        Text(
            text = "以上为资金交易类受保护应用，此规则在其中不会执行。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
