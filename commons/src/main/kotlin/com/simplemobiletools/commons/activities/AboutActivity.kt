package com.simplemobiletools.commons.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.commons.compose.screens.*
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.dialogs.RateStarsDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem

class AboutActivity : ComponentActivity() {
    private val appName get() = intent.getStringExtra(APP_NAME) ?: ""

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0

    companion object {
        private const val EASTER_EGG_TIME_LIMIT = 3000L
        private const val EASTER_EGG_REQUIRED_CLICKS = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            val context = LocalContext.current
            val resources = context.resources
            AppThemeSurface {
                val showExternalLinks = remember { !resources.getBoolean(R.bool.hide_all_external_links) }
                val showGoogleRelations = remember { !resources.getBoolean(R.bool.hide_google_relations) }
                AboutScreen(
                    goBack = ::finish,
                    helpUsSection = {
                        val showHelpUsSection =
                            remember { showGoogleRelations || !showExternalLinks }
                        HelpUsSection(
                            onRateUsClick = ::onRateUsClick,
                            onInviteClick = ::onInviteClick,
                            onContributorsClick = ::onContributorsClick,
                            showDonate = resources.getBoolean(R.bool.show_donate_in_about) && showExternalLinks,
                            onDonateClick = ::onDonateClick,
                            showInvite = showHelpUsSection,
                            showRateUs = showHelpUsSection
                        )
                    },
                    aboutSection = {
                        val setupFAQ = remember { !(intent.getSerializableExtra(APP_FAQ) as? ArrayList<FAQItem>).isNullOrEmpty() }
                        if (!showExternalLinks || setupFAQ) {
                            AboutSection(setupFAQ = setupFAQ, onFAQClick = ::launchFAQActivity, onEmailClick = ::onEmailClick)
                        }
                    },
                    socialSection = {
                        if (showExternalLinks) {
                            SocialSection(
                                onFacebookClick = ::onFacebookClick,
                                onGithubClick = ::onGithubClick,
                                onRedditClick = ::onRedditClick,
                                onTelegramClick = ::onTelegramClick
                            )
                        }
                    }
                ) {
                    val showWebsite = remember { resources.getBoolean(R.bool.show_donate_in_about) && !showExternalLinks }
                    var version = intent.getStringExtra(APP_VERSION_NAME) ?: ""
                    if (baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
                        version += " ${getString(R.string.pro)}"
                    }
                    val fullVersion = remember { String.format(getString(R.string.version_placeholder, version)) }

                    OtherSection(
                        showMoreApps = showGoogleRelations,
                        onMoreAppsClick = ::launchMoreAppsFromUsIntent,
                        showWebsite = showWebsite,
                        onWebsiteClick = ::onWebsiteClick,
                        showPrivacyPolicy = showExternalLinks,
                        onPrivacyPolicyClick = ::onPrivacyPolicyClick,
                        onLicenseClick = ::onLicenseClick,
                        version = fullVersion,
                        onVersionClick = ::onVersionClick
                    )
                }
            }
        }
    }

    private fun onEmailClick() {
        val msg = "${getString(R.string.before_asking_question_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
        if (intent.getBooleanExtra(SHOW_FAQ_BEFORE_MAIL, false) && !baseConfig.wasBeforeAskingShown) {
            baseConfig.wasBeforeAskingShown = true
            ConfirmationAdvancedDialog(this@AboutActivity, msg, 0, R.string.read_faq, R.string.skip) { success ->
                if (success) {
                    launchFAQActivity()
                } else {
                    launchEmailIntent()
                }
            }
        } else {
            launchEmailIntent()
        }
    }

    private fun launchFAQActivity() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(APP_ICON_IDS, intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>())
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    private fun launchEmailIntent() {
        val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "\n"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"

        val address = if (packageName.startsWith("com.simplemobiletools")) {
            getString(R.string.my_email)
        } else {
            getString(R.string.my_fake_email)
        }

        val selectorIntent = Intent(ACTION_SENDTO)
            .setData("mailto:$address".toUri())
        val emailIntent = Intent(ACTION_SEND).apply {
            putExtra(EXTRA_EMAIL, arrayOf(address))
            putExtra(EXTRA_SUBJECT, appName)
            putExtra(EXTRA_TEXT, body)
            selector = selectorIntent
        }

        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            val chooser = createChooser(emailIntent, getString(R.string.send_email))
            try {
                startActivity(chooser)
            } catch (e: Exception) {
                toast(R.string.no_email_client_found)
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }


    private fun onRateUsClick() {
        if (baseConfig.wasBeforeRateShown) {
            launchRateUsPrompt()
        } else {
            baseConfig.wasBeforeRateShown = true
            val msg = "${getString(R.string.before_rate_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
            ConfirmationAdvancedDialog(this@AboutActivity, msg, 0, R.string.read_faq, R.string.skip) { success ->
                if (success) {
                    launchFAQActivity()
                } else {
                    launchRateUsPrompt()
                }
            }
        }
    }

    private fun launchRateUsPrompt() {
        if (baseConfig.wasAppRated) {
            redirectToRateUs()
        } else {
            RateStarsDialog(this@AboutActivity)
        }
    }

    private fun onInviteClick() {
        val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
        Intent().apply {
            action = ACTION_SEND
            putExtra(EXTRA_SUBJECT, appName)
            putExtra(EXTRA_TEXT, text)
            type = "text/plain"
            startActivity(createChooser(this, getString(R.string.invite_via)))
        }
    }

    private fun onContributorsClick() {
        val intent = Intent(applicationContext, ContributorsActivity::class.java)
        startActivity(intent)
    }


    private fun onDonateClick() {
        launchViewIntent(getString(R.string.donate_url))
    }

    private fun onFacebookClick() {
        var link = "https://www.facebook.com/simplemobiletools"
        try {
            packageManager.getPackageInfo("com.facebook.katana", 0)
            link = "fb://page/150270895341774"
        } catch (ignored: Exception) {
        }

        launchViewIntent(link)
    }

    private fun onGithubClick() {
        launchViewIntent("https://github.com/SimpleMobileTools")
    }

    private fun onRedditClick() {
        launchViewIntent("https://www.reddit.com/r/SimpleMobileTools")
    }


    private fun onTelegramClick() {
        launchViewIntent("https://t.me/SimpleMobileTools")
    }


    private fun onWebsiteClick() {
        launchViewIntent("https://simplemobiletools.com/")
    }

    private fun onPrivacyPolicyClick() {
        val appId = baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro").removePrefix("com.simplemobiletools.")
        val url = "https://simplemobiletools.com/privacy/$appId.txt"
        launchViewIntent(url)
    }

    private fun onLicenseClick() {
        Intent(applicationContext, LicenseActivity::class.java).apply {
            putExtra(APP_ICON_IDS, intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>())
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_LICENSES, intent.getLongExtra(APP_LICENSES, 0))
            startActivity(this)
        }
    }

    private fun onVersionClick() {
        if (firstVersionClickTS == 0L) {
            firstVersionClickTS = System.currentTimeMillis()
            Handler(Looper.getMainLooper()).postDelayed({
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }, EASTER_EGG_TIME_LIMIT)
        }

        clicksSinceFirstClick++
        if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS) {
            toast(R.string.hello)
            firstVersionClickTS = 0L
            clicksSinceFirstClick = 0
        }
    }
}
