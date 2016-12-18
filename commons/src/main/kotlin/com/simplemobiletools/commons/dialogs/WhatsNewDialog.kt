package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.getDialogBackgroundColor
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.dialog_whats_new.view.*

class WhatsNewDialog(val activity: Activity, val releases: List<Release>) {
    var dialog: AlertDialog? = null

    init {
        val baseConfig = BaseConfig.newInstance(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_whats_new, null)
        view.whats_new_content.text = getNewReleases()

        val builder = AlertDialog.Builder(activity)
                .setTitle(R.string.whats_new)
                .setView(view)
                .setPositiveButton(R.string.ok, null)

        val primaryColor = BaseConfig.newInstance(activity).primaryColor
        dialog = builder.create().apply {
            setCanceledOnTouchOutside(true)
            show()
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(primaryColor)
            window.setBackgroundDrawable(context.getDialogBackgroundColor(baseConfig.backgroundColor))
        }
    }

    fun getNewReleases(): String {
        val config = BaseConfig.newInstance(activity)
        val lastVersion = config.lastVersion
        val sb = StringBuilder()

        releases.forEach {
            if (it.id > lastVersion) {
                val parts = activity.getString(it.textId).split("\n").map(String::trim)
                parts.forEach {
                    sb.append("- $it\n")
                }
            }
        }

        config.lastVersion = if (releases.isEmpty()) 0 else releases.last().id
        return sb.toString()
    }
}
