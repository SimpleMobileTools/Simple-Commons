package com.simplemobiletools.commons.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.simplemobiletools.commons.helpers.APP_NAME
import com.simplemobiletools.commons.helpers.Config

open class SimpleActivity : AppCompatActivity() {
    lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        config = Config.newInstance(applicationContext)
        super.onCreate(savedInstanceState)
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

    fun startAboutActivity(appNameId: Int) {
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_NAME, getString(appNameId))
            startActivity(this)
        }
    }
}
