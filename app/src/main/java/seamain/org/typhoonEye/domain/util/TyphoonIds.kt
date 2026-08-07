package seamain.org.typhoonEye.domain.util

/**
 * Juhe uses numeric tfids (e.g. `202418`); QWeather uses `NP_2418`.
 * Treat those as the same storm when matching list ↔ detail ↔ nav args.
 */
fun normalizeTyphoonId(id: String): String {
    val t = id.trim().uppercase()
    if (t.isEmpty()) return ""
    val stripped = when {
        t.startsWith("NP_") -> t.removePrefix("NP_")
        else -> t
    }
    // Prefer last 4 digits when the body is numeric / mostly numeric.
    val digits = stripped.filter { it.isDigit() }
    return when {
        digits.length >= 4 -> digits.takeLast(4)
        digits.isNotEmpty() -> digits
        else -> stripped
    }
}

fun typhoonIdsMatch(a: String?, b: String?): Boolean {
    if (a == null || b == null) return false
    if (a == b) return true
    val na = normalizeTyphoonId(a)
    val nb = normalizeTyphoonId(b)
    return na.isNotEmpty() && na == nb
}
