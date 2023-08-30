package com.simplemobiletools.commons.compose.screens

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.SettingsHorizontalDivider
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsLazyScaffold
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.extensions.removeUnderlines
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun FAQScreen(
    goBack: () -> Unit,
    faqItems: ImmutableList<FAQItem>,
    canScroll: (canPerformScroll: Boolean) -> Unit,
) {
    SettingsLazyScaffold(
        title = stringResource(id = R.string.frequently_asked_questions),
        goBack = goBack,
        contentPadding = PaddingValues(bottom = 8.dp),
        canScroll = canScroll
    ) { paddingValues ->
        itemsIndexed(faqItems) { index, faqItem ->
            Column(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = {
                        val text = if (faqItem.title is Int) stringResource(faqItem.title) else faqItem.title as String
                        Text(
                            text = text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp,
                        )
                    },
                    supportingContent = {
                        if (faqItem.text is Int) {
                            val text = stringFromHTML(stringResource(id = faqItem.text))
                            LinkifyText(
                                text = { text },
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = faqItem.text as String,
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        }
                    },
                )
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
                if (index != faqItems.lastIndex) {
                    SettingsHorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding()))
        }
    }
}

@Suppress("deprecation")
fun stringFromHTML(source: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(source)
    }
}

@Composable
fun LinkifyText(modifier: Modifier = Modifier, fontSize: TextUnit = 14.sp, text: () -> Spanned) {
    val context = LocalContext.current
    val customLinkifyTextView = remember {
        TextView(context)
    }
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkTextColor = MaterialTheme.colorScheme.primary
    AndroidView(modifier = modifier, factory = { customLinkifyTextView }) { textView ->
        textView.setTextColor(textColor.toArgb())
        textView.setLinkTextColor(linkTextColor.toArgb())
        textView.text = text()
        textView.textSize = fontSize.value
        textView.movementMethod = LinkMovementMethod.getInstance()
        customLinkifyTextView.removeUnderlines()
    }
}

@MyDevices
@Composable
private fun FAQScreenPreview() {
    AppThemeSurface {
        FAQScreen(
            goBack = {},
            faqItems = listOf(
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons),
                FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons)
            ).toImmutableList(),
            canScroll = {}
        )
    }
}
