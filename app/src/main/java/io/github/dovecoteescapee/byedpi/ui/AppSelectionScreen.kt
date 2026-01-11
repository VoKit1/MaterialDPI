package io.github.dovecoteescapee.byedpi.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel = viewModel(),
    onBack: () -> Unit
) {
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
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

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
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

@Composable
fun AppItem(
    app: AppInfo,
    onCheckedChange: (Boolean) -> Unit
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
        },
        modifier = Modifier.clickable { onCheckedChange(!app.isSelected) }
    )
}
