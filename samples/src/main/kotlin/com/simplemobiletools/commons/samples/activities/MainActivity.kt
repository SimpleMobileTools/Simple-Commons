package com.simplemobiletools.commons.samples.activities

import android.os.Bundle
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.SimpleBottomSheetChooserDialog
import com.simplemobiletools.commons.extensions.appLaunched
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.models.SimpleListItem
import com.simplemobiletools.commons.samples.BuildConfig
import com.simplemobiletools.commons.samples.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseSimpleActivity() {
    override fun getAppLauncherName() = getString(R.string.smtco_app_name)

    override fun getAppIconIDs(): ArrayList<Int> {
        val ids = ArrayList<Int>()
        ids.add(R.mipmap.commons_launcher)
        return ids
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        main_color_customization.setOnClickListener {
            startCustomizationActivity()
        }

        bottom_sheet_chooser.setOnClickListener {
            launchBottomSheetDemo()
        }

        //startCustomizationActivity()
        //startAboutActivity(R.string.smtco_app_name, 3, "0.2", arrayListOf(FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons)), false)

        /*val letters = arrayListOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q")
        StringsAdapter(this, letters, media_grid, media_refresh_layout) {
        }.apply {
            media_grid.adapter = this
        }

        media_refresh_layout.setOnRefreshListener {
            Handler().postDelayed({
                media_refresh_layout.isRefreshing = false
            }, 1000L)
        }*/
    }

    private fun launchBottomSheetDemo() {
        SimpleBottomSheetChooserDialog.createChooser(
            fragmentManager = supportFragmentManager,
            title = R.string.please_select_destination,
            subtitle = null,
            data = arrayOf(
                SimpleListItem(1, R.drawable.ic_settings_cog_vector, R.string.choose_video),
                SimpleListItem(2, R.drawable.ic_settings_cog_vector, R.string.choose_photo),
                SimpleListItem(4, R.drawable.ic_settings_cog_vector, R.string.choose_contact)
            )
        ) {
            toast("Clicked ${it.id}")
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(main_toolbar)
    }
}
