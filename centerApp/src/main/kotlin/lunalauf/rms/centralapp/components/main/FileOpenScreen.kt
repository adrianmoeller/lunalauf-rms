package lunalauf.rms.centralapp.components.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.File
import compose.icons.fontawesomeicons.regular.FolderOpen
import compose.icons.fontawesomeicons.regular.Save
import compose.icons.fontawesomeicons.regular.TimesCircle
import lunalauf.rms.centralapp.components.commons.CustomSnackBarHost
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.dialogs.preferences.PreferencesSheet
import lunalauf.rms.centralapp.components.modelcontrols.ModelControls
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.resource.ModelResourceManager
import lunalauf.rms.utilities.network.bot.BotManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOpenScreen(
    modifier: Modifier = Modifier,
    modelResourceManager: ModelResourceManager,
    botManager: BotManager,
    modelState: ModelState.Loaded,
    onMenuNewFile: () -> Unit,
    onMenuOpenFile: () -> Unit,
    onMenuSaveFile: () -> Unit,
    onMenuCloseFile: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val screenModel = remember {
        FileOpenScreenModel(
            modelResourceManager = modelResourceManager,
            botManager = botManager,
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }
    val preferences by screenModel.preferences.collectAsState()
    val competitorMessenger by botManager.competitorMessenger.collectAsState()

    var settingsOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { CustomSnackBarHost(snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                title = {
                    Text(
                        text = modelState.fileName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    Box {
                        IconButton(
                            onClick = { menuOpen = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false },
                            offset = DpOffset(x = 8.dp, y = (-2).dp)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New") },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(IconSize.small),
                                        imageVector = FontAwesomeIcons.Regular.File,
                                        contentDescription = null
                                    )
                                },
                                onClick = onMenuNewFile
                            )
                            DropdownMenuItem(
                                text = { Text("Open") },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(IconSize.small),
                                        imageVector = FontAwesomeIcons.Regular.FolderOpen,
                                        contentDescription = null
                                    )
                                },
                                onClick = onMenuOpenFile
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Save") },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(IconSize.small),
                                        imageVector = FontAwesomeIcons.Regular.Save,
                                        contentDescription = null
                                    )
                                },
                                onClick = onMenuSaveFile
                            )
                            DropdownMenuItem(
                                text = { Text("Close") },
                                leadingIcon = {
                                    Icon(
                                        modifier = Modifier.size(IconSize.small),
                                        imageVector = FontAwesomeIcons.Regular.TimesCircle,
                                        contentDescription = null
                                    )
                                },
                                onClick = onMenuCloseFile
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { settingsOpen = true }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (settingsOpen) {
            PreferencesSheet(
                onClose = { settingsOpen = false },
                autoSaveActive = preferences.autoSaveActive,
                onAutoSaveActiveChange = screenModel::updateAutoSaveActive,
                autoSaveInterval = preferences.autoSaveInterval,
                onAutoSaveIntervalChange = screenModel::updateDisplayedAutoSaveInterval,
                onAutoSaveIntervalChangeFinished = screenModel::updateAutoSaveInterval,
                roundThreshold = preferences.roundThreshold,
                onRoundThresholdChange = screenModel::updateDisplayedRoundThreshold,
                onRoundThresholdChangeFinished = screenModel::updateRoundThreshold,
                saveConnectionsActive = preferences.saveConnectionsActive,
                onSaveConnectionsActiveChange = screenModel::updateSaveConnectionsActive
            )
        }

        ModelControls(
            modifier = Modifier.padding(innerPadding),
            botManager = botManager,
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }
}