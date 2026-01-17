package io.github.dovecoteescapee.byedpi.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppInfo
import io.github.dovecoteescapee.byedpi.ui.viewmodel.AppSelectionViewModel
import io.github.dovecoteescapee.byedpi.utility.isTablet
import io.github.dovecoteescapee.byedpi.utility.isTv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.tv.material3.ClickableSurfaceDefaults as TvClickableSurfaceDefaults
import androidx.tv.material3.Surface as TvSurface

@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }
    val isTablet = remember { context.isTablet() }
    val isLargeScreen = isTv || isTablet
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = viewModel.searchQuery.isNotEmpty() || viewModel.showSelectedOnly) {
        if (viewModel.searchQuery.isNotEmpty()) {
            viewModel.searchQuery = ""
        } else if (viewModel.showSelectedOnly) {
            viewModel.showSelectedOnly = false
        }
        if (isLargeScreen) focusManager.clearFocus()
    }

    if (isTv) {
        AppSelectionScreenTv(viewModel, onBack)
    } else {
        AppSelectionScreenPhone(viewModel, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppSelectionScreenPhone(
    viewModel: AppSelectionViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTablet = remember { context.isTablet() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apps_select)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_selection))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = viewModel.searchQuery,
                        onQueryChange = { viewModel.searchQuery = it },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_apps)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (viewModel.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) { }

            MultiChoiceSegmentedButtonRow(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    checked = viewModel.showSelectedOnly,
                    onCheckedChange = { viewModel.showSelectedOnly = it },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { SegmentedButtonDefaults.Icon(active = viewModel.showSelectedOnly) },
                    label = { Text(stringResource(R.string.filter_selected)) }
                )
                SegmentedButton(
                    checked = viewModel.showSystemApps,
                    onCheckedChange = { viewModel.showSystemApps = it },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { SegmentedButtonDefaults.Icon(active = viewModel.showSystemApps) },
                    label = { Text(stringResource(R.string.filter_system)) }
                )
            }

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (isTablet) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(viewModel.filteredApps, key = { it.packageName }) { app ->
                            AppItem(
                                app = app,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleAppSelection(app, isChecked)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(viewModel.filteredApps, key = { it.packageName }) { app ->
                            AppItem(
                                app = app,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleAppSelection(app, isChecked)
                                },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppSelectionScreenTv(
    viewModel: AppSelectionViewModel,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val searchFocusRequester = remember { FocusRequester() }

    // Initial focus
    LaunchedEffect(Unit) {
        searchFocusRequester.requestFocus()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        Column(
            modifier = Modifier
                .width(300.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(24.dp)
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.apps_select),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Search
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(searchFocusRequester)
                    .onPreviewKeyEvent {
                        if (it.type == KeyEventType.KeyDown) {
                            when (it.key) {
                                Key.DirectionRight -> {
                                    focusManager.moveFocus(FocusDirection.Right)
                                    true
                                }
                                Key.Enter, Key.NumPadEnter -> {
                                    focusManager.moveFocus(FocusDirection.Right)
                                    true
                                }
                                Key.DirectionDown -> {
                                    focusManager.moveFocus(FocusDirection.Down)
                                    true
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.moveFocus(FocusDirection.Right) })
            )

            if (viewModel.searchQuery.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.searchQuery = ""
                        searchFocusRequester.requestFocus()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.cmd_args_clear))
                }
            }

            // Filters
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.showSelectedOnly,
                    onClick = { viewModel.showSelectedOnly = !viewModel.showSelectedOnly },
                    leadingIcon = if (viewModel.showSelectedOnly) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.filter_selected)) }

                )

                FilterChip(
                    selected = viewModel.showSystemApps,
                    onClick = { viewModel.showSystemApps = !viewModel.showSystemApps },
                    leadingIcon = if (viewModel.showSystemApps) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.filter_system)) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Clear Selection
            Button(
                onClick = { viewModel.clearSelection() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.clear_selection))
            }
        }

        // Main Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 200.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusGroup(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.filteredApps, key = { it.packageName }) { app ->
                        AppItemTv(
                            app = app,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleAppSelection(app, isChecked)
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(
    app: AppInfo,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var icon by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(app.packageName) {
        withContext(Dispatchers.IO) {
            icon = try {
                context.packageManager.getApplicationIcon(app.packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                context.packageManager.defaultActivityIcon
            }
        }
    }

    Surface(
        onClick = { onCheckedChange(!app.isSelected) },
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        ListItem(
            headlineContent = { Text(app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            supportingContent = { Text(app.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            leadingContent = {
                if (icon != null) {
                    Image(
                        bitmap = icon!!.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Box(modifier = Modifier.size(40.dp))
                }
            },
            trailingContent = {
                Switch(
                    checked = app.isSelected,
                    onCheckedChange = onCheckedChange
                )
            }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AppItemTv(
    app: AppInfo,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var icon by remember { mutableStateOf<Drawable?>(null) }

    LaunchedEffect(app.packageName) {
        withContext(Dispatchers.IO) {
            icon = try {
                context.packageManager.getApplicationIcon(app.packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                context.packageManager.defaultActivityIcon
            }
        }
    }

    TvSurface(
        onClick = { onCheckedChange(!app.isSelected) },
        shape = TvClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium),
        scale = TvClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        colors = TvClickableSurfaceDefaults.colors(
            containerColor = if (app.isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            ),
            focusedContainerColor = if (app.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (app.isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            focusedContentColor = if (app.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Image(
                    bitmap = icon!!.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Box(modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.appName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    app.packageName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (app.isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                )
            }
        }
    }
}
