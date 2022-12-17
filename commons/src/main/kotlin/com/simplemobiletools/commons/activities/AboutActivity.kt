package com.simplemobiletools.commons.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import androidx.core.net.toUri
import androidx.core.view.isEmpty
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.dialogs.RateStarsDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.item_about.view.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var primaryColor = 0
    private var textColor = 0
    private var backgroundColor = 0
    private var inflater: LayoutInflater? = null

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0
    private val EASTER_EGG_TIME_LIMIT = 3000L
    private val EASTER_EGG_REQUIRED_CLICKS = 7

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        primaryColor = getProperPrimaryColor()
        textColor = getProperTextColor()
        backgroundColor = getProperBackgroundColor()
        inflater = LayoutInflater.from(this)

        updateMaterialActivityViews(about_coordinator, about_holder)
        setupMaterialScrollListener(about_nested_scrollview, about_toolbar)

        appName = intent.getStringExtra(APP_NAME) ?: ""

        arrayOf(about_support, about_help_us, about_social, about_other).forEach {
            it.setTextColor(primaryColor)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_nested_scrollview)
        setupToolbar(about_toolbar, NavigationIcon.Arrow)
        support_layout.removeAllViews()

        setupFAQ()
        setupEmail()
        setupRateUs()
        setupInvite()
        setupContributors()
        setupDonate()
        setupFacebook()
        setupGitHub()
        setupReddit()
        setupTelegram()
        setupGetSimplePhone()
        setupMoreApps()
        setupWebsite()
        setupPrivacyPolicy()
        setupLicense()
        setupVersion()
    }

    private fun setupFAQ() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        if (faqItems.isNotEmpty()) {
            inflater?.inflate(R.layout.item_about, null)?.apply {
                about_item_icon.setImageDrawable(resources.getColoredDrawableWithColor(R.drawable.ic_question_mark_vector, textColor))
                about_item_label.setText(R.string.frequently_asked_questions)
                about_item_label.setTextColor(textColor)
                support_layout.addView(this)

                setOnClickListener {
                    launchFAQActivity()
                }
            }
        }
    }

    private fun launchFAQActivity() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    private fun setupEmail() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            if (support_layout.isEmpty()) {
                about_support.beGone()
                support_divider.beGone()
            }

            return
        }

        inflater?.inflate(R.layout.item_about, null)?.apply {
            about_item_icon.setImageDrawable(resources.getColoredDrawableWithColor(R.drawable.ic_mail_vector, textColor))
            about_item_label.setText(R.string.my_email)
            about_item_label.setTextColor(textColor)
            support_layout.addView(this)

            setOnClickListener {
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
        }
    }

    private fun launchEmailIntent() {
        val appVersion = String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "\n"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"

        val address = getString(R.string.my_email)
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
            toast(R.string.no_email_client_found)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    private fun setupRateUs() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_rate_us_holder.beGone()
        }

        about_rate_us_holder.setOnClickListener {
            if (baseConfig.wasBeforeRateShown) {
                if (baseConfig.wasAppRated) {
                    redirectToRateUs()
                } else {
                    RateStarsDialog(this)
                }
            } else {
                baseConfig.wasBeforeRateShown = true
                val msg = "${getString(R.string.before_rate_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
                ConfirmationAdvancedDialog(this, msg, 0, R.string.read_faq, R.string.skip) { success ->
                    if (success) {
                        //about_faq_holder.performClick()
                    } else {
                        about_rate_us_holder.performClick()
                    }
                }
            }
        }
    }

    private fun setupInvite() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_invite_holder.beGone()
        }

        about_invite_holder.setOnClickListener {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = ACTION_SEND
                putExtra(EXTRA_SUBJECT, appName)
                putExtra(EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(createChooser(this, getString(R.string.invite_via)))
            }
        }
    }

    private fun setupContributors() {
        about_contributors_holder.setOnClickListener {
            val intent = Intent(applicationContext, ContributorsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDonate() {
        if (resources.getBoolean(R.bool.show_donate_in_about) && !resources.getBoolean(R.bool.hide_all_external_links)) {
            about_donate_holder.beVisible()
            about_donate_holder.setOnClickListener {
                launchViewIntent("https://simplemobiletools.com/donate")
            }
        } else {
            about_donate_holder.beGone()
        }
    }

    private fun setupFacebook() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_social.beGone()
            about_social_holder.beGone()
        }

        about_facebook_holder.setOnClickListener {
            var link = "https://www.facebook.com/simplemobiletools"
            try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                link = "fb://page/150270895341774"
            } catch (ignored: Exception) {
            }

            launchViewIntent(link)
        }
    }

    private fun setupGitHub() {
        about_github_icon.applyColorFilter(getProperBackgroundColor().getContrastColor())
        about_github_holder.setOnClickListener {
            launchViewIntent("https://github.com/SimpleMobileTools")
        }
    }

    private fun setupReddit() {
        about_reddit_holder.setOnClickListener {
            launchViewIntent("https://www.reddit.com/r/SimpleMobileTools")
        }
    }

    private fun setupTelegram() {
        about_telegram_holder.setOnClickListener {
            launchViewIntent("https://t.me/SimpleMobileTools")
        }
    }

    private fun setupGetSimplePhone() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_get_simple_phone_holder.beGone()
        }

        about_get_simple_phone_holder.setOnClickListener {
            launchViewIntent("https://simplemobiletools.com/phone")
        }
    }

    private fun setupMoreApps() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_more_apps_holder.beGone()
        }

        about_more_apps_holder.setOnClickListener {
            launchMoreAppsFromUsIntent()
        }
    }

    private fun setupWebsite() {
        if (resources.getBoolean(R.bool.show_donate_in_about) && !resources.getBoolean(R.bool.hide_all_external_links)) {
            about_website_holder.beVisible()
            about_website_holder.setOnClickListener {
                launchViewIntent("https://simplemobiletools.com/")
            }
        } else {
            about_website_holder.beGone()
        }
    }

    private fun setupPrivacyPolicy() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_privacy_policy_holder.beGone()
        }

        about_privacy_policy_holder.setOnClickListener {
            val appId = baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro").removePrefix("com.simplemobiletools.")
            val url = "https://simplemobiletools.com/privacy/$appId.txt"
            launchViewIntent(url)
        }
    }

    private fun setupLicense() {
        about_licenses_holder.setOnClickListener {
            Intent(applicationContext, LicenseActivity::class.java).apply {
                putExtra(APP_ICON_IDS, getAppIconIDs())
                putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
                putExtra(APP_LICENSES, intent.getLongExtra(APP_LICENSES, 0))
                startActivity(this)
            }
        }
    }

    private fun setupVersion() {
        var version = intent.getStringExtra(APP_VERSION_NAME) ?: ""
        if (baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
            version += " ${getString(R.string.pro)}"
        }

        val fullVersion = String.format(getString(R.string.version_placeholder, version))
        about_version.text = fullVersion
        about_version_holder.setOnClickListener {
            if (firstVersionClickTS == 0L) {
                firstVersionClickTS = System.currentTimeMillis()
                Handler().postDelayed({
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
}
