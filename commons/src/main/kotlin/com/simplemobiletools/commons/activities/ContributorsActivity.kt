package com.simplemobiletools.commons.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.extensions.AdjustNavigationBarColors
import com.simplemobiletools.commons.compose.extensions.TransparentSystemBars
import com.simplemobiletools.commons.compose.screens.ContributorsScreen
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.models.LanguageContributor
import kotlinx.collections.immutable.toImmutableList

class ContributorsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            TransparentSystemBars()
            AppThemeSurface {
                var canScroll by remember { mutableStateOf<Boolean?>(null) }
                AdjustNavigationBarColors(canScroll)
                val contributors = remember {
                    languageContributors()
                }
                val showContributorsLabel = remember {
                    !resources.getBoolean(R.bool.hide_all_external_links)
                }
                ContributorsScreen(
                    goBack = ::finish,
                    showContributorsLabel = showContributorsLabel,
                    contributors = contributors,
                    canScroll = { canPerformScroll -> canScroll = canPerformScroll }
                )
            }
        }
    }

    private fun languageContributors() = listOf(
        LanguageContributor(R.drawable.ic_flag_arabic_vector, R.string.translation_arabic, R.string.translators_arabic),
        LanguageContributor(R.drawable.ic_flag_azerbaijani_vector, R.string.translation_azerbaijani, R.string.translators_azerbaijani),
        LanguageContributor(R.drawable.ic_flag_bengali_vector, R.string.translation_bengali, R.string.translators_bengali),
        LanguageContributor(R.drawable.ic_flag_catalan_vector, R.string.translation_catalan, R.string.translators_catalan),
        LanguageContributor(R.drawable.ic_flag_czech_vector, R.string.translation_czech, R.string.translators_czech),
        LanguageContributor(R.drawable.ic_flag_welsh_vector, R.string.translation_welsh, R.string.translators_welsh),
        LanguageContributor(R.drawable.ic_flag_danish_vector, R.string.translation_danish, R.string.translators_danish),
        LanguageContributor(R.drawable.ic_flag_german_vector, R.string.translation_german, R.string.translators_german),
        LanguageContributor(R.drawable.ic_flag_greek_vector, R.string.translation_greek, R.string.translators_greek),
        LanguageContributor(R.drawable.ic_flag_spanish_vector, R.string.translation_spanish, R.string.translators_spanish),
        LanguageContributor(R.drawable.ic_flag_basque_vector, R.string.translation_basque, R.string.translators_basque),
        LanguageContributor(R.drawable.ic_flag_persian_vector, R.string.translation_persian, R.string.translators_persian),
        LanguageContributor(R.drawable.ic_flag_finnish_vector, R.string.translation_finnish, R.string.translators_finnish),
        LanguageContributor(R.drawable.ic_flag_french_vector, R.string.translation_french, R.string.translators_french),
        LanguageContributor(R.drawable.ic_flag_galician_vector, R.string.translation_galician, R.string.translators_galician),
        LanguageContributor(R.drawable.ic_flag_hindi_vector, R.string.translation_hindi, R.string.translators_hindi),
        LanguageContributor(R.drawable.ic_flag_croatian_vector, R.string.translation_croatian, R.string.translators_croatian),
        LanguageContributor(R.drawable.ic_flag_hungarian_vector, R.string.translation_hungarian, R.string.translators_hungarian),
        LanguageContributor(R.drawable.ic_flag_indonesian_vector, R.string.translation_indonesian, R.string.translators_indonesian),
        LanguageContributor(R.drawable.ic_flag_italian_vector, R.string.translation_italian, R.string.translators_italian),
        LanguageContributor(R.drawable.ic_flag_hebrew_vector, R.string.translation_hebrew, R.string.translators_hebrew),
        LanguageContributor(R.drawable.ic_flag_japanese_vector, R.string.translation_japanese, R.string.translators_japanese),
        LanguageContributor(R.drawable.ic_flag_korean_vector, R.string.translation_korean, R.string.translators_korean),
        LanguageContributor(R.drawable.ic_flag_lithuanian_vector, R.string.translation_lithuanian, R.string.translators_lithuanian),
        LanguageContributor(R.drawable.ic_flag_nepali_vector, R.string.translation_nepali, R.string.translators_nepali),
        LanguageContributor(R.drawable.ic_flag_norwegian_vector, R.string.translation_norwegian, R.string.translators_norwegian),
        LanguageContributor(R.drawable.ic_flag_dutch_vector, R.string.translation_dutch, R.string.translators_dutch),
        LanguageContributor(R.drawable.ic_flag_polish_vector, R.string.translation_polish, R.string.translators_polish),
        LanguageContributor(R.drawable.ic_flag_portuguese_vector, R.string.translation_portuguese, R.string.translators_portuguese),
        LanguageContributor(R.drawable.ic_flag_romanian_vector, R.string.translation_romanian, R.string.translators_romanian),
        LanguageContributor(R.drawable.ic_flag_russian_vector, R.string.translation_russian, R.string.translators_russian),
        LanguageContributor(R.drawable.ic_flag_slovak_vector, R.string.translation_slovak, R.string.translators_slovak),
        LanguageContributor(R.drawable.ic_flag_slovenian_vector, R.string.translation_slovenian, R.string.translators_slovenian),
        LanguageContributor(R.drawable.ic_flag_swedish_vector, R.string.translation_swedish, R.string.translators_swedish),
        LanguageContributor(R.drawable.ic_flag_tamil_vector, R.string.translation_tamil, R.string.translators_tamil),
        LanguageContributor(R.drawable.ic_flag_turkish_vector, R.string.translation_turkish, R.string.translators_turkish),
        LanguageContributor(R.drawable.ic_flag_ukrainian_vector, R.string.translation_ukrainian, R.string.translators_ukrainian),
        LanguageContributor(R.drawable.ic_flag_chinese_hk_vector, R.string.translation_chinese_hk, R.string.translators_chinese_hk),
        LanguageContributor(R.drawable.ic_flag_chinese_cn_vector, R.string.translation_chinese_cn, R.string.translators_chinese_cn),
        LanguageContributor(R.drawable.ic_flag_chinese_tw_vector, R.string.translation_chinese_tw, R.string.translators_chinese_tw)
    ).toImmutableList()
}
