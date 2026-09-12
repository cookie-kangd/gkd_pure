package li.gkd.app.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.serialization.Serializable
import li.gkd.app.ui.component.PerfIcon
import li.gkd.app.ui.share.LocalMainViewModel

sealed class BottomNavItem(
    val key: Int,
    val label: String,
    val icon: ImageVector,
) {
    object Dashboard : BottomNavItem(
        key = 0,
        label = "首页",
        icon = PerfIcon.Home,
    )

    object SubsManage : BottomNavItem(
        key = 1,
        label = "订阅",
        icon = PerfIcon.FormatListBulleted,
    )

    object AppList : BottomNavItem(
        key = 2,
        label = "应用",
        icon = PerfIcon.Apps,
    )

    object Settings : BottomNavItem(
        key = 3,
        label = "设置",
        icon = PerfIcon.Settings,
    )

    companion object {
        val allSubObjects by lazy { arrayOf(Dashboard, SubsManage, AppList, Settings) }
    }
}

@Serializable
data object HomeRoute : NavKey

@Composable
fun ResetPageScrollOnRequest(
    navItem: BottomNavItem,
    resetScroll: suspend () -> Unit,
) {
    val mainVm = LocalMainViewModel.current
    val request by mainVm.pageScrollResetRequestFlow.collectAsStateWithLifecycle()
    val currentRequest = request
    LaunchedEffect(currentRequest) {
        if (currentRequest?.navItem == navItem) {
            resetScroll()
            mainVm.consumePageScrollResetRequest(currentRequest)
        }
    }
}

@Composable
fun HomePage() {
    val mainVm = LocalMainViewModel.current
    viewModel<SubsManageVm>()
    val tab by mainVm.tabFlow.collectAsStateWithLifecycle()
    val selectedTab = BottomNavItem.allSubObjects.find { it.key == tab }
        ?: BottomNavItem.Dashboard
    val saveableStateHolder = rememberSaveableStateHolder()
    val hazeState = rememberHazeState()

    saveableStateHolder.SaveableStateProvider(selectedTab.key) {
        val page = when (selectedTab) {
            BottomNavItem.Dashboard -> useDashboardPage()
            BottomNavItem.SubsManage -> useSubsManagePage()
            BottomNavItem.AppList -> useAppListPage()
            BottomNavItem.Settings -> useSettingsPage()
        }
        Scaffold(
            modifier = page.modifier,
            topBar = page.topBar,
            // 浮岛底栏是 overlay, 不占 Scaffold 的 bottomBar 槽位, 否则内容被顶起、
            // 浮岛背后就没有可模糊的内容了。FAB 需要自己上移让开浮岛。
            floatingActionButton = {
                Box(modifier = Modifier.padding(bottom = DockContentClearance)) {
                    page.floatingActionButton()
                }
            },
            bottomBar = {},
        ) { contentPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // ⚠️ 模糊源只能包住「内容层」, 浮岛必须是它的兄弟节点, 不能是后代。
                // haze 的 SourceNode 一进入 draw() 就把 area.contentDrawing 置为 true,
                // 而 EffectNode 绘制时 require(!area.contentDrawing) —— 浮岛若是源的后代,
                // 抽帧读取源内容时正好落在绘制窗口内, 会直接抛异常(应用崩溃)。
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(hazeState)
                ) {
                    // 只在原 padding 的 bottom 上多加一段, 让列表末项能完整滚出浮岛区域。
                    // 这里手写实现而不是用 PaddingValues(start/top/end/bottom) 重建,
                    // 是因为只转发 left/right/top 不涉及 RTL 换算, 不会把左右搞反。
                    page.content(object : PaddingValues {
                        override fun calculateTopPadding() = contentPadding.calculateTopPadding()
                        override fun calculateBottomPadding() =
                            contentPadding.calculateBottomPadding() + DockContentClearance

                        override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
                            contentPadding.calculateLeftPadding(layoutDirection)

                        override fun calculateRightPadding(layoutDirection: LayoutDirection) =
                            contentPadding.calculateRightPadding(layoutDirection)
                    })
                }
                GlassBottomBar(
                    hazeState = hazeState,
                    selectedTab = selectedTab,
                    onTabClick = mainVm::handleClickTab,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}
