package com.simplemobiletools.commons.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.*
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

private val startingTitlePadding = Modifier.padding(horizontal = 4.dp)

@Composable
internal fun AboutScreen(
    goBack: () -> Unit,
    helpUsSection: @Composable () -> Unit,
    aboutSection: @Composable () -> Unit,
    socialSection: @Composable () -> Unit,
    otherSection: @Composable () -> Unit
) {
    SettingsScaffold(title = stringResource(id = R.string.about), goBack = goBack) {
        aboutSection()
        helpUsSection()
        socialSection()
        otherSection()
        MadeWithLove()
        Spacer(modifier = Modifier.padding(bottom = 8.dp))
    }
}

@Composable
private fun MadeWithLove() {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = R.string.about_footer),
                modifier = Modifier
                    .fillMaxWidth()
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(24.dp),
            )
        }
    )
}

@Composable
internal fun HelpUsSection(
    onRateUsClick: () -> Unit,
    onInviteClick: () -> Unit,
    onContributorsClick: () -> Unit,
    showRateUs: Boolean,
    showInvite: Boolean,
    showDonate: Boolean,
    onDonateClick: () -> Unit,
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.help_us), modifier = startingTitlePadding)
    }) {
        if (showRateUs) {
            SettingsListItem(click = onRateUsClick, text = stringResource(id = R.string.rate_us), icon = R.drawable.ic_star_vector)
        }
        if (showInvite) {
            SettingsListItem(click = onInviteClick, text = stringResource(id = R.string.invite_friends), icon = R.drawable.ic_add_person_vector)
        }
        SettingsListItem(click = onContributorsClick, text = stringResource(id = R.string.contributors), icon = R.drawable.ic_face_vector)
        if (showDonate) {
            SettingsListItem(click = onDonateClick, text = stringResource(id = R.string.donate), icon = R.drawable.ic_dollar_vector)
        }
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun OtherSection(
    showMoreApps: Boolean,
    onMoreAppsClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    showWebsite: Boolean,
    showPrivacyPolicy: Boolean,
    onPrivacyPolicyClick: () -> Unit,
    onLicenseClick: () -> Unit,
    version: String,
    onVersionClick: () -> Unit,
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.other), modifier = startingTitlePadding)
    }) {
        if (showMoreApps) {
            SettingsListItem(click = onMoreAppsClick, text = stringResource(id = R.string.more_apps_from_us), icon = R.drawable.ic_heart_vector)
        }
        if (showWebsite) {
            SettingsListItem(click = onWebsiteClick, text = stringResource(id = R.string.website), icon = R.drawable.ic_link_vector)
        }
        if (showPrivacyPolicy) {
            SettingsListItem(click = onPrivacyPolicyClick, text = stringResource(id = R.string.privacy_policy), icon = R.drawable.ic_unhide_vector)
        }
        SettingsListItem(click = onLicenseClick, text = stringResource(id = R.string.third_party_licences), icon = R.drawable.ic_article_vector)
        SettingsListItem(click = onVersionClick, text = version, icon = R.drawable.ic_info_vector)
        SettingsHorizontalDivider()
    }
}


@Composable
internal fun AboutSection(
    setupFAQ: Boolean,
    onFAQClick: () -> Unit,
    onEmailClick: () -> Unit
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.support), modifier = startingTitlePadding)
    }) {
        if (setupFAQ) {
            SettingsListItem(click = onFAQClick, text = stringResource(id = R.string.frequently_asked_questions), icon = R.drawable.ic_question_mark_vector)
        }
        SettingsListItem(click = onEmailClick, text = stringResource(id = R.string.my_email), icon = R.drawable.ic_mail_vector)
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun SocialSection(
    onFacebookClick: () -> Unit,
    onGithubClick: () -> Unit,
    onRedditClick: () -> Unit,
    onTelegramClick: () -> Unit
) {
    SettingsGroup(title = {
        SettingsTitleTextComponent(text = stringResource(id = R.string.social), modifier = startingTitlePadding)
    }) {
        SettingsListItem(click = onFacebookClick, text = stringResource(id = R.string.facebook), icon = R.drawable.ic_facebook_vector, isImage = true)
        SettingsListItem(click = onGithubClick, text = stringResource(id = R.string.github), icon = R.drawable.ic_github_vector, isImage = true)
        SettingsListItem(click = onRedditClick, text = stringResource(id = R.string.reddit), icon = R.drawable.ic_reddit_vector, isImage = true)
        SettingsListItem(click = onTelegramClick, text = stringResource(id = R.string.telegram), icon = R.drawable.ic_telegram_vector, isImage = true)
        SettingsHorizontalDivider()
    }
}

@MyDevices
@Composable
private fun AboutScreenPreview() {
    AppThemeSurface {
        AboutScreen(
            goBack = {},
            aboutSection = {
                AboutSection(setupFAQ = true, onFAQClick = {}, onEmailClick = {})
            },
            helpUsSection = {
                HelpUsSection(
                    onRateUsClick = {},
                    onInviteClick = {},
                    onContributorsClick = {},
                    showRateUs = true,
                    showInvite = true,
                    showDonate = true,
                    onDonateClick = {}
                )
            },
            socialSection = {
                SocialSection(
                    onFacebookClick = {},
                    onGithubClick = {},
                    onRedditClick = {},
                    onTelegramClick = {}
                )
            },
            otherSection = {
                OtherSection(
                    showMoreApps = true,
                    onMoreAppsClick = {},
                    onWebsiteClick = {},
                    showWebsite = true,
                    showPrivacyPolicy = true,
                    onPrivacyPolicyClick = {},
                    onLicenseClick = {},
                    version = "5.0.4",
                    onVersionClick = {}
                )
            }
        )
    }
}
