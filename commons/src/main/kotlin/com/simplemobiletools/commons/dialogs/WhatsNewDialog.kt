package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.dialog_whats_new.view.*

class WhatsNewDialog(val activity: Activity, val releases: List<Release>) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_whats_new, null)
        view.whats_new_content.text = getNewReleases()

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.whats_new)
        }
    }

    fun getNewReleases(): String {
        val config = BaseConfig.newInstance(activity)
        val sb = StringBuilder()

        releases.forEach {
            val parts = activity.getString(it.textId).split("\n").map(String::trim)
            parts.forEach {
                sb.append("- $it\n")
            }
        }

        config.lastVersion = if (releases.isEmpty()) 0 else releases.last().id
        return sb.toString()
    }
}
