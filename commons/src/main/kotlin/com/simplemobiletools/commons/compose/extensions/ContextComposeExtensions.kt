package com.simplemobiletools.commons.compose.extensions

import android.content.Context
import com.simplemobiletools.commons.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)
