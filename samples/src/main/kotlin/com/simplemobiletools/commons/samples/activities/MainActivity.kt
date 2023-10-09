package com.simplemobiletools.commons.samples.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.commons.activities.AboutActivity
import com.simplemobiletools.commons.activities.CustomizationActivity
import com.simplemobiletools.commons.activities.ManageBlockedNumbersActivity
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.extensions.*
import com.simplemobiletools.commons.compose.lists.SimpleLazyListScaffold
import com.simplemobiletools.commons.compose.lists.simpleTopAppBarColors
import com.simplemobiletools.commons.compose.lists.topAppBarInsets
import com.simplemobiletools.commons.compose.lists.topAppBarPaddings
import com.simplemobiletools.commons.compose.menus.ActionItem
import com.simplemobiletools.commons.compose.menus.ActionMenu
import com.simplemobiletools.commons.compose.menus.OverflowMode
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.DonateAlertDialog
import com.simplemobiletools.commons.dialogs.RateStarsAlertDialog
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.launchMoreAppsFromUsIntent
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.samples.BuildConfig
import com.simplemobiletools.commons.samples.R
import kotlinx.collections.immutable.toImmutableList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                SimpleLazyListScaffold(
                    customTopBar = { scrolledColor: Color, _: MutableInteractionSource, scrollBehavior: TopAppBarScrollBehavior, statusBarColor: Int, colorTransitionFraction: Float, contrastColor: Color ->
                        TopAppBar(
                            title = {},
                            actions = {
                                val actionMenus = remember {
                                    val about = ActionItem(
                                        com.simplemobiletools.commons.R.string.about,
                                        icon = Icons.Outlined.Info,
                                        doAction = ::launchAbout,
                                        overflowMode = OverflowMode.NEVER_OVERFLOW
                                    )
                                    val moreApps =
                                        ActionItem(
                                            com.simplemobiletools.commons.R.string.more_apps_from_us,
                                            doAction = ::launchMoreAppsFromUsIntent,
                                            overflowMode = OverflowMode.ALWAYS_OVERFLOW
                                        )
                                    listOf(about, moreApps).toImmutableList()
                                }
                                var isMenuVisible by remember { mutableStateOf(false) }
                                ActionMenu(
                                    items = actionMenus,
                                    numIcons = 2,
                                    isMenuVisible = isMenuVisible,
                                    onMenuToggle = { isMenuVisible = it },
                                    iconsColor = scrolledColor
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            colors = simpleTopAppBarColors(statusBarColor, colorTransitionFraction, contrastColor),
                            modifier = Modifier.topAppBarPaddings(),
                            windowInsets = topAppBarInsets()
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = ::startCustomizationActivity
                        ) {
                            Text(stringResource(id = com.simplemobiletools.commons.R.string.color_customization))
                        }
                        Button(
                            onClick = ::launchAbout
                        ) {
                            Text("About")
                        }
                        Button(
                            onClick = {
                                startActivity(Intent(this@MainActivity, ManageBlockedNumbersActivity::class.java))
                            }
                        ) {
                            Text("Manage blocked numbers")
                        }
                        Button(
                            onClick = {
                                startActivity(Intent(this@MainActivity, TestDialogActivity::class.java))
                            }
                        ) {
                            Text("Compose dialogs")
                        }
                        Button(
                            onClick = {
                                ConfirmationDialog(
                                    this@MainActivity,
                                    FAKE_VERSION_APP_LABEL,
                                    positive = com.simplemobiletools.commons.R.string.ok,
                                    negative = 0
                                ) {
                                    launchViewIntent(DEVELOPER_PLAY_STORE_URL)
                                }
                            }
                        ) {
                            Text("Test button")
                        }
                    }
                }

                AppLaunched()
            }
        }
    }

    @Composable
    private fun AppLaunched(
        donateAlertDialogState: AlertDialogState = getDonateAlertDialogState(),
        rateStarsAlertDialogState: AlertDialogState = getRateStarsAlertDialogState(),
    ) {
        LaunchedEffect(Unit) {
            appLaunchedCompose(
                appId = BuildConfig.APPLICATION_ID,
                showDonateDialog = donateAlertDialogState::show,
                showRateUsDialog = rateStarsAlertDialogState::show,
                showUpgradeDialog = {}
            )
        }
    }

    @Composable
    private fun getDonateAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                DonateAlertDialog(alertDialogState = this)
            }
        }

    @Composable
    private fun getRateStarsAlertDialogState() = rememberAlertDialogState().apply {
        DialogMember {
            RateStarsAlertDialog(alertDialogState = this, onRating = ::rateStarsRedirectAndThankYou)
        }
    }

    private fun startCustomizationActivity() {
        Intent(applicationContext, CustomizationActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            startActivity(this)
        }
    }

    private fun launchAbout() {
        val licenses = LICENSE_AUTOFITTEXTVIEW

        val faqItems = arrayListOf(
            FAQItem(com.simplemobiletools.commons.R.string.faq_1_title_commons, com.simplemobiletools.commons.R.string.faq_1_text_commons),
            FAQItem(com.simplemobiletools.commons.R.string.faq_1_title_commons, com.simplemobiletools.commons.R.string.faq_1_text_commons),
            FAQItem(com.simplemobiletools.commons.R.string.faq_4_title_commons, com.simplemobiletools.commons.R.string.faq_4_text_commons)
        )

        if (!resources.getBoolean(com.simplemobiletools.commons.R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(com.simplemobiletools.commons.R.string.faq_2_title_commons, com.simplemobiletools.commons.R.string.faq_2_text_commons))
            faqItems.add(FAQItem(com.simplemobiletools.commons.R.string.faq_6_title_commons, com.simplemobiletools.commons.R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.smtco_app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun startAboutActivity(
        appNameId: Int, licenseMask: Long, versionName: String, faqItems: ArrayList<FAQItem>, showFAQBeforeMail: Boolean,
        getAppIconIDs: ArrayList<Int> = getAppIconIDs(),
        getAppLauncherName: String = getAppLauncherName()
    ) {
        hideKeyboard()
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs)
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName)
            putExtra(APP_NAME, getString(appNameId))
            putExtra(APP_LICENSES, licenseMask)
            putExtra(APP_VERSION_NAME, versionName)
            putExtra(APP_FAQ, faqItems)
            putExtra(SHOW_FAQ_BEFORE_MAIL, showFAQBeforeMail)
            startActivity(this)
        }
    }

    private fun getAppLauncherName() = getString(R.string.smtco_app_name)

    private fun getAppIconIDs(): ArrayList<Int> {
        val ids = ArrayList<Int>()
        ids.add(R.mipmap.commons_launcher)
        return ids
    }
}
