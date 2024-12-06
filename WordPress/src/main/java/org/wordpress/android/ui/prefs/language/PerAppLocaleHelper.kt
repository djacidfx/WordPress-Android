package org.wordpress.android.ui.prefs.language

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.wordpress.android.fluxc.utils.AppLogWrapper
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.util.AppLog
import org.wordpress.android.util.LocaleManager
import java.util.Locale
import javax.inject.Inject

/**
 * Helper class to manage per-app language preferences
 * https://developer.android.com/guide/topics/resources/app-languages
 */
class PerAppLocaleHelper @Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper,
    private val appLogWrapper: AppLogWrapper,
) {
    private fun getCurrentLocale(): Locale {
        return if (isApplicationLocaleEmpty()) {
            Locale.getDefault()
        } else {
            getApplicationLocaleList()[0] ?: Locale.getDefault()
        }
    }

    fun getCurrentLocaleDisplayName(): String = getCurrentLocale().displayName

    /**
     * Important: this should only be called after Activity.onCreate()
     * https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate#getApplicationLocales()
     */
    private fun getApplicationLocaleList() = AppCompatDelegate.getApplicationLocales()

    private fun isApplicationLocaleEmpty(): Boolean {
        val locales = getApplicationLocaleList()
        return (locales.isEmpty || locales == LocaleListCompat.getEmptyLocaleList())
    }

    /*
     * Useful during testing to clear the system stored app locale
     */
    @Suppress("unused")
    fun resetApplicationLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    fun setCurrentLocaleByLanguageCode(languageCode: String) {
        // Set the locale for the pref key so LocaleManager.getLanguage() returns the same value - this can be removed
        // once we switch entirely to per-app locale
        val languagePrefKey = LocaleManager.getLocalePrefKeyString()
        appPrefsWrapper.prefs().edit().putString(languagePrefKey, languageCode).apply()
        // We shouldn't have to replace "_" with "-" but this is in order to work with our existing language picker
        val appLocale = LocaleListCompat.forLanguageTags(languageCode.replace("_", "-"))
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Previously the app locale was stored in SharedPreferences, so here we migrate to AndroidX per-app language prefs
     */
    fun performMigrationIfNecessary() {
        if (isPerAppLanguagePrefsEnabled() && isApplicationLocaleEmpty()) {
            val languagePrefKey = LocaleManager.getLocalePrefKeyString()
            val previousLanguage = appPrefsWrapper.prefs().getString(languagePrefKey, "")
            if (previousLanguage?.isNotEmpty() == true) {
                appLogWrapper.d(
                    AppLog.T.SETTINGS,
                    "LocaleHelper: performing migration to AndroidX per-app language prefs"
                )
                setCurrentLocaleByLanguageCode(previousLanguage)
                appPrefsWrapper.prefs().edit().remove(languagePrefKey).apply()
            } else {
                appLogWrapper.d(
                    AppLog.T.SETTINGS,
                    "LocaleHelper: setting default locale"
                )
                setCurrentLocaleByLanguageCode(Locale.getDefault().language)
            }
        }
    }

    fun isPerAppLanguagePrefsEnabled(): Boolean {
        return appPrefsWrapper.getManualFeatureConfig(EXPERIMENTAL_PER_APP_LANGUAGE_PREF_KEY)
    }

    /**
     * Open the app settings screen so the user can change the app language.
     * Note that the per-app language setting is only available in API 33+
     * and it's up to the caller to check the version
     */
    fun openAppLanguageSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent().also { intent ->
                intent.setAction(Settings.ACTION_APP_LOCALE_SETTINGS)
                intent.setData(Uri.parse("package:" + context.packageName))
                context.startActivity(intent)
            }
        } else {
            throw UnsupportedOperationException(
                "Per-app language settings are not available in this version of Android"
            )
        }
    }

    companion object {
        const val EXPERIMENTAL_PER_APP_LANGUAGE_PREF_KEY = "experimental_per_app_language_prefs"
    }
}
