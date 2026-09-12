package li.gkd.app.util

import li.gkd.app.util.ToastUtils.toast

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import li.gkd.app.META
import li.gkd.app.app
import li.gkd.app.store.FileStateStore
import li.gkd.app.store.AppStore.storeFlow
import li.gkd.app.ui.component.AppAlertDialog
import li.songe.codeorigin.CallSite
import java.io.File
import java.net.URI
import kotlin.time.Duration.Companion.days


private val UPDATE_URL: String
    get() = UpdateChannelOption.objects.findOption(storeFlow.value.updateChannel).url

@Serializable
data class NewVersion(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val fileSize: Long,
    val versionLogs: List<VersionLog> = emptyList(),
)

@Serializable
data class VersionLog(
    val name: String,
    val code: Int,
    val desc: String,
)

private var lastCheckTime = 0L

/**
 * 国内直连 github 的 release 资源不稳定, github 链接优先走 gh-proxy 镜像, 失败再回落直链。
 * 返回顺序即尝试顺序; 非 github 链接原样返回单个元素。
 */
private fun mirrorUrlList(url: String): List<String> {
    if (!url.startsWith(GITHUB_URL_PREFIX)) return listOf(url)
    return listOf(GITHUB_PROXY_PREFIX + url, url)
}

private suspend fun fetchNewVersion(): NewVersion {
    var lastError: Exception? = null
    for (url in mirrorUrlList(UPDATE_URL)) {
        try {
            return client.get(url).body<NewVersion>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastError = e
        }
    }
    throw (lastError ?: Exception("检查更新失败"))
}

class UpdateStatus(val scope: CoroutineScope) {
    private val checkUpdatingMutex = MutexState()
    val checkUpdatingFlow
        get() = checkUpdatingMutex.state
    private val newVersionFlow = MutableStateFlow<NewVersion?>(null)
    private val downloadStatusFlow = MutableStateFlow<LoadStatus<File>?>(null)
    private var downloadJob: Job? = null

    private val ignoreVersionListFlow by lazy {
        FileStateStore.createJsonFlow(
            key = "ignore_version_list",
            default = { emptySet<Int>() },
            scope = scope,
        )
    }
    private var lastManual = false

    val canRecheck get() = System.currentTimeMillis() - lastCheckTime > 1.days.inWholeMilliseconds

