package io.github.dovecoteescapee.byedpi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.Command
import io.github.dovecoteescapee.byedpi.ui.components.EditTextPreference
import io.github.dovecoteescapee.byedpi.ui.components.PreferenceItem
import io.github.dovecoteescapee.byedpi.ui.components.SettingsCard
import io.github.dovecoteescapee.byedpi.ui.viewmodel.CmdSettingsViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CmdSettingsScreen(
    viewModel: CmdSettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    
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
                .padding(padding),
            contentPadding = PaddingValues(if (isTv) 48.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(title = stringResource(R.string.command_line_arguments)) {
                    EditTextPreference(
                        title = stringResource(R.string.command_line_arguments),
                        placeholder = stringResource(R.string.some_strategy),
                        value = viewModel.cmdArgs,
                        onValueChange = { viewModel.updateCmdArgs(it) },
                        icon = Icons.Default.Terminal
                    )

                    PreferenceItem(
                        title = stringResource(R.string.cmd_args_paste),
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = clipboard.primaryClip
                            if (clip != null && clip.itemCount > 0) {
                                val text = clip.getItemAt(0).text
                                if (text != null) {
                                    viewModel.updateCmdArgs(text.toString())
                                }
                            }
                        },
                        icon = Icons.Default.ContentPaste
                    )

                    PreferenceItem(
                        title = stringResource(R.string.cmd_args_clear),
                        onClick = { viewModel.clearCmdArgs() },
                        icon = Icons.Default.Delete
                    )
                }
            }

            if (viewModel.history.isNotEmpty()) {
                item {
                    SettingsCard(title = stringResource(R.string.cmd_history_title)) {
                        PreferenceItem(
                            title = stringResource(R.string.cmd_history_delete_all),
                            summary = stringResource(R.string.cmd_history_title_summary),
                            onClick = { showClearHistoryDialog = true },
                            icon = Icons.Default.ClearAll
                        )

                        val sortedHistory = viewModel.history.sortedWith(
                            compareByDescending<Command> { it.pinned }
                                .thenBy { viewModel.history.indexOf(it) }
                        )

                        sortedHistory.forEach { command ->
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
        }
    }

    // Dialogs
    if (showClearHistoryDialog) {
        if (isTv) {
            AlertDialog(
                onDismissRequest = { showClearHistoryDialog = false },
                title = { Text(stringResource(R.string.cmd_history_menu)) },
                text = {
                    Column {
                        TextButton(
                            onClick = {
                                viewModel.clearUnpinnedHistory()
                                showClearHistoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.cmd_history_delete_unpinned))
                        }
                        TextButton(
                            onClick = {
                                viewModel.clearAllHistory()
                                showClearHistoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.cmd_history_delete_all))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showClearHistoryDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showClearHistoryDialog = false },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = stringResource(R.string.cmd_history_menu),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    )
                    Surface(
                        onClick = {
                            viewModel.clearUnpinnedHistory()
                            showClearHistoryDialog = false
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cmd_history_delete_unpinned)) }
                        )
                    }
                    Surface(
                        onClick = {
                            viewModel.clearAllHistory()
                            showClearHistoryDialog = false
                        },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        ListItem(
                            headlineContent = { Text(stringResource(R.string.cmd_history_delete_all)) }
                        )
                    }
                }
            }
        }
    }

    showActionDialog?.let { command ->
        val renameLabel = stringResource(R.string.cmd_history_rename)
        val editLabel = stringResource(R.string.cmd_history_edit)

        if (isTv) {
            AlertDialog(
                onDismissRequest = { showActionDialog = null },
                title = { Text(stringResource(R.string.cmd_history_menu)) },
                text = {
                    Column {
                        val actions = mutableListOf(
                            Triple(stringResource(R.string.cmd_history_apply), Icons.Default.Terminal) {
                                viewModel.updateCmdArgs(command.text)
                            }
                        )
                        
                        if (!command.pinned) {
                            actions.add(Triple(stringResource(R.string.profiles_add), Icons.Default.Add) {
                                viewModel.pinCommand(command.text)
                            })
                        }

                        actions.addAll(listOf(
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
                        ))

                        actions.forEach { (label, icon, action) ->
                            TextButton(
                                onClick = {
                                    action()
                                    if (label != renameLabel && label != editLabel) {
                                        showActionDialog = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(icon, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showActionDialog = null }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                }
            )
        } else {
            ModalBottomSheet(
                onDismissRequest = { showActionDialog = null },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = stringResource(R.string.cmd_history_menu),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    )
                    val actions = mutableListOf(
                        Triple(stringResource(R.string.cmd_history_apply), Icons.Default.Terminal) {
                            viewModel.updateCmdArgs(command.text)
                        }
                    )

                    if (!command.pinned) {
                        actions.add(Triple(stringResource(R.string.profiles_add), Icons.Default.Add) {
                            viewModel.pinCommand(command.text)
                        })
                    }

                    actions.addAll(listOf(
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
                    ))

                    actions.forEach { (label, icon, action) ->
                        Surface(
                            onClick = {
                                action()
                                if (label != renameLabel && label != editLabel) {
                                    showActionDialog = null
                                }
                            },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            ListItem(
                                headlineContent = { Text(label) },
                                leadingContent = { Icon(icon, contentDescription = null) }
                            )
                        }
                    }
                }
            }
        }
    }

    showRenameDialog?.let { command ->
        var newName by remember { mutableStateOf(command.name ?: "") }
        if (isTv) {
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
                    Button(onClick = {
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
        } else {
            ModalBottomSheet(
                onDismissRequest = { showRenameDialog = null },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
                    Text(
                        text = stringResource(R.string.cmd_history_rename),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.cmd_history_rename)) },
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showRenameDialog = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                viewModel.renameCommand(command.text, newName)
                                showRenameDialog = null
                                showActionDialog = null
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

    showEditDialog?.let { command ->
        var newText by remember { mutableStateOf(command.text) }
        if (isTv) {
            AlertDialog(
                onDismissRequest = { showEditDialog = null },
                title = { Text(stringResource(R.string.cmd_history_edit)) },
                text = {
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.command_line_arguments)) },
                        placeholder = { Text(stringResource(R.string.some_strategy)) }
                    )
                },
                confirmButton = {
                    Button(onClick = {
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
        } else {
            ModalBottomSheet(
                onDismissRequest = { showEditDialog = null },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
                    Text(
                        text = stringResource(R.string.cmd_history_edit),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp, top = 16.dp)
                    )
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.command_line_arguments)) },
                        placeholder = { Text(stringResource(R.string.some_strategy)) }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { showEditDialog = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        ) {
                            Text(stringResource(android.R.string.cancel))
                        }
                        Button(
                            onClick = {
                                viewModel.editCommand(command.text, newText)
                                showEditDialog = null
                                showActionDialog = null
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
