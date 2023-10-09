package com.simplemobiletools.commons.compose.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.lists.SimpleColumnScaffold
import com.simplemobiletools.commons.compose.settings.SettingsGroup
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.settings.SettingsListItem
import com.simplemobiletools.commons.compose.settings.SettingsTitleTextComponent
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme

private val startingTitlePadding = Modifier.padding(start = 60.dp)

@Composable
internal fun AboutScreen(
    goBack: () -> Unit,
    helpUsSection: @Composable () -> Unit,
    aboutSection: @Composable () -> Unit,
    socialSection: @Composable () -> Unit,
    otherSection: @Composable () -> Unit,
) {
    SimpleColumnScaffold(title = stringResource(id = R.string.about), goBack = goBack) {
        aboutSection()
        helpUsSection()
        socialSection()
        otherSection()
        SettingsListItem(text = stringResource(id = R.string.about_footer))
    }
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
            TwoLinerTextItem(text = stringResource(id = R.string.rate_us), icon = R.drawable.ic_star_vector, click = onRateUsClick)
        }
        if (showInvite) {
            TwoLinerTextItem(text = stringResource(id = R.string.invite_friends), icon = R.drawable.ic_add_person_vector, click = onInviteClick)
        }
        TwoLinerTextItem(
            click = onContributorsClick,
            text = stringResource(id = R.string.contributors),
            icon = R.drawable.ic_face_vector
        )
        if (showDonate) {
            TwoLinerTextItem(
                click = onDonateClick,
                text = stringResource(id = R.string.donate),
                icon = R.drawable.ic_dollar_vector
            )
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
            TwoLinerTextItem(
                click = onMoreAppsClick,
                text = stringResource(id = R.string.more_apps_from_us),
                icon = R.drawable.ic_heart_vector
            )
        }
        if (showWebsite) {
            TwoLinerTextItem(
                click = onWebsiteClick,
                text = stringResource(id = R.string.website),
                icon = R.drawable.ic_link_vector
            )
        }
        if (showPrivacyPolicy) {
            TwoLinerTextItem(
                click = onPrivacyPolicyClick,
                text = stringResource(id = R.string.privacy_policy),
                icon = R.drawable.ic_unhide_vector
            )
        }
        TwoLinerTextItem(
            click = onLicenseClick,
            text = stringResource(id = R.string.third_party_licences),
            icon = R.drawable.ic_article_vector
        )
        TwoLinerTextItem(
            click = onVersionClick,
            text = version,
            icon = R.drawable.ic_info_vector
        )
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
            TwoLinerTextItem(
                click = onFAQClick,
                text = stringResource(id = R.string.frequently_asked_questions),
                icon = R.drawable.ic_question_mark_vector
            )
        }
        TwoLinerTextItem(
            click = onEmailClick,
            text = stringResource(id = R.string.my_email),
            icon = R.drawable.ic_mail_vector
        )
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
        SocialText(
            click = onFacebookClick,
            text = stringResource(id = R.string.facebook),
            icon = R.drawable.ic_facebook_vector,
        )
        SocialText(
            click = onGithubClick,
            text = stringResource(id = R.string.github),
            icon = R.drawable.ic_github_vector,
            tint = SimpleTheme.colorScheme.onSurface
        )
        SocialText(
            click = onRedditClick,
            text = stringResource(id = R.string.reddit),
            icon = R.drawable.ic_reddit_vector,
        )
        SocialText(
            click = onTelegramClick,
            text = stringResource(id = R.string.telegram),
            icon = R.drawable.ic_telegram_vector,
        )
        SettingsHorizontalDivider()
    }
}

@Composable
internal fun SocialText(
    text: String,
    icon: Int,
    tint: Color? = null,
    click: () -> Unit
) {
    SettingsListItem(
        click = click,
        text = text,
        icon = icon,
        isImage = true,
        tint = tint,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
internal fun TwoLinerTextItem(text: String, icon: Int, click: () -> Unit) {
    SettingsListItem(
        tint = SimpleTheme.colorScheme.onSurface,
        click = click,
        text = text,
        icon = icon,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@MyDevices
@Composable
private fun AboutScreenPreview() {
    AppThemeSurface {
        AboutScreen(
            goBack = {},
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
            aboutSection = {
                AboutSection(setupFAQ = true, onFAQClick = {}, onEmailClick = {})
            },
            socialSection = {
                SocialSection(
                    onFacebookClick = {},
                    onGithubClick = {},
                    onRedditClick = {},
                    onTelegramClick = {}
                )
            }
        ) {
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
    }
}
