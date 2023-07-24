package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import com.simplemobiletools.commons.databinding.ActivityFaqBinding
import com.simplemobiletools.commons.databinding.ItemFaqBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.APP_FAQ
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.models.FAQItem

class FAQActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    private val binding by viewBinding(ActivityFaqBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        updateMaterialActivityViews(binding.faqCoordinator, binding.faqHolder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(binding.faqNestedScrollview, binding.faqToolbar)

        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()

        val inflater = LayoutInflater.from(this)
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        faqItems.forEach {
            val faqItem = it

            ItemFaqBinding.inflate(inflater, null, false).apply {
                faqCard.setCardBackgroundColor(backgroundColor)
                faqTitle.apply {
                    text = if (faqItem.title is Int) getString(faqItem.title) else faqItem.title as String
                    setTextColor(primaryColor)
                }

                faqText.apply {
                    text = if (faqItem.text is Int) Html.fromHtml(getString(faqItem.text)) else faqItem.text as String
                    setTextColor(textColor)
                    setLinkTextColor(primaryColor)

                    movementMethod = LinkMovementMethod.getInstance()
                    removeUnderlines()
                }

                binding.faqHolder.addView(root)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.faqToolbar, NavigationIcon.Arrow)
    }
}
