package com.simplemobiletools.commons.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.dialogs.AddOrEditBlockedNumberAlertDialog
import com.simplemobiletools.commons.dialogs.AppSideLoadedAlertDialog

class TestDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppThemeSurface {
                val appSideLoadedDialogState = rememberAlertDialogState().apply {
                    DialogMember {
                        AppSideLoadedAlertDialog(onDownloadClick = {}, onCancelClick = {}, alertDialogState = this)
                    }
                }
                val addBlockedNumberDialogState = rememberAlertDialogState().apply {
                    DialogMember {
                        AddOrEditBlockedNumberAlertDialog(blockedNumber = null, deleteBlockedNumber = {}, addBlockedNumber = {}, alertDialogState = this)
                    }
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.padding(top = 16.dp))
                    ShowButton(appSideLoadedDialogState, text = "App side loaded dialog")
                    ShowButton(addBlockedNumberDialogState, text = "Add blocked number")
                    Spacer(modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }
    }

    @Composable
    private fun ShowButton(appSideLoadedDialogState: AlertDialogState, text: String) {
        Button(onClick = appSideLoadedDialogState::show) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
