package io.github.dovecoteescapee.byedpi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.utility.isTv

@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp)
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardDefaults.shape)
            ) {
                content()
            }
        }
    }
}

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
    modifier: Modifier = Modifier,
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    trailing: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        color = androidx.compose.ui.graphics.Color.Transparent
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
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    PreferenceItem(
        modifier = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPreference(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    entries: Map<String, String>,
    onValueChange: (String) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    var showDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    PreferenceItem(
        modifier = modifier,
        title = title,
        summary = summary ?: entries[value],
        enabled = enabled,
        icon = icon,
        onClick = { showDialog = true }
    )

    if (showDialog) {
        if (isTv) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(title) },
                text = {
                    Column(modifier = Modifier.selectableGroup()) {
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
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .selectableGroup()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    )
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
                                .padding(horizontal = 24.dp),
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTextPreference(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String = "",
    value: String,
    onValueChange: (String) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    var showDialog by remember { mutableStateOf(false) }
    var tempValue by remember(value) { mutableStateOf(value) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    PreferenceItem(
        modifier = modifier,
        title = title,
        summary = value.ifBlank { null },
        enabled = enabled,
        icon = icon,
        onClick = { 
            tempValue = value
            showDialog = true 
        }
    )

    if (showDialog) {
        if (isTv) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(title) },
                text = {
                    OutlinedTextField(
                        value = tempValue,
                        placeholder = { Text(placeholder) },
                        onValueChange = { tempValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        onValueChange(tempValue)
                        showDialog = false
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                    )
                    OutlinedTextField(
                        value = tempValue,
                        placeholder = { Text(placeholder) },
                        onValueChange = { tempValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = keyboardOptions,
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                onValueChange(tempValue)
                                showDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectListPreference(
    modifier: Modifier = Modifier,
    title: String,
    values: Set<String>,
    entries: Map<String, String>,
    onValuesChange: (Set<String>) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    var showDialog by remember { mutableStateOf(false) }
    var tempValues by remember(values) { mutableStateOf(values) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    PreferenceItem(
        modifier = modifier,
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
        if (isTv) {
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
                    Button(onClick = {
                        onValuesChange(tempValues)
                        showDialog = false
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    )
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
                                    .padding(horizontal = 24.dp),
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                onValuesChange(tempValues)
                                showDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListEditPreference(
    modifier: Modifier = Modifier,
    title: String,
    placeholder: String = "",
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    splitByLinesOnly: Boolean = false
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    var showDialog by remember { mutableStateOf(false) }
    var tempValues by remember(values) { mutableStateOf(values) }
    var newItemValue by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val onAdd = {
        if (newItemValue.isNotBlank()) {
            val newItems = if (splitByLinesOnly) {
                newItemValue.lines().map { it.trim() }.filter { it.isNotBlank() }
            } else {
                newItemValue.split(Regex("[\\s,]+")).filter { it.isNotBlank() }
            }
            tempValues = tempValues + newItems
            newItemValue = ""
        }
    }

    PreferenceItem(
        modifier = modifier,
        title = title,
        summary = summary ?: if (values.isEmpty()) stringResource(R.string.empty) else values.joinToString(", "),
        enabled = enabled,
        icon = icon,
        onClick = {
            tempValues = values
            showDialog = true
        }
    )

    if (showDialog) {
        if (isTv) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(title) },
                text = {
                    Column(modifier = Modifier.width(400.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newItemValue,
                                onValueChange = { newItemValue = it },
                                modifier = Modifier.weight(1f),
                                label = { Text(stringResource(R.string.add)) },
                                placeholder = { Text(placeholder)},
                                singleLine = !splitByLinesOnly,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { onAdd() })
                            )
                            IconButton(onClick = onAdd) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            itemsIndexed(tempValues) { index, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        tempValues = tempValues.toMutableList().apply { removeAt(index) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        onValuesChange(tempValues)
                        showDialog = false
                    }) {
                        Text(stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showDialog = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemValue,
                            onValueChange = { newItemValue = it },
                            modifier = Modifier.weight(1f),
                            label = { Text(stringResource(R.string.add)) },
                            placeholder = { Text(placeholder)},
                            singleLine = !splitByLinesOnly,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { onAdd() })
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onAdd,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        itemsIndexed(tempValues) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = {
                                    tempValues = tempValues.toMutableList().apply { removeAt(index) }
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                onValuesChange(tempValues)
                                showDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.ok))
                        }
                    }
                }
            }
        }
    }
}