    fun checkUpdate(
        manual: Boolean = false,
        @CallSite loc: String = "",
    ) {
        scope.launchLogged(Dispatchers.IO, loc = loc) {
            try {
                lastManual = manual
                checkUpdatingMutex.tryWithStateLock {
                    lastCheckTime = System.currentTimeMillis()
                    if (!NetworkUtils.isAvailable()) {
                        error("网络不可用")
                    }
                    val newVersion = fetchNewVersion()
                    if (newVersion.versionCode <= META.versionCode) {
                        if (manual) toast("暂无更新", loc = loc)
                        return@tryWithStateLock
                    }
                    if (
                        !manual &&
                        ignoreVersionListFlow.value.contains(newVersion.versionCode)
                    ) return@tryWithStateLock
                    newVersionFlow.value = newVersion
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (manual) {
                    toast(e.message ?: e.stackTraceToString(), loc = "")
                }
                throw e
            }
        }
    }

    private fun startDownload(newVersion: NewVersion) {
        if (downloadStatusFlow.value is LoadStatus.Loading) return
        downloadStatusFlow.value = LoadStatus.Loading(0f)
        val apkFile = FolderUtils.sharedDir.resolve("gkd-v${newVersion.versionCode}.apk").apply {
            if (exists()) {
                delete()
            }
        }
        // 以 index.json 里的原始 github 地址为准, 下载时按镜像 -> 直链的顺序尝试
        val downloadUrls = mirrorUrlList(
            URI(UPDATE_URL).resolve(newVersion.downloadUrl).toString()
        )
        downloadJob = scope.launch(Dispatchers.IO) {
            try {
                var lastError: Exception? = null
                for (url in downloadUrls) {
                    // 用户可能在下载过程中点了"终止下载"
                    if (downloadStatusFlow.value !is LoadStatus.Loading) break
                    try {
                        downloadApk(url, newVersion.fileSize, apkFile)
                        lastError = null
                        break
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // 镜像不通时丢弃半截文件, 换下一个地址重试
                        lastError = e
                        apkFile.delete()
                    }
                }
                val error = lastError
                if (downloadStatusFlow.value is LoadStatus.Loading) {
                    if (error == null) {
                        downloadStatusFlow.value = LoadStatus.Success(apkFile)
                    } else {
                        downloadStatusFlow.value = LoadStatus.Failure(error)
                    }
                }
            } finally {
                downloadJob = null
            }
        }
    }

    private suspend fun downloadApk(
        url: String,
        fileSize: Long,
        apkFile: File,
    ) {
        val channel = client.get(url) {
            onDownload { bytesSentTotal, _ ->
                val downloadStatus = downloadStatusFlow.value
                if (downloadStatus is LoadStatus.Loading) {
                    downloadStatusFlow.value = LoadStatus.Loading(
                        bytesSentTotal.toFloat() / fileSize
                    )
                } else if (downloadStatus is LoadStatus.Failure) {
                    // 提前终止下载
                    downloadJob?.cancel()
                }
            }
        }.bodyAsChannel()
        if (downloadStatusFlow.value is LoadStatus.Loading) {
            channel.copyAndClose(apkFile.writeChannel())
        }
    }

    @Composable
    fun UpgradeDialog() {
        newVersionFlow.collectAsStateWithLifecycle().value?.let { newVersionVal ->
            val text = remember {
                val logs = newVersionVal.versionLogs.takeWhile { v ->
                    v.code > META.versionCode
                }
                "v${META.versionName} -> v${newVersionVal.versionName}\n\n${
                    if (logs.size > 1) {
                        logs.joinToString("\n\n") { v -> "v${v.name}\n${v.desc}" }
                    } else if (logs.isNotEmpty()) {
                        logs.first().desc
                    } else {
                        ""
                    }
                }".trimEnd()
            }
            val scrollState = rememberScrollState()
            AppAlertDialog(
                title = {
                    Text(text = "新版本")
                },
                text = {
                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(scrollState)
                    )
                },
                onDismissRequest = { },
                confirmButton = {
                    TextButton(onClick = {
                        newVersionFlow.value = null
                        startDownload(newVersionVal)
                    }) {
                        Text(text = "下载更新")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { newVersionFlow.value = null }) {
                        Text(text = "取消")
                    }
                    if (!lastManual) {
                        TextButton(onClick = {
                            newVersionFlow.value = null
                            ignoreVersionListFlow.update {
                                it + newVersionVal.versionCode
                            }
                            toast("已忽略此版本")
                        }) {
                            Text(text = "忽略")
                        }
                    }
                },
            )
        }

        downloadStatusFlow.collectAsStateWithLifecycle().value?.let { downloadStatusVal ->
            when (downloadStatusVal) {
                is LoadStatus.Loading -> {
                    AppAlertDialog(
                        title = { Text(text = "下载中") },
                        text = {
                            LinearProgressIndicator(
                                progress = { downloadStatusVal.progress },
                            )
                        },
                        onDismissRequest = {},
                        confirmButton = {
                            TextButton(onClick = {
                                downloadStatusFlow.value = LoadStatus.Failure(
                                    Exception("终止下载")
                                )
                            }) {
                                Text(text = "终止下载")
                            }
                        },
                    )
                }

                is LoadStatus.Failure -> {
                    AppAlertDialog(
                        title = { Text(text = "下载失败") },
                        text = {
                            Text(text = downloadStatusVal.exception.let {
                                it.message ?: it.toString()
                            })
                        },
                        onDismissRequest = { downloadStatusFlow.value = null },
                        confirmButton = {
                            TextButton(onClick = {
                                downloadStatusFlow.value = null
                            }) {
                                Text(text = "关闭")
                            }
                        },
                    )
                }

                is LoadStatus.Success -> {
                    AppAlertDialog(
                        title = { Text(text = "下载完毕") },
                        text = {
                            Text(text = "可继续选择安装新版本")
                        },
                        onDismissRequest = {},
                        dismissButton = {
                            TextButton(onClick = {
                                downloadStatusFlow.value = null
                            }) {
                                Text(text = "关闭")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = throttle {
                                installApk(downloadStatusVal.result)
                            }) {
                                Text(text = "安装")
                            }
                        })
                }
            }
        }
    }
}


private fun installApk(file: File) {
    val uri = FileProvider.getUriForFile(
        app,
        "${app.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(uri, "application/vnd.android.package-archive")
    }
    app.tryStartActivity(intent)
}
