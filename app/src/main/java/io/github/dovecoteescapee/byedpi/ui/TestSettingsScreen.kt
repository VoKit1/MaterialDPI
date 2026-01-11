package io.github.dovecoteescapee.byedpi.ui

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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.ui.components.*
import io.github.dovecoteescapee.byedpi.ui.viewmodel.TestSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestSettingsScreen(
    viewModel: TestSettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
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
                .padding(padding)
        ) {
            item {
                PreferenceCategory(title = stringResource(R.string.byedpi_category))

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

                EditTextPreference(
                    title = stringResource(R.string.test_settings_domains),
                    value = viewModel.domains,
                    onValueChange = { viewModel.updateDomains(it) },
                    enabled = viewModel.domainLists.contains("custom"),
                    icon = Icons.Default.Edit
                )

                SwitchPreference(
                    title = stringResource(R.string.test_settings_usercommands),
                    checked = viewModel.userCommandsEnabled,
                    onCheckedChange = { viewModel.updateUserCommandsEnabled(it) },
                    icon = Icons.Default.Terminal
                )

                EditTextPreference(
                    title = stringResource(R.string.test_settings_commands),
                    value = viewModel.commands,
                    onValueChange = { viewModel.updateCommands(it) },
                    enabled = viewModel.userCommandsEnabled,
                    icon = Icons.Default.Code
                )
            }
        }
    }
}
