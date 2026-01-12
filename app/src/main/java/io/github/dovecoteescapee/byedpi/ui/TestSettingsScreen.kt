package io.github.dovecoteescapee.byedpi.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.ui.components.*
import io.github.dovecoteescapee.byedpi.ui.viewmodel.TestSettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSettingsScreen(
    viewModel: TestSettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    var showAddDomainDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_test)) },
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsCard(title = stringResource(R.string.byedpi_proxy)) {
                    EditTextPreference(
                        title = stringResource(R.string.test_delay),
                        value = viewModel.delay,
                        onValueChange = { viewModel.updateDelay(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Timer
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_requests),
                        value = viewModel.requests,
                        onValueChange = { viewModel.updateRequests(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Repeat
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_timeout),
                        value = viewModel.timeout,
                        onValueChange = { viewModel.updateTimeout(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.HourglassEmpty
                    )

                    EditTextPreference(
                        title = stringResource(R.string.test_settings_sni),
                        value = viewModel.sni,
                        onValueChange = { viewModel.updateSni(it) },
                        icon = Icons.Default.Dns
                    )
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.test_settings_domain_lists)) {
                    val entries = stringArrayResource(R.array.domain_lists_entries)
                    val values = stringArrayResource(R.array.domain_lists_values)
                    val entryMap = values.zip(entries).toMap()

                    MultiSelectListPreference(
                        title = stringResource(R.string.test_settings_domain_lists),
                        values = viewModel.domainLists,
                        entries = entryMap,
                        onValuesChange = { viewModel.updateDomainLists(it) },
                        icon = Icons.AutoMirrored.Filled.List
                    )

                    if (viewModel.domainLists.contains("custom")) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        PreferenceItem(
                            title = stringResource(R.string.test_settings_domains),
                            summary = stringResource(R.string.open_editor),
                            icon = Icons.Default.Add,
                            onClick = { showAddDomainDialog = true }
                        )

                        viewModel.domainsList.forEach { domain ->
                            key(domain) {
                                DomainItem(
                                    domain = domain,
                                    onRemove = { viewModel.removeDomain(domain) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.test_settings_commands)) {
                    SwitchPreference(
                        title = stringResource(R.string.test_settings_usercommands),
                        checked = viewModel.userCommandsEnabled,
                        onCheckedChange = { viewModel.updateUserCommandsEnabled(it) },
                        icon = Icons.Default.Terminal
                    )

                    AnimatedVisibility(
                        visible = viewModel.userCommandsEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        EditTextPreference(
                            title = stringResource(R.string.test_settings_commands),
                            value = viewModel.commands,
                            onValueChange = { viewModel.updateCommands(it) },
                            icon = Icons.Default.Code
                        )
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.appearance_category)) {
                    SwitchPreference(
                        title = stringResource(R.string.test_settings_fulllog),
                        checked = viewModel.fullLog,
                        onCheckedChange = { viewModel.updateFullLog(it) },
                        icon = Icons.AutoMirrored.Filled.Notes
                    )

                    SwitchPreference(
                        title = stringResource(R.string.test_settings_logclickable),
                        checked = viewModel.logClickable,
                        onCheckedChange = { viewModel.updateLogClickable(it) },
                        icon = Icons.Default.TouchApp
                    )

                    SwitchPreference(
                        title = stringResource(R.string.test_settings_autosort),
                        checked = viewModel.autoSort,
                        onCheckedChange = { viewModel.updateAutoSort(it) },
                        icon = Icons.AutoMirrored.Filled.Sort
                    )
                }
            }
        }
    }

    if (showAddDomainDialog) {
        var domainText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDomainDialog = false },
            title = { Text(stringResource(R.string.test_settings_domains)) },
            text = {
                OutlinedTextField(
                    value = domainText,
                    onValueChange = { domainText = it },
                    label = { Text("Domain") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addDomain(domainText)
                    showAddDomainDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDomainDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun DomainItem(domain: String, onRemove: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ListItem(
            headlineContent = { Text(domain) },
            trailingContent = {
                IconButton(onClick = { visible = false }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }

    LaunchedEffect(visible) {
        if (!visible) {
            delay(300)
            onRemove()
        }
    }
}
