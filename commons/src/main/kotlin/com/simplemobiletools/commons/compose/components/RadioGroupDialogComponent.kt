package com.simplemobiletools.commons.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simplemobiletools.commons.compose.extensions.MyDevices
import com.simplemobiletools.commons.compose.theme.AppThemeSurface

@Composable
fun RadioGroupDialogComponent(
    modifier: Modifier = Modifier,
    items: List<String>,
    selected: String?,
    verticalPadding: Dp = 10.dp,
    horizontalPadding: Dp = 20.dp,
    setSelected: (selected: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        items.forEach { item ->
            RadioButtonDialogComponent(
                setSelected = setSelected,
                item = item,
                selected = selected,
                modifier = Modifier.padding(vertical = verticalPadding, horizontal = horizontalPadding)
            )
        }
    }
}

@Composable
@MyDevices
private fun RadioGroupDialogComponentPreview() {
    AppThemeSurface {
        RadioGroupDialogComponent(items = listOf(
            "Test 1",
            "Test 2",
            "Test 3",
            "Test 4",
            "Test 5",
            "Test 6",
        ), selected = null, setSelected = {})
    }
}
