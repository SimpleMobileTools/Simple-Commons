package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.removeUnderlines
import com.simplemobiletools.commons.extensions.updateTextColors
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

        contributors_label.text = Html.fromHtml(getString(R.string.contributors_label))
        contributors_label.removeUnderlines()
        contributors_label.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
