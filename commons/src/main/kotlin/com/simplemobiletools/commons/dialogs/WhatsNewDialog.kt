package com.simplemobiletools.commons.dialogs

import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.Release
import kotlinx.android.synthetic.main.dialog_whats_new.view.*

class WhatsNewDialog(val activity: AppCompatActivity, val releases: List<Release>) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_whats_new, null)
        view.whats_new_content.text = getNewReleases()

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.whats_new)
                }
    }

    private fun getNewReleases(): String {
        val sb = StringBuilder()

        releases.forEach {
            val parts = activity.getString(it.textId).split("\n").map(String::trim)
            parts.forEach {
                sb.append("- $it\n")
            }
        }

        return sb.toString()
    }
}
