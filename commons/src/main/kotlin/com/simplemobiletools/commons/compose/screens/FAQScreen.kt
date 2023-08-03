package com.simplemobiletools.commons.compose.screens

import android.os.Build
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.settings.scaffold.SettingsLazyScaffold
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.extensions.removeUnderlines
import com.simplemobiletools.commons.models.FAQItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList


@Composable
internal fun FAQScreen(
    goBack: () -> Unit,
    faqItems: ImmutableList<FAQItem>
) {
    SettingsLazyScaffold(
        title = stringResource(id = R.string.frequently_asked_questions),
        goBack = goBack,
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(faqItems) { faqItem ->
            ListItem(headlineContent = {
                val text = if (faqItem.title is Int) stringResource(faqItem.title) else faqItem.title as String
                Text(
                    text = text,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }, supportingContent = {
                if (faqItem.text is Int) {
                    val text = fromHtml(stringResource(id = faqItem.text))
                    LinkifyText(
                        text = { text },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = faqItem.text as String,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            })
        }
    }
}

@Suppress("deprecation")
private fun fromHtml(source: String): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(source)
    }
}

@Composable
private fun LinkifyText(modifier: Modifier = Modifier, text: () -> Spanned) {
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
            ).toImmutableList()
        )
    }
}

