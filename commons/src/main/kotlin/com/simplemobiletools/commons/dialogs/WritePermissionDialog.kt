package com.simplemobiletools.commons.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_write_permission.view.*
import kotlinx.android.synthetic.main.dialog_write_permission_otg.view.*

class WritePermissionDialog(activity: Activity, val mode: Mode, val callback: () -> Unit) {
    enum class Mode {
        OTG,
        SD_CARD,
        SDK_30
    }

    var dialog: AlertDialog

    init {
        val layout = if (mode == Mode.OTG || mode == Mode.SDK_30) R.layout.dialog_write_permission_otg else R.layout.dialog_write_permission
        val view = activity.layoutInflater.inflate(layout, null)

        val glide = Glide.with(activity)
        val crossFade = DrawableTransitionOptions.withCrossFade()
        when (mode) {
            Mode.OTG -> {
                view.write_permissions_dialog_otg_text.setText(R.string.confirm_usb_storage_access_text)
                glide.load(R.drawable.img_write_storage_otg).transition(crossFade).into(view.write_permissions_dialog_otg_image)
            }
            Mode.SD_CARD -> {
                glide.load(R.drawable.img_write_storage).transition(crossFade).into(view.write_permissions_dialog_image)
                glide.load(R.drawable.img_write_storage_sd).transition(crossFade).into(view.write_permissions_dialog_image_sd)
            }
            Mode.SDK_30 -> {
                view.write_permissions_dialog_otg_text.setText(R.string.confirm_storage_access_android_text)
                glide.load(R.drawable.img_write_storage_sdk_30).transition(crossFade).into(view.write_permissions_dialog_otg_image)
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { dialog, which -> dialogConfirmed() }
            .setOnCancelListener {
                BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
                BaseSimpleActivity.funAfterSAFPermission = null
            }
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.confirm_storage_access_title)
            }
    }

    private fun dialogConfirmed() {
        dialog.dismiss()
        callback()
    }
}
