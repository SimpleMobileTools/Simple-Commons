package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.commons.compose.alert_dialog.DialogSurface
import com.simplemobiletools.commons.compose.alert_dialog.ShowKeyboardWhenDialogIsOpenedAndRequestFocus
import com.simplemobiletools.commons.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.commons.compose.components.RadioGroupDialogComponent
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface
import com.simplemobiletools.commons.compose.theme.SimpleTheme
import com.simplemobiletools.commons.databinding.DialogCustomIntervalPickerBinding
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DAY_SECONDS
import com.simplemobiletools.commons.helpers.HOUR_SECONDS
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import kotlinx.collections.immutable.toImmutableList

class CustomIntervalPickerDialog(val activity: Activity, val selectedSeconds: Int = 0, val showSeconds: Boolean = false, val callback: (minutes: Int) -> Unit) {
    private var dialog: AlertDialog? = null
    private var view = DialogCustomIntervalPickerBinding.inflate(activity.layoutInflater, null, false)

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> confirmReminder() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(view.dialogCustomIntervalValue)
                }
            }

        view.apply {
            dialogRadioSeconds.beVisibleIf(showSeconds)
            when {
                selectedSeconds == 0 -> dialogRadioView.check(R.id.dialog_radio_minutes)
                selectedSeconds % DAY_SECONDS == 0 -> {
                    dialogRadioView.check(R.id.dialog_radio_days)
                    dialogCustomIntervalValue.setText((selectedSeconds / DAY_SECONDS).toString())
                }

                selectedSeconds % HOUR_SECONDS == 0 -> {
                    dialogRadioView.check(R.id.dialog_radio_hours)
                    dialogCustomIntervalValue.setText((selectedSeconds / HOUR_SECONDS).toString())
                }

                selectedSeconds % MINUTE_SECONDS == 0 -> {
                    dialogRadioView.check(R.id.dialog_radio_minutes)
                    dialogCustomIntervalValue.setText((selectedSeconds / MINUTE_SECONDS).toString())
                }

                else -> {
                    dialogRadioView.check(R.id.dialog_radio_seconds)
                    dialogCustomIntervalValue.setText(selectedSeconds.toString())
                }
            }

            dialogCustomIntervalValue.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.performClick()
                        return true
                    }

                    return false
                }
            })
        }
    }

    private fun confirmReminder() {
        val value = view.dialogCustomIntervalValue.value
        val multiplier = getMultiplier(view.dialogRadioView.checkedRadioButtonId)
        val minutes = Integer.valueOf(value.ifEmpty { "0" })
        callback(minutes * multiplier)
        activity.hideKeyboard()
        dialog?.dismiss()
    }

    private fun getMultiplier(id: Int) = when (id) {
        R.id.dialog_radio_days -> DAY_SECONDS
        R.id.dialog_radio_hours -> HOUR_SECONDS
        R.id.dialog_radio_minutes -> MINUTE_SECONDS
        else -> 1
    }
}

