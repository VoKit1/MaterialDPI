package io.github.dovecoteescapee.byedpi.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppStatus
import io.github.dovecoteescapee.byedpi.data.Mode
import io.github.dovecoteescapee.byedpi.ui.viewmodel.MainViewModel
import io.github.dovecoteescapee.byedpi.utility.isTablet
import io.github.dovecoteescapee.byedpi.utility.isTv
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel(),
    onPrepareVpn: (Intent) -> Unit,
    onOpenSettings: () -> Unit,
    onSaveLogs: () -> Unit,
    onCloseApp: () -> Unit,
    onOpenEditor: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    val isTablet = remember { context.isTablet() }
    val status = viewModel.currentStatus
    val mode = viewModel.currentMode
    val preferredMode = viewModel.preferredMode
    val (ip, port) = viewModel.proxyAddress
    val profileName = viewModel.currentProfileName
    val uploadSpeed by viewModel.uploadSpeed.collectAsState()
    val downloadSpeed by viewModel.downloadSpeed.collectAsState()
    val totalUpload by viewModel.totalUpload.collectAsState()
    val totalDownload by viewModel.totalDownload.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isTrafficMonitoringEnabled = viewModel.isTrafficMonitoringEnabled

    var showMenu by remember { mutableStateOf(false) }
    val isRunning = status == AppStatus.Running

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { messageResId ->
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
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
                            text = { Text(stringResource(R.string.save_logs)) },
                            leadingIcon = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onSaveLogs()
                            }
                        )
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
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val actions = remember(preferredMode, viewModel.isCmdEnabled, isRunning) {
                listOf(
                    DashboardAction(
                        icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                        label = if (preferredMode == Mode.VPN) R.string.vpn_mode else R.string.proxy_mode,
                        onClick = {
                            val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                            viewModel.setMode(newMode)
                        },
                        enabled = !isRunning
                    ),
                    DashboardAction(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = R.string.profiles_title,
                        onClick = { viewModel.performActionIfStopped(onOpenProfiles) },
                        enabled = !isRunning
                    ),
                    DashboardAction(
                        icon = if (viewModel.isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                        label = R.string.editor,
                        onClick = { viewModel.performActionIfStopped(onOpenEditor) },
                        enabled = !isRunning
                    ),
                    DashboardAction(
                        icon = Icons.Default.Settings,
                        label = R.string.settings,
                        onClick = { viewModel.performActionIfStopped(onOpenSettings) },
                        enabled = !isRunning
                    )
                )
            }

            if (isTv || isTablet) {
                LargeScreenLayout(
                    isRunning = isRunning,
                    status = status,
                    mode = mode,
                    ip = ip,
                    port = port,
                    profileName = profileName,
                    isClickable = viewModel.isClickable,
                    onToggle = { viewModel.toggleService(onPrepareVpn) },
                    actions = actions,
                    isTv = isTv,
                    uploadSpeed = uploadSpeed,
                    downloadSpeed = downloadSpeed,
                    totalUpload = totalUpload,
                    totalDownload = totalDownload,
                    duration = duration,
                    isTrafficMonitoringEnabled = isTrafficMonitoringEnabled
                )
            } else {
                MobileLayout(
                    isRunning = isRunning,
                    status = status,
                    mode = mode,
                    ip = ip,
                    port = port,
                    profileName = profileName,
                    isClickable = viewModel.isClickable,
                    onToggle = { viewModel.toggleService(onPrepareVpn) },
                    actions = actions,
                    uploadSpeed = uploadSpeed,
                    downloadSpeed = downloadSpeed,
                    totalUpload = totalUpload,
                    totalDownload = totalDownload,
                    duration = duration,
                    isTrafficMonitoringEnabled = isTrafficMonitoringEnabled
                )
            }
        }
    }
}

data class DashboardAction(
    val icon: ImageVector,
    val label: Int,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

@Composable
fun LargeScreenLayout(
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    onToggle: () -> Unit,
    actions: List<DashboardAction>,
    isTv: Boolean,
    uploadSpeed: String,
    downloadSpeed: String,
    totalUpload: String,
    totalDownload: String,
    duration: String,
    isTrafficMonitoringEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            ConnectionHero(
                isRunning = isRunning,
                status = status,
                mode = mode,
                ip = ip,
                port = port,
                profileName = profileName,
                isClickable = isClickable,
                onToggle = onToggle,
                isTv = isTv,
                uploadSpeed = uploadSpeed,
                downloadSpeed = downloadSpeed,
                totalUpload = totalUpload,
                totalDownload = totalDownload,
                duration = duration,
                isTrafficMonitoringEnabled = isTrafficMonitoringEnabled
            )
        }

        Spacer(modifier = Modifier.width(48.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            ActionGrid(
                actions = actions,
                columns = 2,
                isTv = isTv
            )
        }
    }
}

