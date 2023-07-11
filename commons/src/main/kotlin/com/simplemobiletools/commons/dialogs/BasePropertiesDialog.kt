package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.content.res.Resources
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.exifinterface.media.ExifInterface
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FileDirItem
import kotlinx.android.synthetic.main.dialog_properties.view.*
import kotlinx.android.synthetic.main.item_property.view.*
import java.io.File
import java.util.*

abstract class BasePropertiesDialog(activity: Activity) {
    protected val mInflater: LayoutInflater
    protected val mPropertyView: ViewGroup
    protected val mResources: Resources
    protected val mActivity: Activity = activity
    protected val mDialogView: View

    init {
        mInflater = LayoutInflater.from(activity)
        mResources = activity.resources
        mDialogView = mInflater.inflate(R.layout.dialog_properties, null)
        mPropertyView = mDialogView.properties_holder!!
    }

    protected fun addProperty(labelId: Int, value: String?, viewId: Int = 0) {
        if (value == null) {
            return
        }

        mInflater.inflate(R.layout.item_property, mPropertyView, false).apply {
            property_value.setTextColor(mActivity.getProperTextColor())
            property_label.setTextColor(mActivity.getProperTextColor())

            property_label.text = mResources.getString(labelId)
            property_value.text = value
            mPropertyView.properties_holder.addView(this)

            setOnLongClickListener {
                mActivity.copyToClipboard(property_value.value)
                true
            }

            if (labelId == R.string.gps_coordinates) {
                setOnClickListener {
                    mActivity.showLocationOnMap(value)
                }
            }

            if (viewId != 0) {
                id = viewId
            }
        }
    }
}
