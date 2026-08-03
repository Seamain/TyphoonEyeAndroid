package seamain.org.typhoonEye.ui.navigation

/**
 * Navigation Compose routes for the single-activity app.
 */
object AppDestination {
    const val Home = "home"
    const val Settings = "settings"
    const val Detail = "detail/{typhoonId}"

    fun detail(typhoonId: String): String = "detail/$typhoonId"

    const val ArgTyphoonId = "typhoonId"
}
