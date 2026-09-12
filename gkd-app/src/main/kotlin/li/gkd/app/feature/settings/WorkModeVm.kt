package li.gkd.app.feature.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import li.gkd.app.permission.PermissionStates
import li.gkd.app.ui.share.BaseViewModel

class WorkModeVm : BaseViewModel() {
    /**
     * 刷新一次全部权限状态。
     *
     * 这里原来是「while(isActive) { refreshAll(); delay(1s) }」的每秒轮询 ——
     * PermissionStates.refreshAll() 会挨个执行 check()，其中包含
     * Settings.canDrawOverlays / AppOpsManager.checkOpNoThrow /
     * PowerManager.isIgnoringBatteryOptimizations / XXPermissions.isGrantedPermission
     * 等 binder 调用，以及跨越进程的 Privilege.pingServer() IPC。
     * 也就是说只要「工作模式」页开着，就每秒做十几次 binder + 一次 IPC，纯属空转耗电。
     *
     * 权限状态只可能在用户跳到系统设置里改动后变化，而那一刻本页面必然处于
     * onPause 状态，回来时必然走 onResume —— 所以改为由页面在 ON_RESUME 时调用本方法刷新一次即可。
     */
    fun refreshPermissions() {
        scope.launch(Dispatchers.IO) {
            PermissionStates.refreshAll()
        }
    }
}
