package io.github.dovecoteescapee.byedpi.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val minHeight = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isTv || isTablet) 48.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    isTv -> {
                        TvLayout(
                            isRunning = isRunning,
                            status = status,
                            mode = mode,
                            preferredMode = preferredMode,
                            ip = ip,
                            port = port,
                            profileName = profileName,
                            isClickable = viewModel.isClickable,
                            isCmdEnabled = viewModel.isCmdEnabled,
                            onToggle = { viewModel.toggleService(onPrepareVpn) },
                            onSetMode = { viewModel.setMode(it) },
                            onOpenEditor = { viewModel.performActionIfStopped(onOpenEditor) },
                            onOpenSettings = { viewModel.performActionIfStopped(onOpenSettings) },
                            onOpenProfiles = { viewModel.performActionIfStopped(onOpenProfiles) }
                        )
                    }

                    isTablet -> {
                        TabletLayout(
                            minHeight = minHeight,
                            isRunning = isRunning,
                            status = status,
                            mode = mode,
                            preferredMode = preferredMode,
                            ip = ip,
                            port = port,
                            profileName = profileName,
                            isClickable = viewModel.isClickable,
                            isCmdEnabled = viewModel.isCmdEnabled,
                            onToggle = { viewModel.toggleService(onPrepareVpn) },
                            onSetMode = { viewModel.setMode(it) },
                            onOpenEditor = { viewModel.performActionIfStopped(onOpenEditor) },
                            onOpenSettings = { viewModel.performActionIfStopped(onOpenSettings) },
                            onOpenProfiles = { viewModel.performActionIfStopped(onOpenProfiles) }
                        )
                    }

                    else -> {
                        MobileLayout(
                            minHeight = minHeight,
                            isRunning = isRunning,
                            status = status,
                            mode = mode,
                            preferredMode = preferredMode,
                            ip = ip,
                            port = port,
                            profileName = profileName,
                            isClickable = viewModel.isClickable,
                            isCmdEnabled = viewModel.isCmdEnabled,
                            onToggle = { viewModel.toggleService(onPrepareVpn) },
                            onSetMode = { viewModel.setMode(it) },
                            onOpenEditor = { viewModel.performActionIfStopped(onOpenEditor) },
                            onOpenSettings = { viewModel.performActionIfStopped(onOpenSettings) },
                            onOpenProfiles = { viewModel.performActionIfStopped(onOpenProfiles) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MobileLayout(
    minHeight: androidx.compose.ui.unit.Dp,
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    isCmdEnabled: Boolean,
    onToggle: () -> Unit,
    onSetMode: (Mode) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Column(
        modifier = Modifier
            .heightIn(min = minHeight)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        StatusButton(isRunning, isClickable, onToggle)

        Spacer(modifier = Modifier.height(32.dp))

        StatusText(status, isRunning, mode, preferredMode, ip, port, profileName)

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                    label = if (preferredMode == Mode.VPN) stringResource(R.string.vpn_mode) else stringResource(R.string.proxy_mode),
                    onClick = {
                        val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                        onSetMode(newMode)
                    },
                    enabled = !isRunning
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.List,
                    label = stringResource(R.string.profiles_title),
                    onClick = onOpenProfiles,
                    enabled = !isRunning
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = if (isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                    label = stringResource(R.string.editor),
                    onClick = onOpenEditor,
                    enabled = !isRunning
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    onClick = onOpenSettings,
                    enabled = !isRunning
                )
            }
        }
    }
}

@Composable
fun TabletLayout(
    minHeight: androidx.compose.ui.unit.Dp,
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    isCmdEnabled: Boolean,
    onToggle: () -> Unit,
    onSetMode: (Mode) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Row(
        modifier = Modifier
            .heightIn(min = minHeight)
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 32.dp)
        ) {
            StatusButton(isRunning, isClickable, onToggle)
            Spacer(modifier = Modifier.height(32.dp))
            StatusText(status, isRunning, mode, preferredMode, ip, port, profileName)
        }

        Column(
            modifier = Modifier
                .weight(1.2f)
                .padding(start = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                    label = if (preferredMode == Mode.VPN) stringResource(R.string.vpn_mode) else stringResource(R.string.proxy_mode),
                    onClick = {
                        val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                        onSetMode(newMode)
                    },
                    enabled = !isRunning
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.List,
                    label = stringResource(R.string.profiles_title),
                    onClick = onOpenProfiles,
                    enabled = !isRunning
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = if (isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                    label = stringResource(R.string.editor),
                    onClick = onOpenEditor,
                    enabled = !isRunning
                )
                ActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    label = stringResource(R.string.settings),
                    onClick = onOpenSettings,
                    enabled = !isRunning
                )
            }
        }
    }
}

