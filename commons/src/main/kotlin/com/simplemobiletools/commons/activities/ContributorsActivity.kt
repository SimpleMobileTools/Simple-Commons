package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.helpers.NavigationIcon
import kotlinx.android.synthetic.main.activity_contributors.*

class ContributorsActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contributors)

        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()

        updateTextColors(contributors_holder)
        contributors_development_label.setTextColor(primaryColor)
        contributors_translation_label.setTextColor(primaryColor)

        contributors_label.apply {
            setTextColor(textColor)
            text = Html.fromHtml(getString(R.string.contributors_label))
            setLinkTextColor(primaryColor)
            movementMethod = LinkMovementMethod.getInstance()
            removeUnderlines()
        }

        contributors_development_icon.applyColorFilter(textColor)
        contributors_footer_icon.applyColorFilter(textColor)

        arrayOf(contributors_development_holder, contributors_translation_holder).forEach {
            it.background.applyColorFilter(backgroundColor.getContrastColor())
        }

        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            contributors_footer_icon.beGone()
            contributors_label.beGone()
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(contributors_toolbar, NavigationIcon.Arrow)
    }
}
