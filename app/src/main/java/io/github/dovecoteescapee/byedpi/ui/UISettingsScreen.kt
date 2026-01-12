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
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.core.ByeDpiProxyUIPreferences.DesyncMethod.*
import io.github.dovecoteescapee.byedpi.core.ByeDpiProxyUIPreferences.HostsMode.*
import io.github.dovecoteescapee.byedpi.ui.components.*
import io.github.dovecoteescapee.byedpi.ui.viewmodel.UISettingsViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UISettingsScreen(
    viewModel: UISettingsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isTv = remember { context.isTv() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_editor)) },
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
                SettingsCard(title = stringResource(R.string.byedpi_readme_link)) {
                    PreferenceItem(
                        title = stringResource(R.string.byedpi_readme_link),
                        onClick = { uriHandler.openUri("https://github.com/hufrea/byedpi/blob/v0.13/README.md") },
                        icon = Icons.Default.Description
                    )
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.byedpi_proxy)) {
                    EditTextPreference(
                        title = stringResource(R.string.byedpi_max_connections_setting),
                        value = viewModel.maxConnections,
                        onValueChange = { viewModel.updateMaxConnections(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Numbers
                    )

                    EditTextPreference(
                        title = stringResource(R.string.byedpi_buffer_size_setting),
                        value = viewModel.bufferSize,
                        onValueChange = { viewModel.updateBufferSize(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Storage
                    )

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_no_domain_setting),
                        checked = viewModel.noDomain,
                        onCheckedChange = { viewModel.updateNoDomain(it) },
                        icon = Icons.Default.DomainDisabled
                    )

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_tcp_fast_open_setting),
                        checked = viewModel.tcpFastOpen,
                        onCheckedChange = { viewModel.updateTcpFastOpen(it) },
                        icon = Icons.Default.Speed
                    )
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.byedpi_desync)) {
                    val hostsModes = stringArrayResource(R.array.byedpi_hosts_modes)
                    val hostsModeValues = stringArrayResource(R.array.byedpi_hosts_modes_entries)
                    val hostsModeMap = hostsModeValues.zip(hostsModes).toMap()

                    ListPreference(
                        title = stringResource(R.string.byedpi_hosts_mode_setting),
                        value = viewModel.hostsMode.toString().lowercase(),
                        entries = hostsModeMap,
                        onValueChange = { viewModel.updateHostsMode(it) },
                        icon = Icons.Default.Security
                    )

                    AnimatedVisibility(
                        visible = viewModel.hostsMode == Blacklist,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ListEditPreference(
                            title = stringResource(R.string.byedpi_hosts_blacklist_setting),
                            placeholder = stringResource(R.string.some_domain),
                            values = viewModel.hostsBlacklist,
                            onValuesChange = { viewModel.updateHostsBlacklist(it) },
                            icon = Icons.Default.Block
                        )
                    }

                    AnimatedVisibility(
                        visible = viewModel.hostsMode == Whitelist,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        ListEditPreference(
                            title = stringResource(R.string.byedpi_hosts_whitelist_setting),
                            placeholder = stringResource(R.string.some_domain),
                            values = viewModel.hostsWhitelist,
                            onValuesChange = { viewModel.updateHostsWhitelist(it) },
                            icon = Icons.Default.Checklist
                        )
                    }

                    EditTextPreference(
                        title = stringResource(R.string.byedpi_default_ttl_setting),
                        value = viewModel.defaultTtl,
                        onValueChange = { viewModel.updateDefaultTtl(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        icon = Icons.Default.Timer
                    )

                    val desyncMethods = stringArrayResource(R.array.byedpi_desync_methods)
                    val desyncMethodValues = stringArrayResource(R.array.byedpi_desync_methods_entries)
                    val desyncMethodMap = desyncMethodValues.zip(desyncMethods).toMap()

                    ListPreference(
                        title = stringResource(R.string.byedpi_desync_method_setting),
                        value = viewModel.desyncMethod.toString().lowercase(),
                        entries = desyncMethodMap,
                        onValueChange = { viewModel.updateDesyncMethod(it) },
                        icon = Icons.Default.SyncAlt
                    )

                    val desyncEnabled = viewModel.desyncMethod != None
                    AnimatedVisibility(
                        visible = desyncEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            EditTextPreference(
                                title = stringResource(R.string.byedpi_split_position_setting),
                                value = viewModel.splitPosition,
                                onValueChange = { viewModel.updateSplitPosition(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                icon = Icons.AutoMirrored.Filled.CallSplit
                            )

                            SwitchPreference(
                                title = stringResource(R.string.byedpi_split_at_host_setting),
                                checked = viewModel.splitAtHost,
                                onCheckedChange = { viewModel.updateSplitAtHost(it) },
                                icon = Icons.AutoMirrored.Filled.AltRoute
                            )
                        }
                    }

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_drop_sack_setting),
                        checked = viewModel.dropSack,
                        onCheckedChange = { viewModel.updateDropSack(it) },
                        icon = Icons.Default.DeleteSweep
                    )

                    val isFake = viewModel.desyncMethod == Fake
                    AnimatedVisibility(
                        visible = isFake,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            EditTextPreference(
                                title = stringResource(R.string.byedpi_fake_ttl_setting),
                                value = viewModel.fakeTtl,
                                onValueChange = { viewModel.updateFakeTtl(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                icon = Icons.Default.Timer
                            )

                            EditTextPreference(
                                title = stringResource(R.string.byedpi_fake_offset_setting),
                                value = viewModel.fakeOffset,
                                onValueChange = { viewModel.updateFakeOffset(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                icon = Icons.AutoMirrored.Filled.FormatIndentIncrease
                            )

                            EditTextPreference(
                                title = stringResource(R.string.sni_of_fake_packet),
                                value = viewModel.fakeSni,
                                onValueChange = { viewModel.updateFakeSni(it) },
                                icon = Icons.Default.Dns
                            )
                        }
                    }

                    val isOob = viewModel.desyncMethod == OOB || viewModel.desyncMethod == DISOOB
                    AnimatedVisibility(
                        visible = isOob,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        EditTextPreference(
                            title = stringResource(R.string.oob_data),
                            value = viewModel.oobData,
                            onValueChange = { viewModel.updateOobData(it) },
                            icon = Icons.Default.DataArray
                        )
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.byedpi_protocols_category)) {
                    SwitchPreference(
                        title = stringResource(R.string.desync_http),
                        checked = viewModel.desyncHttp,
                        onCheckedChange = { viewModel.updateDesyncHttp(it) },
                        icon = Icons.Default.Http
                    )

                    SwitchPreference(
                        title = stringResource(R.string.desync_https),
                        checked = viewModel.desyncHttps,
                        onCheckedChange = { viewModel.updateDesyncHttps(it) },
                        icon = Icons.Default.Https
                    )

                    SwitchPreference(
                        title = stringResource(R.string.desync_udp),
                        checked = viewModel.desyncUdp,
                        onCheckedChange = { viewModel.updateDesyncUdp(it) },
                        icon = Icons.AutoMirrored.Filled.DirectionsRun
                    )
                }
            }

            val desyncAllProtocols = !viewModel.desyncHttp && !viewModel.desyncHttps && !viewModel.desyncUdp
            
            item {
                SettingsCard(title = stringResource(R.string.desync_http_category)) {
                    val httpEnabled = desyncAllProtocols || viewModel.desyncHttp

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_host_mixed_case_setting),
                        checked = viewModel.hostMixedCase,
                        onCheckedChange = { viewModel.updateHostMixedCase(it) },
                        enabled = httpEnabled,
                        icon = Icons.Default.TextFields
                    )

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_domain_mixed_case_setting),
                        checked = viewModel.domainMixedCase,
                        onCheckedChange = { viewModel.updateDomainMixedCase(it) },
                        enabled = httpEnabled,
                        icon = Icons.Default.Abc
                    )

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_host_remove_spaces_setting),
                        checked = viewModel.hostRemoveSpaces,
                        onCheckedChange = { viewModel.updateHostRemoveSpaces(it) },
                        enabled = httpEnabled,
                        icon = Icons.Default.SpaceBar
                    )
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.desync_https_category)) {
                    val httpsEnabled = desyncAllProtocols || viewModel.desyncHttps

                    SwitchPreference(
                        title = stringResource(R.string.byedpi_tlsrec_enabled_setting),
                        checked = viewModel.tlsRecEnabled,
                        onCheckedChange = { viewModel.updateTlsRecEnabled(it) },
                        enabled = httpsEnabled,
                        icon = Icons.Default.Lock
                    )

                    AnimatedVisibility(
                        visible = viewModel.tlsRecEnabled && httpsEnabled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            EditTextPreference(
                                title = stringResource(R.string.byedpi_tlsrec_position_setting),
                                value = viewModel.tlsRecPosition,
                                onValueChange = { viewModel.updateTlsRecPosition(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                icon = Icons.Default.Place
                            )

                            SwitchPreference(
                                title = stringResource(R.string.byedpi_tlsrec_at_sni_setting),
                                checked = viewModel.tlsRecAtSni,
                                onCheckedChange = { viewModel.updateTlsRecAtSni(it) },
                                icon = Icons.Default.LocationOn
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(title = stringResource(R.string.desync_udp_category)) {
                    val udpEnabled = desyncAllProtocols || viewModel.desyncUdp

                    EditTextPreference(
                        title = stringResource(R.string.byedpi_udp_fake_count),
                        value = viewModel.udpFakeCount,
                        onValueChange = { viewModel.updateUdpFakeCount(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        enabled = udpEnabled,
                        icon = Icons.Default.Repeat
                    )
                }
            }
        }
    }
}
