package io.github.dovecoteescapee.byedpi.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onPrepareVpn: (android.content.Intent) -> Unit,
    onOpenSettings: () -> Unit,
    onSaveLogs: () -> Unit,
    onCloseApp: () -> Unit,
    onOpenEditor: () -> Unit
) {
    val status = viewModel.currentStatus
    val mode = viewModel.currentMode
    val preferredMode = viewModel.preferredMode
    val (ip, port) = viewModel.proxyAddress
    
    var showMenu by remember { mutableStateOf(false) }
    val isRunning = status == AppStatus.Running

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.close_app)) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onCloseApp()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Status Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                val buttonScale by animateFloatAsState(
                    targetValue = if (isRunning) 1.1f else 1f,
                    animationSpec = tween(500), 
                    label = "scale"
                )
                
                val buttonColor by animateColorAsState(
                    targetValue = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = tween(500),
                    label = "color"
                )

                val iconColor by animateColorAsState(
                    targetValue = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(500),
                    label = "iconColor"
                )

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(buttonScale)
                        .clip(CircleShape)
                        .background(buttonColor)
                        .clickable(
                            enabled = viewModel.isClickable,
                            onClick = { viewModel.toggleService(onPrepareVpn) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = iconColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status Text
            val statusTextRes = when (status) {
                AppStatus.Halted -> if (preferredMode == Mode.VPN) R.string.vpn_disconnected else R.string.proxy_down
                AppStatus.Running -> if (mode == Mode.VPN) R.string.vpn_connected else R.string.proxy_up
            }

            Text(
                text = stringResource(statusTextRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            if (isRunning && mode == Mode.Proxy) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.proxy_address, ip, port),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Quick Actions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.quick_actions),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QuickActionButton(
                            icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                            label = if (preferredMode == Mode.VPN) stringResource(R.string.vpn_mode) else stringResource(R.string.proxy_mode),
                            onClick = { 
                                val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                                viewModel.setMode(newMode)
                            }
                        )
                        QuickActionButton(
                            icon = if (viewModel.isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                            label = stringResource(R.string.editor),
                            onClick = onOpenEditor
                        )
                        QuickActionButton(
                            icon = Icons.Default.Settings,
                            label = stringResource(R.string.settings),
                            onClick = onOpenSettings
                        )
                        QuickActionButton(
                            icon = Icons.Outlined.BugReport,
                            label = stringResource(R.string.save_logs),
                            onClick = onSaveLogs
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
