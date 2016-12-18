package com.simplemobiletools.commons.activities

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatTextView
import android.view.MenuItem
import android.view.ViewGroup
import com.simplemobiletools.commons.helpers.APP_LICENSES
import com.simplemobiletools.commons.helpers.APP_NAME
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.helpers.OPEN_DOCUMENT_TREE
import com.simplemobiletools.commons.views.MyAppCompatSpinner
import com.simplemobiletools.commons.views.MySwitchCompat
import com.simplemobiletools.filepicker.extensions.isShowingWritePermissions
import java.io.File

open class BaseSimpleActivity : AppCompatActivity() {
    lateinit var baseConfig: BaseConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseConfig = BaseConfig.newInstance(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundColor()
        updateActionbarColor()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun updateBackgroundColor(color: Int = baseConfig.backgroundColor) {
        window.decorView.setBackgroundColor(color)
    }

    fun updateActionbarColor(color: Int = baseConfig.primaryColor) {
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
        updateStatusbarColor(color)
    }

    fun updateStatusbarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[2] *= 0.9f
            window.statusBarColor = Color.HSVToColor(hsv)
        }
    }

    fun updateTextColors(viewGroup: ViewGroup, color: Int = baseConfig.textColor) {
        val cnt = viewGroup.childCount
        (0..cnt - 1).map { viewGroup.getChildAt(it) }
                .forEach {
                    if (it is AppCompatEditText) {
                        it.background.mutate().setColorFilter(baseConfig.primaryColor, PorterDuff.Mode.SRC_ATOP)
                        it.setTextColor(color)
                    } else if (it is AppCompatTextView) {
                        it.setTextColor(color)
                    } else if (it is MyAppCompatSpinner) {
                        it.setColor(color)
                    } else if (it is MySwitchCompat) {
                        it.setColor(color)
                        it.setTextColor(color)
                    } else if (it is ViewGroup) {
                        updateTextColors(it, color)
                    }
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == OPEN_DOCUMENT_TREE && resultCode == Activity.RESULT_OK && resultData != null) {
            saveTreeUri(resultData)
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun saveTreeUri(resultData: Intent) {
        val treeUri = resultData.data
        baseConfig.treeUri = treeUri.toString()

        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(treeUri, takeFlags)
    }

    fun startAboutActivity(appNameId: Int, licenseMask: Int) {
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_NAME, getString(appNameId))
            putExtra(APP_LICENSES, licenseMask)
            startActivity(this)
        }
    }

    fun startCustomizationActivity() = startActivity(Intent(this, CustomizationActivity::class.java))

    fun launchViewIntent(id: Int) = launchViewIntent(resources.getString(id))

    fun launchViewIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    fun isShowingPermDialog(file: File) = isShowingWritePermissions(file, baseConfig.treeUri, OPEN_DOCUMENT_TREE)
}
