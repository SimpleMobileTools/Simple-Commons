package com.simplemobiletools.commons.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.SettingsGroupTitle
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.settings.SettingsListItem
import com.simplemobiletools.commons.compose.settings.SettingsTitleTextComponent
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsLazyScaffold
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.models.LanguageContributor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

private val startingPadding = Modifier.padding(start = 58.dp)

@Composable
internal fun ContributorsScreen(
    goBack: () -> Unit,
    showContributorsLabel: Boolean,
    contributors: ImmutableList<LanguageContributor>,
    canScroll: (canPerformScroll: Boolean) -> Unit,
) {
    SettingsLazyScaffold(
        canScroll = canScroll,
        title = { scrolledColor ->
            Text(
                text = stringResource(id = R.string.contributors),
                modifier = Modifier
                    .padding(start = 28.dp)
                    .fillMaxWidth(),
                color = scrolledColor
            )
        },
        goBack = goBack,
        lazyContent = { paddingValues ->
            item {
                SettingsGroupTitle {
                    SettingsTitleTextComponent(text = stringResource(id = R.string.development), modifier = startingPadding)
                }
            }
            item {
                SettingsListItem(
                    text = stringResource(id = R.string.contributors_developers),
                    icon = R.drawable.ic_code_vector,
                    tint = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
            item {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                SettingsHorizontalDivider()
            }
            item {
                SettingsGroupTitle {
                    SettingsTitleTextComponent(text = stringResource(id = R.string.translation), modifier = startingPadding)
                }
            }
            items(contributors, key = { it.contributorsId.plus(it.iconId).plus(it.labelId) }) {
                ContributorItem(
                    languageContributor = it
                )
            }
            if (showContributorsLabel) {
                item {
                    SettingsListItem(
                        icon = R.drawable.ic_heart_vector,
                        text = {
                            val source = stringResource(id = R.string.contributors_label)
                            LinkifyText {
                                stringFromHTML(source)
                            }
                        },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                item {
                    Spacer(modifier = Modifier.padding(bottom = 8.dp))
                }
            }
            item {
                Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
            }
        })
}

@Composable
private fun ContributorItem(
    modifier: Modifier = Modifier,
    languageContributor: LanguageContributor
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = languageContributor.labelId),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(modifier)
            )
        },
        leadingContent = {
            val imageSize = Modifier
                .size(48.dp)
                .padding(8.dp)
            Image(
                modifier = imageSize,
                painter = painterResource(id = languageContributor.iconId),
                contentDescription = stringResource(id = languageContributor.contributorsId),
            )
        },
        modifier = Modifier
            .fillMaxWidth(),
        supportingContent = {
            Text(
                text = stringResource(id = languageContributor.contributorsId),
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}

@Composable
@MyDevices
private fun ContributorsScreenPreview() {
    AppThemeSurface {
        ContributorsScreen(
            goBack = {},
            contributors = listOf(
                LanguageContributor(R.drawable.ic_flag_arabic_vector, R.string.translation_arabic, R.string.translators_arabic),
                LanguageContributor(R.drawable.ic_flag_azerbaijani_vector, R.string.translation_azerbaijani, R.string.translators_azerbaijani),
                LanguageContributor(R.drawable.ic_flag_bengali_vector, R.string.translation_bengali, R.string.translators_bengali),
                LanguageContributor(R.drawable.ic_flag_catalan_vector, R.string.translation_catalan, R.string.translators_catalan),
            ).toImmutableList(),
            showContributorsLabel = true,
            canScroll = {}
        )
    }
}
