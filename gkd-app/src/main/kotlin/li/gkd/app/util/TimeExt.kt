package li.gkd.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val timeDifference = currentTime - timestamp

    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
    val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
    val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
    val weeks = days / 7
    val months = (days / 30)
    val years = (days / 365)
    return when {
        years > 0 -> "${years}年前"
        months > 0 -> "${months}月前"
        weeks > 0 -> "${weeks}周前"
        days > 0 -> "${days}天前"
        hours > 0 -> "${hours}小时前"
        minutes > 0 -> "${minutes}分钟前"
        else -> "刚刚"
    }
}

/**
 * 每个线程一份 SimpleDateFormat 缓存。
 *
 * 原来是全进程共享一个 HashMap<String, SimpleDateFormat>，但 SimpleDateFormat 与 HashMap
 * 都不是线程安全的，而 [format] 同时被主线程（各日志/记录页）和后台线程
 * (LogUtils 的 logFileExecutor 单线程) 调用。并发 format 会输出错乱的时间串，
 * 极端情况下抛 ArrayIndexOutOfBoundsException。
 * 改成 ThreadLocal 后既保证线程安全，也没有加锁开销。
 */
private val formatDateLocal = object : ThreadLocal<HashMap<String, SimpleDateFormat>>() {
    override fun initialValue() = HashMap<String, SimpleDateFormat>()
}

fun Long.format(formatStr: String): String {
    val map = formatDateLocal.get()!!
    var df = map[formatStr]
    if (df == null) {
        df = SimpleDateFormat(formatStr, Locale.getDefault())
        map[formatStr] = df
    }
    return df.format(this)
}

data class ThrottleTimer(
    private val interval: Long = 500L,
) {
    private var lastAccessTime: Long = 0L
    fun expired(): Boolean {
        val t = System.currentTimeMillis()
        if (t - lastAccessTime > interval) {
            lastAccessTime = t
            return true
        }
        return false
    }
}

@Composable
fun throttle(
    fn: (() -> Unit),
): (() -> Unit) {
    val timer = remember { ThrottleTimer() }
    return remember(fn) {
        {
            if (timer.expired()) {
                fn.invoke()
            }
        }
    }
}

@Composable
fun <T> throttle(
    fn: ((T) -> Unit),
): ((T) -> Unit) {
    val timer = remember { ThrottleTimer() }
    return remember(fn) {
        {
            if (timer.expired()) {
                fn.invoke(it)
            }
        }
    }
}
