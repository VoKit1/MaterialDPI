package io.github.dovecoteescapee.byedpi.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PreferenceCategory(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun PreferenceItem(
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 16.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                if (summary != null) {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }
            if (trailing != null) {
                Box(modifier = Modifier.padding(start = 16.dp)) {
                    trailing()
                }
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    PreferenceItem(
        title = title,
        summary = summary,
        enabled = enabled,
        icon = icon,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    )
}

@Composable
fun ListPreference(
    title: String,
    value: String,
    entries: Map<String, String>,
    onValueChange: (String) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    PreferenceItem(
        title = title,
        summary = summary ?: entries[value],
        enabled = enabled,
        icon = icon,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(Modifier.selectableGroup()) {
                    entries.forEach { (entryValue, entryLabel) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (entryValue == value),
                                    onClick = {
                                        onValueChange(entryValue)
                                        showDialog = false
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (entryValue == value),
                                onClick = null
                            )
                            Text(
                                text = entryLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EditTextPreference(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempValue by remember(value) { mutableStateOf(value) }

    PreferenceItem(
        title = title,
        summary = summary ?: value,
        enabled = enabled,
        icon = icon,
        onClick = { 
            tempValue = value
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = tempValue,
                    onValueChange = { tempValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = keyboardOptions,
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(tempValue)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MultiSelectListPreference(
    title: String,
    values: Set<String>,
    entries: Map<String, String>,
    onValuesChange: (Set<String>) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    var tempValues by remember(values) { mutableStateOf(values) }

    PreferenceItem(
        title = title,
        summary = summary ?: values.joinToString(", ") { entries[it] ?: it },
        enabled = enabled,
        icon = icon,
        onClick = { 
            tempValues = values
            showDialog = true 
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                Column(Modifier.selectableGroup()) {
                    entries.forEach { (entryValue, entryLabel) ->
                        val isSelected = tempValues.contains(entryValue)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        tempValues = if (isSelected) {
                                            tempValues - entryValue
                                        } else {
                                            tempValues + entryValue
                                        }
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                            Text(
                                text = entryLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onValuesChange(tempValues)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
