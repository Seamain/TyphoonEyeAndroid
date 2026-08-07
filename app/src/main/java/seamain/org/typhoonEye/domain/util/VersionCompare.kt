package seamain.org.typhoonEye.domain.util

/**
 * Lightweight semver-ish compare for app tags / versionName.
 * Supports `1.0`, `v1.2.3`, `1.2.3-beta` (pre-release suffix ignored for ordering core).
 *
 * @return negative if [a] < [b], 0 if equal, positive if [a] > [b]
 */
fun compareVersionLabels(a: String, b: String): Int {
    val pa = parseVersionParts(a)
    val pb = parseVersionParts(b)
    val n = maxOf(pa.size, pb.size)
    for (i in 0 until n) {
        val x = pa.getOrElse(i) { 0 }
        val y = pb.getOrElse(i) { 0 }
        if (x != y) return x.compareTo(y)
    }
    return 0
}

fun isNewerVersion(candidate: String, current: String): Boolean =
    compareVersionLabels(candidate, current) > 0

private fun parseVersionParts(raw: String): List<Int> {
    val cleaned = raw.trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore('-')
        .substringBefore('+')
        .trim()
    if (cleaned.isEmpty()) return listOf(0)
    return cleaned.split('.', '_', ' ')
        .mapNotNull { token ->
            token.filter { it.isDigit() }.takeIf { it.isNotEmpty() }?.toIntOrNull()
        }
        .ifEmpty { listOf(0) }
}
