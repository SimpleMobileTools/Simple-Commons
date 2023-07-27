package com.simplemobiletools.commons.samples.activities

import android.os.Bundle
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.BottomSheetChooserDialog
import com.simplemobiletools.commons.extensions.appLaunched
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.viewBinding
import com.simplemobiletools.commons.models.SimpleListItem
import com.simplemobiletools.commons.samples.BuildConfig
import com.simplemobiletools.commons.samples.R
import com.simplemobiletools.commons.samples.databinding.ActivityMainBinding

class MainActivity : BaseSimpleActivity() {
    override fun getAppLauncherName() = getString(R.string.smtco_app_name)

    override fun getAppIconIDs(): ArrayList<Int> {
        val ids = ArrayList<Int>()
        ids.add(R.mipmap.commons_launcher)
        return ids
    }

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        appLaunched(BuildConfig.APPLICATION_ID)

        updateMaterialActivityViews(binding.mainCoordinator, binding.mainHolder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.mainNestedScrollview, binding.mainToolbar)

        binding.mainColorCustomization.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun launchBottomSheetDemo() {
        BottomSheetChooserDialog.createChooser(
            fragmentManager = supportFragmentManager,
            title = com.simplemobiletools.commons.R.string.please_select_destination,
            items = arrayOf(
                SimpleListItem(1, com.simplemobiletools.commons.R.string.record_video, com.simplemobiletools.commons.R.drawable.ic_camera_vector),
                SimpleListItem(
                    2,
                    com.simplemobiletools.commons.R.string.record_audio,
                    com.simplemobiletools.commons.R.drawable.ic_microphone_vector,
                    selected = true
                ),
                SimpleListItem(4, com.simplemobiletools.commons.R.string.choose_contact, com.simplemobiletools.commons.R.drawable.ic_add_person_vector)
            )
        ) {
            toast("Clicked ${it.id}")
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.mainToolbar)
    }
}