@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .height(100.dp)
            .scale(scale),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = icon,
                transitionSpec = {
                    (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                },
                label = "icon"
            ) { targetIcon ->
                Icon(
                    imageVector = targetIcon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.38f
                    ),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedContent(
                targetState = label,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 2 }) togetherWith (fadeOut() + slideOutVertically { -it / 2 })
                },
                label = "label"
            ) { targetLabel ->
                Text(
                    text = targetLabel,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TvLayout(
    isRunning: Boolean,
    status: AppStatus,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?,
    isClickable: Boolean,
    isCmdEnabled: Boolean,
    onToggle: () -> Unit,
    onSetMode: (Mode) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 24.dp)
        ) {
            StatusButton(isRunning, isClickable, onToggle)
            Spacer(modifier = Modifier.height(32.dp))
            StatusText(status, isRunning, mode, preferredMode, ip, port, profileName)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val buttonModifier = Modifier.fillMaxWidth()

            TvActionButton(
                icon = if (preferredMode == Mode.VPN) Icons.Default.VpnKey else Icons.Default.Router,
                label = if (preferredMode == Mode.VPN) stringResource(R.string.vpn_mode) else stringResource(R.string.proxy_mode),
                onClick = {
                    val newMode = if (preferredMode == Mode.VPN) Mode.Proxy else Mode.VPN
                    onSetMode(newMode)
                },
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
            TvActionButton(
                icon = Icons.AutoMirrored.Filled.List,
                label = stringResource(R.string.profiles_title),
                onClick = onOpenProfiles,
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
            TvActionButton(
                icon = if (isCmdEnabled) Icons.Default.Terminal else Icons.Default.EditNote,
                label = stringResource(R.string.editor),
                onClick = onOpenEditor,
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
            TvActionButton(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.settings),
                onClick = onOpenSettings,
                modifier = buttonModifier.alpha(if (isRunning) 0.5f else 1f)
            )
        }
    }
}

@Composable
fun StatusButton(isRunning: Boolean, isClickable: Boolean, onToggle: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
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
        modifier = Modifier.size(220.dp)
    ) {
        val buttonScale by animateFloatAsState(
            targetValue = if (isRunning) 1.1f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "scale"
        )

        val buttonColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            animationSpec = tween(500),
            label = "color"
        )

        val iconColor by animateColorAsState(
            targetValue = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(500),
            label = "iconColor"
        )

        if (isRunning) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(1f + pulseProgress * 0.4f)
                    .alpha(1f - pulseProgress)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )

            val pulseProgress2 = (pulseProgress + 0.5f) % 1f
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(1f + pulseProgress2 * 0.4f)
                    .alpha(1f - pulseProgress2)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            )
        }

        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(buttonColor)
                .onFocusChanged { isFocused = it.isFocused }
                .scale(if (isFocused) 1.1f else 1f)
                .border(
                    if (isFocused) 4.dp else 0.dp,
                    if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CircleShape
                )
                .clickable(
                    enabled = isClickable,
                    onClick = onToggle
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
}

@Composable
fun StatusText(
    status: AppStatus,
    isRunning: Boolean,
    mode: Mode,
    preferredMode: Mode,
    ip: String,
    port: String,
    profileName: String?
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
                val (newStatus, _) = targetState
                val (oldStatus, _) = initialState

                val animation = when {
                    newStatus == AppStatus.Running && oldStatus == AppStatus.Halted -> {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    }

                    newStatus == AppStatus.Halted && oldStatus == AppStatus.Running -> {
                        (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> height } + fadeOut())
                    }

                    else -> {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    }
                }

                animation.using(
                    SizeTransform(clip = false)
                )
            },
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
            label = "statusText"
        ) { (_, resId) ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
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
            visible = isRunning && mode == Mode.Proxy,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.proxy_address, ip, port),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun TvActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { isFocused = it.isFocused }
            .scale(if (isFocused) 1.05f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isFocused) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
