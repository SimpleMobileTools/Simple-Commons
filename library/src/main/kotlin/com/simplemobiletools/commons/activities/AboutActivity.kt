package com.simplemobiletools.commons.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import com.simplemobiletools.commons.BuildConfig
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.APP_NAME
import kotlinx.android.synthetic.main.activity_about.*
import java.util.*

class AboutActivity : SimpleActivity() {
    var appName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appName = intent.getStringExtra(APP_NAME) ?: ""

        setupWebsite()
        setupEmail()
        setupMoreApps()
        setupRateUs()
        setupInvite()
        setupLicense()
        setupDonate()
        setupFacebook()
        setupGPlus()
        setupCopyright()
    }

    private fun setupWebsite() {
        val websiteText = String.format(getString(R.string.two_string_placeholder), getString(R.string.website_label), getString(R.string.website))
        about_website.text = websiteText
    }

    private fun setupEmail() {
        val label = getString(R.string.email_label)
        val email = getString(R.string.email)

        val href = "$label<br><a href=\"mailto:$email?subject=$appName\">$email</a>"
        about_email.text = Html.fromHtml(href)
        about_email.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupMoreApps() {
        about_more_apps.setOnClickListener {
            launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
        }
    }

    private fun setupCopyright() {
        val versionName = BuildConfig.VERSION_NAME
        val year = Calendar.getInstance().get(Calendar.YEAR)
        about_copyright.text = String.format(getString(R.string.copyright), versionName, year)
    }

    private fun setupRateUs() {
        if (config.isFirstRun) {
            about_rate_us.visibility = View.GONE
        } else {
            about_rate_us.setOnClickListener {
                try {
                    launchViewIntent("market://details?id=$packageName")
                } catch (ignored: ActivityNotFoundException) {
                    launchViewIntent(getStoreUrl())
                }
            }
        }
    }

    fun setupInvite() {
        about_invite.setOnClickListener {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, appName)
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(Intent.createChooser(this, getString(R.string.invite_via)))
            }
        }
    }

    fun setupLicense() {
        about_license.setOnClickListener {
            /*val intent = Intent(applicationContext, LicenseActivity::class.java)
            startActivity(intent)*/
        }
    }

    fun setupDonate() {
        about_donate.setOnClickListener {
            launchViewIntent("http://simplemobiletools.github.io/donate")
        }
    }

    fun setupFacebook() {
        about_facebook.setOnClickListener {
            var link = "https://www.facebook.com/simplemobiletools"
            try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                link = "fb://page/150270895341774"
            } catch (ignored: Exception) {
            }

            launchViewIntent(link)
        }
    }

    fun setupGPlus() {
        about_gplus.setOnClickListener {
            launchViewIntent("https://plus.google.com/communities/104880861558693868382")
        }
    }

    private fun getStoreUrl() = "https://play.google.com/store/apps/details?id=$packageName"
}
