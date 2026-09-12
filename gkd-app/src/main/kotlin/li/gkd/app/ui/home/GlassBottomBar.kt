package li.gkd.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import li.gkd.app.ui.component.PerfIcon

/** 底栏浮岛在屏幕上占据的净高度, 列表用它预留底部空间, 保证最后一项能完整滚出浮岛。 */
val DockContentClearance = 96.dp

/**
 * 毛玻璃浮岛底栏。
 *
 * 模糊源由 HomePage 注册在内容层上（`Modifier.hazeSource`），这里只负责把自身画成玻璃：
 * 半透明 tint + 模糊 + 自上而下的白色高光渐变 + 描边，四种元素叠起来才是玻璃质感。
 * Android 12 以下没有 RenderEffect，haze 自动退回半透明 scrim，不会糊成一团。
 */
@Composable
fun GlassBottomBar(
    hazeState: HazeState,
    selectedTab: BottomNavItem,
    onTabClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    // 毛玻璃 tint：浅色偏白、深色偏黑，blur 完成后叠加这层色彩保证两种模式下都可读
    val glassTint = if (isDark) {
        Color(0xFF1C1C22).copy(alpha = 0.58f)
    } else {
        Color(0xFFF6F7FB).copy(alpha = 0.60f)
    }
    val glassStyle = HazeStyle(
        tints = listOf(HazeTint(glassTint)),
        blurRadius = 24.dp,
        noiseFactor = 0.06f,
    )
    val dockShape = RoundedCornerShape(percent = 50)
    // 描边要能明确看出是「一块玻璃」：深色模式用偏白的亮边勾轮廓，浅色模式用高亮白边靠阴影拉开层次
    val borderColor = if (isDark) Color.White.copy(alpha = 0.30f) else Color.White.copy(alpha = 0.90f)
    val glassHighlight = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f), Color.Transparent)
        } else {
            listOf(Color.White.copy(alpha = 0.42f), Color.White.copy(alpha = 0.10f), Color.Transparent)
        },
    )
    val activeBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    // 深色底用亮灰、浅色底用深灰，保证两种模式下的对比度
    val inactiveIcon = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = if (isDark) 10.dp else 16.dp, shape = dockShape, clip = false)
                .clip(dockShape)
                .hazeEffect(hazeState, style = glassStyle)
                .background(brush = glassHighlight, shape = dockShape)
                .border(BorderStroke(1.6.dp, borderColor), dockShape)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomNavItem.allSubObjects.forEach { navItem ->
                val selected = navItem == selectedTab
                DockItem(
                    selected = selected,
                    label = navItem.label,
                    activeBackground = activeBg,
                    onClick = { onTabClick(navItem) },
                ) {
                    PerfIcon(
                        imageVector = navItem.icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else inactiveIcon,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.DockItem(
    selected: Boolean,
    label: String,
    activeBackground: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val scale by animateFloatAsState(targetValue = if (selected) 1.06f else 1.0f, label = "dockScale")
    val bgAlpha by animateFloatAsState(targetValue = if (selected) 1.0f else 0.0f, label = "dockBgAlpha")
    // 去掉默认的矩形水波纹：胶囊浮岛里整块方形灰底非常突兀，
    // 切换反馈改由选中态的胶囊高亮 + 图标/文字变色表达。
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .defaultMinSize(minHeight = 48.dp)
            .padding(vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = activeBackground.copy(alpha = activeBackground.alpha * bgAlpha),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)) {
                Box(modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale)) {
                    icon()
                }
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
            ),
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
