package li.gkd.app.ui.component

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import li.gkd.app.MainActivity
import li.gkd.app.ui.share.LocalMainViewModel
import li.gkd.app.util.throttle


@Composable
fun TermsAcceptDialog() {
    val mainVm = LocalMainViewModel.current
    val context = LocalActivity.current as MainActivity
    val modifier = Modifier.fillMaxWidth()
    val stepDataList = remember {
        arrayOf(
            "使用声明" to @Composable {
                Text(
                    modifier = modifier,
                    text = "感谢使用 gkd_pure！\n\n" +
                            "本应用完全在本地运行: 不收集、不上传您的任何个人信息, 不内置任何统计或上报组件。\n\n" +
                            "应用基于开源项目 GKD 二次开发, 请合理使用订阅规则自动化功能, 遵守设备与目标应用的使用条款",
                )
            },
            "关于无障碍" to @Composable {
                Text(
                    modifier = modifier,
                    text = "gkd_pure 请求使用系统「无障碍 API」获取屏幕信息, 以此基于用户自定义订阅规则执行自动化操作",
                )
            }
        )
    }
    val step by mainVm.termsStepFlow.collectAsStateWithLifecycle()

    AppAlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = stepDataList[step].first)
        },
        text = stepDataList[step].second,
        confirmButton = {
            TextButton(onClick = throttle {
                mainVm.acceptTermsStep(stepDataList.lastIndex)
            }) {
                Text(text = "同意")
            }
        },
        dismissButton = {
            TextButton(onClick = throttle {
                context.finish()
            }) {
                Text(text = "不同意")
            }
        }
    )
}
