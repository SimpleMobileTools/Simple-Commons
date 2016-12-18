package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.License
import kotlinx.android.synthetic.main.activity_license.*
import kotlinx.android.synthetic.main.license_item.view.*

class LicenseActivity : BaseSimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        val inflater = LayoutInflater.from(this)
        val licenses = initLicenses()
        val licenseMask = intent.getIntExtra(APP_LICENSES, 0)

        licenses.filter { licenseMask and it.id != 0 }.forEach {
            val license = it
            val view = inflater.inflate(R.layout.license_item, null)
            view.apply {
                license_title.text = getUnderlinedTitle(getString(license.titleId))
                license_title.setOnClickListener { launchViewIntent(license.urlId) }
                license_title.setTextColor(baseConfig.primaryColor)
                license_text.text = getString(license.textId)
                licenses_holder.addView(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(licenses_holder)
    }

    fun getUnderlinedTitle(title: String): SpannableString {
        val underlined = SpannableString(title)
        underlined.setSpan(UnderlineSpan(), 0, title.length, 0)
        return underlined
    }

    fun initLicenses() =
            arrayOf(
                    License(LICENSE_KOTLIN, R.string.kotlin_title, R.string.kotlin_text, R.string.kotlin_url),
                    License(LICENSE_SUBSAMPLING, R.string.subsampling_title, R.string.subsampling_text, R.string.subsampling_url),
                    License(LICENSE_GLIDE, R.string.glide_title, R.string.glide_text, R.string.glide_url),
                    License(LICENSE_CROPPER, R.string.cropper_title, R.string.cropper_text, R.string.cropper_url),
                    License(LICENSE_MULTISELECT, R.string.multiselect_title, R.string.multiselect_text, R.string.multiselect_url),
                    License(LICENSE_RTL, R.string.rtl_viewpager_title, R.string.rtl_viewpager_text, R.string.rtl_viewpager_url),
                    License(LICENSE_FILEPICKER, R.string.filepicker_title, R.string.filepicker_text, R.string.filepicker_url),
                    License(LICENSE_FILEPROPERTIES, R.string.fileproperties_title, R.string.fileproperties_text, R.string.fileproperties_url),
                    License(LICENSE_AMBILWARNA, R.string.ambilwarna_title, R.string.ambilwarna_text, R.string.ambilwarna_url),
                    License(LICENSE_JODA, R.string.joda_title, R.string.joda_text, R.string.joda_url),
                    License(LICENSE_STETHO, R.string.stetho_title, R.string.stetho_text, R.string.stetho_url),
                    License(LICENSE_OTTO, R.string.otto_title, R.string.otto_text, R.string.otto_url)
            )
}
