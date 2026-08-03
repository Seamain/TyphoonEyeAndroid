package seamain.org.typhoonEye.domain.model

/**
 * App-domain weather / typhoon emergency alert for notifications & UI.
 */
data class EmergencyAlert(
    val id: String,
    val title: String,
    val body: String,
    val sender: String = "",
    val eventName: String = "",
    val severity: AlertSeverity = AlertSeverity.Unknown,
    val colorCode: String = "",
    val issuedTime: String = "",
    val expireTime: String = "",
    val instruction: String = "",
    val source: AlertSource = AlertSource.Official,
    val relatedTyphoonId: String? = null,
    val isCancel: Boolean = false
)

enum class AlertSeverity(val rank: Int, val label: String) {
    Extreme(4, "特别严重"),
    Severe(3, "严重"),
    Moderate(2, "较重"),
    Minor(1, "一般"),
    Unknown(0, "未知")
}

enum class AlertSource { Official, Intensity }
