package org.wordpress.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import org.wordpress.android.fluxc.utils.AppLogWrapper
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import java.util.Locale
import javax.inject.Inject

/**
 * Helper class to manage AndroidX per-app language preferences
 * https://developer.android.com/guide/topics/resources/app-languages
 */
class PerAppLocaleManager @Inject constructor(
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

    /**
     * We want to make sure the language pref for the in-app locale (old implementation) is set
     * to the same locale as the AndroidX per-app locale. This way LocaleManager.getLanguage -
     * which is used throughout the app - returns the correct language code. We can remove
     * this once the per-app language pref is no longer experimental.
     */
    private fun checkAndUpdatedOldLanguagePrefKey() {
        val prefKey = LocaleManager.getLocalePrefKeyString()
        val inAppLanguage = appPrefsWrapper.prefs().getString(prefKey, "")
        val perAppLanguage = getCurrentLocale().language
        if (perAppLanguage.isNotEmpty() && inAppLanguage.equals(perAppLanguage).not()) {
            appPrefsWrapper.prefs().edit().putString(prefKey, perAppLanguage).apply()
            appLogWrapper.d(
                AppLog.T.SETTINGS,
                "PerAppLocaleManager: changed inAppLanguage from $inAppLanguage to $perAppLanguage"
            )
        }
    }

    fun resetApplicationLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    fun setCurrentLocaleByLanguageCode(languageCode: String) {
        // We shouldn't have to replace "_" with "-" but this is in order to work with our existing language picker
        // on pre-Android 13 devices
        val appLocale = LocaleListCompat.forLanguageTags(languageCode.replace("_", "-"))
        AppCompatDelegate.setApplicationLocales(appLocale)
        checkAndUpdatedOldLanguagePrefKey()
    }

    /**
     * Previously the app locale was stored in SharedPreferences, so here we migrate to AndroidX per-app language prefs
     */
    fun performMigrationIfNecessary() {
        if (isPerAppLanguagePrefsEnabled()) {
            if (isApplicationLocaleEmpty()) {
                val prefKey = LocaleManager.getLocalePrefKeyString()
                val previousLanguage = appPrefsWrapper.prefs().getString(prefKey, "")
                if (previousLanguage?.isNotEmpty() == true) {
                    appLogWrapper.d(
                        AppLog.T.SETTINGS,
                        "PerAppLocaleManager: performing migration to AndroidX per-app language prefs"
                    )
                    setCurrentLocaleByLanguageCode(previousLanguage)
                } else {
                    appLogWrapper.d(
                        AppLog.T.SETTINGS,
                        "PerAppLocaleManager: setting default locale"
                    )
                    setCurrentLocaleByLanguageCode(Locale.getDefault().language)
                }
            } else {
                checkAndUpdatedOldLanguagePrefKey()
            }
        }
    }

    fun isPerAppLanguagePrefsEnabled(): Boolean {
        return appPrefsWrapper.getManualFeatureConfig(EXPERIMENTAL_PER_APP_LANGUAGE_PREF_KEY)
    }

    /**
     * Open the app settings dialog so the user can change the app language.
     * Note that the per-app language setting is only available in API 33+
     * and it's up to the caller to check the version.
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
                "PerAppLocaleManager: Per-app language settings are not available in this version of Android"
            )
        }
    }

    companion object {
        const val EXPERIMENTAL_PER_APP_LANGUAGE_PREF_KEY = "experimental_per_app_language_prefs"
    }
}
