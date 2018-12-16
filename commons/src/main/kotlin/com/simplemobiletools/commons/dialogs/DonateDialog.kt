package com.simplemobiletools.commons.dialogs

import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_textview.view.*

class DonateDialog(val activity: AppCompatActivity) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_textview, null).apply {
            text_view.text = Html.fromHtml(activity.getString(R.string.donate_please))
            text_view.movementMethod = LinkMovementMethod.getInstance()
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.purchase) { dialog, which -> activity.launchViewIntent(R.string.thank_you_url) }
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }
}