@Composable
fun MobileLayout(
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    onToggle: () -> Unit,
    actions: List<DashboardAction>,
    uploadSpeed: String,
    downloadSpeed: String,
    totalUpload: String,
    totalDownload: String,
    duration: String,
    isTrafficMonitoringEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        ConnectionHero(
            isRunning = isRunning,
            status = status,
            mode = mode,
            ip = ip,
            port = port,
            profileName = profileName,
            isClickable = isClickable,
            onToggle = onToggle,
            isTv = false,
            uploadSpeed = uploadSpeed,
            downloadSpeed = downloadSpeed,
            totalUpload = totalUpload,
            totalDownload = totalDownload,
            duration = duration,
            isTrafficMonitoringEnabled = isTrafficMonitoringEnabled
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(32.dp))

        ActionGrid(
            actions = actions,
            columns = 2,
            isTv = false
        )
    }
}

@Composable
fun ConnectionHero(
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    onToggle: () -> Unit,
    isTv: Boolean,
    uploadSpeed: String,
    downloadSpeed: String,
    totalUpload: String,
    totalDownload: String,
    duration: String,
    isTrafficMonitoringEnabled: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StatusButton(
            isRunning = isRunning,
            isClickable = isClickable,
            onToggle = onToggle,
            isTv = isTv
        )

        Spacer(modifier = Modifier.height(32.dp))

        StatusText(
            status = status,
            isRunning = isRunning,
            mode = mode,
            ip = ip,
            port = port,
            profileName = profileName,
            uploadSpeed = uploadSpeed,
            downloadSpeed = downloadSpeed,
            totalUpload = totalUpload,
            totalDownload = totalDownload,
            duration = duration,
            isTrafficMonitoringEnabled = isTrafficMonitoringEnabled
        )
    }
}

@Composable
fun ActionGrid(
    actions: List<DashboardAction>,
    columns: Int,
    isTv: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        actions.chunked(columns).forEach { rowActions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowActions.forEach { action ->
                    ActionCard(
                        action = action,
                        modifier = Modifier.weight(1f),
                        isTv = isTv
                    )
                }
                if (rowActions.size < columns) {
                    repeat(columns - rowActions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    action: DashboardAction,
    modifier: Modifier = Modifier,
    isTv: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isFocused by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isFocused) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val containerColor = if (isFocused)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainerHigh

    val contentColor = if (isFocused)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurface

    ElevatedCard(
        onClick = action.onClick,
        modifier = modifier
            .height(110.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        enabled = action.enabled,
        interactionSource = interactionSource,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isFocused) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(action.label),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatusButton(
    isRunning: Boolean,
    isClickable: Boolean,
    onToggle: () -> Unit,
    isTv: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp)
    ) {
        val buttonScale by animateFloatAsState(
            targetValue = if (isPressed) 0.9f else if (isFocused) 1.1f else if (isRunning) 1.05f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "scale"
        )

        val buttonColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            animationSpec = tween(500),
            label = "color"
        )

        val iconColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(500),
            label = "iconColor"
        )

        if (isRunning) {
            // Pulsing circles
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(1f + pulseProgress * 0.3f)
                    .alpha(0.4f * (1f - pulseProgress))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            val pulseProgress2 = (pulseProgress + 0.5f) % 1f
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(1f + pulseProgress2 * 0.3f)
                    .alpha(0.4f * (1f - pulseProgress2))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Surface(
            onClick = onToggle,
            enabled = isClickable,
            modifier = Modifier
                .size(180.dp)
                .scale(buttonScale)
                .onFocusChanged { isFocused = it.isFocused },
            shape = CircleShape,
            color = buttonColor,
            interactionSource = interactionSource,
            border = if (isFocused && isTv) BorderStroke(4.dp, MaterialTheme.colorScheme.primary) else null,
            shadowElevation = if (isRunning || isFocused) 12.dp else 4.dp,
            tonalElevation = if (isRunning) 8.dp else 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = stringResource(if (isRunning) R.string.status_connected else R.string.status_disconnected),
                    modifier = Modifier.size(80.dp),
                    tint = iconColor
                )
            }
        }
    }
}

@Composable
fun StatusText(
    status: AppStatus,
    isRunning: Boolean,
    mode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    uploadSpeed: String,
    downloadSpeed: String,
    totalUpload: String,
    totalDownload: String,
    duration: String,
    isTrafficMonitoringEnabled: Boolean
) {
    val statusTextRes = when (status) {
        AppStatus.Halted -> R.string.status_disconnected
        AppStatus.Running -> R.string.status_connected
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = status to statusTextRes,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "statusText"
        ) { (_, resId) ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        if (profileName != null) {
            Text(
                text = profileName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        AnimatedVisibility(
            visible = isRunning,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                if (isTrafficMonitoringEnabled) {
                    // Duration
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = downloadSpeed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = totalDownload,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uploadSpeed,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = totalUpload,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (mode == Mode.Proxy) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.proxy_address, ip, port),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