@Composable
fun CustomIntervalPickerAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    selectedSeconds: Int = 0,
    showSeconds: Boolean = false,
    callback: (minutes: Int) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var textFieldValue by remember {
        mutableStateOf(initialTextFieldValue(selectedSeconds))
    }

    val context = LocalContext.current
    val selections = remember {
        buildCustomIntervalEntries(context, showSeconds)
    }
    val initiallySelected = remember {
        initialSelection(selectedSeconds, context)
    }

    val (selected, setSelected) = remember { mutableStateOf(initiallySelected) }

    AlertDialog(
        modifier = modifier.fillMaxWidth(0.95f),
        onDismissRequest = alertDialogState::hide,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        DialogSurface {
            Box {
                Column(
                    modifier = modifier
                        .padding(bottom = 64.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = SimpleTheme.dimens.padding.extraLarge,
                                start = SimpleTheme.dimens.padding.extraLarge,
                                end = SimpleTheme.dimens.padding.extraLarge
                            )
                            .focusRequester(focusRequester),
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            if (newValue.text.length <= 5) textFieldValue = newValue
                        },
                        label = {
                            Text(text = stringResource(id = R.string.value))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        maxLines = 1
                    )

                    RadioGroupDialogComponent(
                        items = selections,
                        selected = selected,
                        setSelected = setSelected,
                        modifier = Modifier.padding(
                            vertical = SimpleTheme.dimens.padding.extraLarge,
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = SimpleTheme.dimens.padding.extraLarge,
                            bottom = SimpleTheme.dimens.padding.extraLarge,
                            end = SimpleTheme.dimens.padding.extraLarge
                        )
                        .align(Alignment.BottomStart)
                ) {
                    TextButton(onClick = alertDialogState::hide) {
                        Text(text = stringResource(id = R.string.cancel))
                    }

                    TextButton(onClick = {
                        val multiplier = getMultiplier(context, selected)
                        val minutes = Integer.valueOf(textFieldValue.text.ifEmpty { "0" })
                        callback(minutes * multiplier)
                        alertDialogState.hide()
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
    ShowKeyboardWhenDialogIsOpenedAndRequestFocus(focusRequester = focusRequester)
}

private fun initialSelection(selectedSeconds: Int, context: Context) = requireNotNull(
    when {
        selectedSeconds == 0 -> minutesRaw(context)
        selectedSeconds % DAY_SECONDS == 0 -> daysRaw(context)
        selectedSeconds % HOUR_SECONDS == 0 -> hoursRaw(context)
        selectedSeconds % MINUTE_SECONDS == 0 -> minutesRaw(context)
        else -> secondsRaw(context)

    }
) {
    "Incorrect format, please check selections"
}

private fun initialTextFieldValue(selectedSeconds: Int) = when {
    selectedSeconds == 0 -> TextFieldValue("")
    selectedSeconds % DAY_SECONDS == 0 -> {
        val text = (selectedSeconds / DAY_SECONDS).toString()
        textFieldValueAndSelection(text)
    }

    selectedSeconds % HOUR_SECONDS == 0 -> {
        val text = (selectedSeconds / HOUR_SECONDS).toString()
        textFieldValueAndSelection(text)
    }

    selectedSeconds % MINUTE_SECONDS == 0 -> {
        val text = (selectedSeconds / MINUTE_SECONDS).toString()
        textFieldValueAndSelection(text)
    }

    else -> {
        val text = selectedSeconds.toString()
        textFieldValueAndSelection(text)
    }
}

private fun textFieldValueAndSelection(text: String) = TextFieldValue(text = text, selection = TextRange(text.length))

fun buildCustomIntervalEntries(context: Context, showSeconds: Boolean) =
    buildList {
        if (showSeconds) {
            add(secondsRaw(context))
        }
        add(minutesRaw(context))
        add(hoursRaw(context))
        add(daysRaw(context))
    }.toImmutableList()

private fun daysRaw(context: Context) = context.getString(R.string.days_raw)
private fun hoursRaw(context: Context) = context.getString(R.string.hours_raw)
private fun secondsRaw(context: Context) = context.getString(R.string.seconds_raw)
private fun minutesRaw(context: Context) = context.getString(R.string.minutes_raw)

private fun getMultiplier(context: Context, text: String) = when (text) {
    daysRaw(context) -> DAY_SECONDS
    hoursRaw(context) -> HOUR_SECONDS
    minutesRaw(context) -> MINUTE_SECONDS
    else -> 1
}

@Composable
@MyDevices
private fun CustomIntervalPickerAlertDialogPreview() {
    AppThemeSurface {
        CustomIntervalPickerAlertDialog(alertDialogState = rememberAlertDialogState(),
            selectedSeconds = 0,
            showSeconds = true,
            callback = {}
        )
    }
}

