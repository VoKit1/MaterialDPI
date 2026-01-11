package io.github.dovecoteescapee.byedpi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Command
import io.github.dovecoteescapee.byedpi.ui.components.EditTextPreference
import io.github.dovecoteescapee.byedpi.ui.components.PreferenceCategory
import io.github.dovecoteescapee.byedpi.ui.components.PreferenceItem
import io.github.dovecoteescapee.byedpi.ui.viewmodel.CmdSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmdSettingsScreen(
    viewModel: CmdSettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    var showActionDialog by remember { mutableStateOf<Command?>(null) }
    var showRenameDialog by remember { mutableStateOf<Command?>(null) }
    var showEditDialog by remember { mutableStateOf<Command?>(null) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.command_line_editor)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                EditTextPreference(
                    title = stringResource(R.string.command_line_arguments),
                    value = viewModel.cmdArgs,
                    onValueChange = { viewModel.updateCmdArgs(it) },
                    icon = Icons.Default.Terminal
                )

                PreferenceItem(
                    title = stringResource(R.string.cmd_args_clear),
                    onClick = { viewModel.clearCmdArgs() },
                    icon = Icons.Default.Delete
                )
            }

            if (viewModel.history.isNotEmpty()) {
                item {
                    PreferenceCategory(title = stringResource(R.string.cmd_history_title))
                    PreferenceItem(
                        title = stringResource(R.string.cmd_history_delete_all),
                        summary = stringResource(R.string.cmd_history_title_summary),
                        onClick = { showClearHistoryDialog = true },
                        icon = Icons.Default.ClearAll
                    )
                }

                val sortedHistory = viewModel.history.sortedWith(
                    compareByDescending<Command> { it.pinned }
                        .thenBy { viewModel.history.indexOf(it) }
                )

                items(sortedHistory) { command ->
                    val summary = buildString {
                        if (command.name != null) append(command.name)
                        if (command.pinned) {
                            if (isNotEmpty()) append(" - ")
                            append(stringResource(R.string.cmd_history_pinned))
                        }
                    }
                    PreferenceItem(
                        title = command.text,
                        summary = summary.ifEmpty { null },
                        onClick = { showActionDialog = command },
                        icon = if (command.pinned) Icons.Default.PushPin else Icons.Default.History,
                        trailing = {
                            IconButton(onClick = {
                                if (command.pinned) viewModel.unpinCommand(command.text)
                                else viewModel.pinCommand(command.text)
                            }) {
                                Icon(
                                    if (command.pinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = null,
                                    tint = if (command.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.cmd_history_menu)) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.cmd_history_delete_unpinned)) },
                        modifier = Modifier.clickable {
                            viewModel.clearUnpinnedHistory()
                            showClearHistoryDialog = false
                        }
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.cmd_history_delete_all)) },
                        modifier = Modifier.clickable {
                            viewModel.clearAllHistory()
                            showClearHistoryDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    showActionDialog?.let { command ->
        val renameLabel = stringResource(R.string.cmd_history_rename)
        val editLabel = stringResource(R.string.cmd_history_edit)

        AlertDialog(
            onDismissRequest = { showActionDialog = null },
            title = { Text(stringResource(R.string.cmd_history_menu)) },
            text = {
                Column {
                    val actions = listOf(
                        Triple(stringResource(R.string.cmd_history_apply), Icons.Default.Terminal) {
                            viewModel.updateCmdArgs(command.text)
                        },
                        Triple(renameLabel, Icons.Default.Edit) {
                            showRenameDialog = command
                        },
                        Triple(editLabel, Icons.Default.Edit) {
                            showEditDialog = command
                        },
                        Triple(stringResource(R.string.cmd_history_copy), Icons.Default.ContentCopy) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Command", command.text))
                        },
                        Triple(stringResource(R.string.cmd_history_delete), Icons.Default.Delete) {
                            viewModel.deleteCommand(command.text)
                        }
                    )
                    actions.forEach { (label, icon, action) ->
                        ListItem(
                            headlineContent = { Text(label) },
                            leadingContent = { Icon(icon, contentDescription = null) },
                            modifier = Modifier.clickable {
                                action()
                                if (label != renameLabel && label != editLabel) {
                                    showActionDialog = null
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionDialog = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    showRenameDialog?.let { command ->
        var newName by remember { mutableStateOf(command.name ?: "") }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text(stringResource(R.string.cmd_history_rename)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.cmd_history_rename)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameCommand(command.text, newName)
                    showRenameDialog = null
                    showActionDialog = null
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }

    showEditDialog?.let { command ->
        var newText by remember { mutableStateOf(command.text) }
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text(stringResource(R.string.cmd_history_edit)) },
            text = {
                OutlinedTextField(
                    value = newText,
                    onValueChange = { newText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.command_line_arguments)) }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editCommand(command.text, newText)
                    showEditDialog = null
                    showActionDialog = null
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}
