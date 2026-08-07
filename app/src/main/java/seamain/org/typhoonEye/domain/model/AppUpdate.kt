package seamain.org.typhoonEye.domain.model

/**
 * Available app update discovered from GitHub Releases.
 */
data class AppUpdateInfo(
    val versionName: String,
    val tagName: String,
    val releaseNotes: String,
    val htmlUrl: String,
    val apkUrl: String,
    val apkName: String,
    val apkSizeBytes: Long
)

sealed class AppUpdateState {
    data object Idle : AppUpdateState()
    data object Checking : AppUpdateState()
    data object UpToDate : AppUpdateState()
    data class Available(val info: AppUpdateInfo) : AppUpdateState()
    data class Downloading(val info: AppUpdateInfo, val progress: Int) : AppUpdateState()
    data class ReadyToInstall(val info: AppUpdateInfo, val apkPath: String) : AppUpdateState()
    data class Error(val message: String) : AppUpdateState()
}
