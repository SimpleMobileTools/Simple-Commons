package com.simplemobiletools.commons.dialogs

import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.PasswordTypesAdapter
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.commons.helpers.PROTECTION_PATTERN
import com.simplemobiletools.commons.helpers.PROTECTION_PIN
import com.simplemobiletools.commons.helpers.SHOW_ALL_TABS
import com.simplemobiletools.commons.interfaces.HashListener
import com.simplemobiletools.commons.views.MyDialogViewPager
import kotlinx.android.synthetic.main.dialog_security.view.*

class SecurityDialog(val activity: BaseSimpleActivity, val requiredHash: String, val showTabIndex: Int, val callback: (hash: String, type: Int) -> Unit) : HashListener {
    var dialog: AlertDialog? = null
    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_security, null)
    lateinit var tabsAdapter: PasswordTypesAdapter
    lateinit var viewPager: MyDialogViewPager

    init {
        view.apply {
            viewPager = findViewById(R.id.dialog_tab_view_pager)
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(context, requiredHash, this@SecurityDialog)
            viewPager.adapter = tabsAdapter
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    dialog_tab_layout.getTabAt(position)!!.select()
                }
            })

            viewPager.onGlobalLayout {
                updateTabVisibility()
            }

            if (showTabIndex == SHOW_ALL_TABS) {
                val textColor = context.baseConfig.textColor

                if (!activity.isFingerPrintSensorAvailable())
                    dialog_tab_layout.removeTabAt(PROTECTION_FINGERPRINT)

                dialog_tab_layout.setTabTextColors(textColor, textColor)
                dialog_tab_layout.setSelectedTabIndicatorColor(context.baseConfig.primaryColor)
                dialog_tab_layout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {
                    }

                    override fun onTabSelected(tab: TabLayout.Tab) {
                        when {
                            tab.text.toString().equals(resources.getString(R.string.pattern), true) -> viewPager.currentItem = PROTECTION_PATTERN
                            tab.text.toString().equals(resources.getString(R.string.pin), true) -> viewPager.currentItem = PROTECTION_PIN
                            else -> viewPager.currentItem = PROTECTION_FINGERPRINT
                        }

                        updateTabVisibility()
                    }
                })
            } else {
                dialog_tab_layout.beGone()
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }

        dialog = AlertDialog.Builder(activity)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this)
        }
    }

    override fun receivedHash(hash: String, type: Int) {
        callback(hash, type)
        dialog!!.dismiss()
    }

    private fun updateTabVisibility() {
        for (i in 0..2) {
            tabsAdapter.isTabVisible(i, viewPager.currentItem == i)
        }
    }
}
