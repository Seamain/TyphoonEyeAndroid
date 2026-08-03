package seamain.org.typhoonEye.data.preferences

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * In-app languages. [nativeLabel] stays in the language's own script
 * so users can always recognize the option in Settings.
 */
enum class AppLanguage(val tag: String, val nativeLabel: String) {
    ZhHans("zh-CN", "中文简体"),
    ZhHant("zh-TW", "中文繁體"),
    Yue("yue", "粵語"),
    English("en", "English");

    companion object {
        fun fromStorage(value: String?): AppLanguage =
            entries.find { it.name == value || it.tag.equals(value, ignoreCase = true) }
                ?: fromLocaleTags(value)
                ?: ZhHans

        fun fromLocaleTags(tags: String?): AppLanguage? {
            if (tags.isNullOrBlank()) return null
            val primary = tags.split(",").firstOrNull()?.trim().orEmpty()
            if (primary.isBlank()) return null
            val locale = Locale.forLanguageTag(primary.replace('_', '-'))
            return when {
                locale.language.equals("yue", ignoreCase = true) -> Yue
                locale.language.equals("en", ignoreCase = true) -> English
                locale.language.equals("zh", ignoreCase = true) &&
                    (
                        locale.country.equals("TW", ignoreCase = true) ||
                            locale.script.equals("Hant", ignoreCase = true)
                        ) -> ZhHant
                locale.language.equals("zh", ignoreCase = true) &&
                    locale.country.equals("HK", ignoreCase = true) -> Yue
                locale.language.equals("zh", ignoreCase = true) -> ZhHans
                else -> entries.find { primary.startsWith(it.tag, ignoreCase = true) }
            }
        }

        fun current(): AppLanguage {
            val appLocales = AppCompatDelegate.getApplicationLocales()
            if (!appLocales.isEmpty) {
                fromLocaleTags(appLocales.toLanguageTags())?.let { return it }
            }
            return fromSystemDefault()
        }

        fun fromSystemDefault(): AppLanguage {
            val sys = LocaleListCompat.getAdjustedDefault().get(0) ?: Locale.getDefault()
            return fromLocaleTags(sys.toLanguageTag()) ?: ZhHans
        }

        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language.tag)
            )
        }
    }
}
