package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import kotlinx.android.synthetic.main.activity_contributors.*
import java.util.*

class ContributorsActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contributors)

        updateTextColors(contributors_holder)
        contributors_development_label.setTextColor(getAdjustedPrimaryColor())
        contributors_translation_label.setTextColor(getAdjustedPrimaryColor())

        contributors_label.apply {
            setTextColor(baseConfig.textColor)
            text = Html.fromHtml(getString(R.string.contributors_label))
            setLinkTextColor(getAdjustedPrimaryColor())
            movementMethod = LinkMovementMethod.getInstance()
            removeUnderlines()
        }

        contributors_development_icon.applyColorFilter(baseConfig.textColor)
        contributors_footer_icon.applyColorFilter(baseConfig.textColor)

        arrayOf(contributors_development_holder, contributors_translation_holder).forEach {
            it.background.applyColorFilter(baseConfig.backgroundColor.getContrastColor())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
