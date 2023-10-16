package com.simplemobiletools.commons.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.*
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.models.BlockedNumber

@Composable
fun AddOrEditBlockedNumberAlertDialog(
    alertDialogState: AlertDialogState,
    blockedNumber: BlockedNumber?,
    modifier: Modifier = Modifier,
    deleteBlockedNumber: (String) -> Unit,
    addBlockedNumber: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = blockedNumber?.number.orEmpty(),
                selection = TextRange(blockedNumber?.number?.length ?: 0)
            )
        )
    }

    AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder,
        onDismissRequest = alertDialogState::hide,
        confirmButton = {
            TextButton(onClick = {
                var newBlockedNumber = textFieldValue.text
                if (blockedNumber != null && newBlockedNumber != blockedNumber.number) {
                    deleteBlockedNumber(blockedNumber.number)
                }
                if (newBlockedNumber.isNotEmpty()) {
                    // in case the user also added a '.' in the pattern, remove it
                    if (newBlockedNumber.contains(".*")) {
                        newBlockedNumber = newBlockedNumber.replace(".*", "*")
                    }
                    addBlockedNumber(newBlockedNumber)
                }
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        shape = dialogShape,
        text = {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                },
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.add_blocked_number_helper_text),
                        color = dialogTextColor
                    )
                },
                label = {
                    Text(text = stringResource(id = R.string.number))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        },
        tonalElevation = dialogElevation
    )
    ShowKeyboardWhenDialogIsOpenedAndRequestFocus(focusRequester = focusRequester)
}

@Composable
@MyDevices
private fun AddOrEditBlockedNumberAlertDialogPreview() {
    AppThemeSurface {
        AddOrEditBlockedNumberAlertDialog(
            alertDialogState = rememberAlertDialogState(),
            blockedNumber = null,
            deleteBlockedNumber = {}
        ) {}
    }
}
