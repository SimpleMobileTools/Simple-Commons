package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.DialogNewAppsIconsBinding
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.launchViewIntent
import com.simplemobiletools.commons.extensions.setupDialogStuff

class NewAppsIconsDialog(val activity: Activity) {
    init {
        val view = DialogNewAppsIconsBinding.inflate(activity.layoutInflater, null, false).apply {
            val dialerUrl = "https://play.google.com/store/apps/details?id=com.simplemobiletools.dialer"
            val smsMessengerUrl = "https://play.google.com/store/apps/details?id=com.simplemobiletools.smsmessenger"
            val voiceRecorderUrl = "https://play.google.com/store/apps/details?id=com.simplemobiletools.voicerecorder"

            val text = String.format(
                activity.getString(R.string.new_app),
                dialerUrl, activity.getString(R.string.simple_dialer),
                smsMessengerUrl, activity.getString(R.string.simple_sms_messenger),
                voiceRecorderUrl, activity.getString(R.string.simple_voice_recorder)
            )

            newAppsText.text = Html.fromHtml(text)
            newAppsText.movementMethod = LinkMovementMethod.getInstance()

            newAppsDialer.setOnClickListener { activity.launchViewIntent(dialerUrl) }
            newAppsSmsMessenger.setOnClickListener { activity.launchViewIntent(smsMessengerUrl) }
            newAppsVoiceRecorder.setOnClickListener { activity.launchViewIntent(voiceRecorderUrl) }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .apply {
                activity.setupDialogStuff(view.root, this, cancelOnTouchOutside = false)
            }
    }
}
