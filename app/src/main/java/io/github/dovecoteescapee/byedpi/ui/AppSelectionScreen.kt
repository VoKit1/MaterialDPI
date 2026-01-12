package io.github.dovecoteescapee.byedpi.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.dovecoteescapee.byedpi.R
import io.github.dovecoteescapee.byedpi.data.AppInfo
import io.github.dovecoteescapee.byedpi.ui.viewmodel.AppSelectionViewModel
import io.github.dovecoteescapee.byedpi.utility.isTv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isTv = remember { context.isTv() }

    BackHandler(enabled = viewModel.searchQuery.isNotEmpty() || viewModel.showSelectedOnly) {
        if (viewModel.searchQuery.isNotEmpty()) {
            viewModel.searchQuery = ""
        } else if (viewModel.showSelectedOnly) {
            viewModel.showSelectedOnly = false
        }
    }

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
        Column(modifier = Modifier.padding(padding)) {
            if (!isTv) {
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
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) { }
            } else {
                // TV Search - simpler TextField
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_apps)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true
                )
            }

            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = if (isTv) 48.dp else 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = viewModel.showSelectedOnly,
                        onClick = { viewModel.showSelectedOnly = !viewModel.showSelectedOnly },
                        label = { Text(stringResource(R.string.filter_selected)) },
                        leadingIcon = if (viewModel.showSelectedOnly) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
                item {
                    FilterChip(
                        selected = viewModel.showSystemApps,
                        onClick = { viewModel.showSystemApps = !viewModel.showSystemApps },
                        label = { Text(stringResource(R.string.filter_system)) },
                        leadingIcon = if (viewModel.showSystemApps) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else null
                    )
                }
            }

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (isTv) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 300.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(viewModel.filteredApps, key = { it.packageName }) { app ->
                            AppItem(
                                app = app,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleAppSelection(app, isChecked)
                                },
                                isTv = true
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
                                }
                            )
                        }
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
    isTv: Boolean = false
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

    if (isTv) {
        Surface(
            onClick = { onCheckedChange(!app.isSelected) },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = if (app.isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon!!.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Box(modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                    Text(app.packageName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
                Checkbox(
                    checked = app.isSelected,
                    onCheckedChange = null
                )
            }
        }
    } else {
        Surface(
            onClick = { onCheckedChange(!app.isSelected) },
            color = MaterialTheme.colorScheme.surface
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
}
