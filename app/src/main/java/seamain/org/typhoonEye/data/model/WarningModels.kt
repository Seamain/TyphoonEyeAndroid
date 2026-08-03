package seamain.org.typhoonEye.data.model

import kotlinx.serialization.Serializable
import seamain.org.typhoonEye.domain.model.AlertSeverity
import seamain.org.typhoonEye.domain.model.AlertSource
import seamain.org.typhoonEye.domain.model.EmergencyAlert

@Serializable
data class QWeatherAlertResponse(
    val metadata: QWeatherAlertMetadata? = null,
    val alerts: List<QWeatherAlert> = emptyList()
)

@Serializable
data class QWeatherAlertMetadata(
    val tag: String = "",
    val zeroResult: Boolean = false,
    val attributions: List<String> = emptyList()
)

@Serializable
data class QWeatherAlert(
    val id: String = "",
    val senderName: String = "",
    val issuedTime: String = "",
    val messageType: QWeatherAlertMessageType? = null,
    val eventType: QWeatherAlertEventType? = null,
    val urgency: String? = null,
    val severity: String? = null,
    val certainty: String? = null,
    val icon: String = "",
    val color: QWeatherAlertColor? = null,
    val effectiveTime: String = "",
    val onsetTime: String = "",
    val expireTime: String = "",
    val headline: String = "",
    val description: String = "",
    val criteria: String = "",
    val instruction: String = ""
)

@Serializable
data class QWeatherAlertMessageType(
    val code: String = "",
    val supersedes: List<String> = emptyList()
)

@Serializable
data class QWeatherAlertEventType(
    val name: String = "",
    val code: String = ""
)

@Serializable
data class QWeatherAlertColor(
    val code: String = "",
    val red: Int = 0,
    val green: Int = 0,
    val blue: Int = 0,
    val alpha: Double = 1.0
)

fun QWeatherAlert.toDomain(): EmergencyAlert {
    val severity = when (severity?.lowercase()) {
        "extreme" -> AlertSeverity.Extreme
        "severe" -> AlertSeverity.Severe
        "moderate" -> AlertSeverity.Moderate
        "minor" -> AlertSeverity.Minor
        else -> when (color?.code?.lowercase()) {
            "red" -> AlertSeverity.Extreme
            "orange" -> AlertSeverity.Severe
            "yellow" -> AlertSeverity.Moderate
            "blue" -> AlertSeverity.Minor
            else -> AlertSeverity.Unknown
        }
    }
    val cancel = messageType?.code.equals("cancel", ignoreCase = true)
    return EmergencyAlert(
        id = id.ifBlank { "${eventType?.code}-$issuedTime-$headline".hashCode().toString() },
        title = headline.ifBlank { "${eventType?.name.orEmpty()}预警" },
        body = description.ifBlank { criteria }.ifBlank { instruction },
        sender = senderName,
        eventName = eventType?.name.orEmpty(),
        severity = severity,
        colorCode = color?.code.orEmpty(),
        issuedTime = issuedTime,
        expireTime = expireTime,
        instruction = instruction,
        source = AlertSource.Official,
        isCancel = cancel
    )
}

/** Typhoon-related QWeather event type codes (Alert Info). */
val TYPHOON_ALERT_EVENT_CODES = setOf(
    "1001", // 台风
    "2330", // 台风警报
    "2331", // 飓风警报
    "2365", // 热带风暴关注
    "2366", // 台风关注
    "2615", // 台风提示
    "2616"  // 台风警告
)

fun QWeatherAlert.isTyphoonRelated(): Boolean {
    val code = eventType?.code.orEmpty()
    if (code in TYPHOON_ALERT_EVENT_CODES) return true
    val name = eventType?.name.orEmpty()
    return name.contains("台风") ||
        name.contains("热带风暴") ||
        name.contains("飓风") ||
        name.contains("热带气旋")
}
