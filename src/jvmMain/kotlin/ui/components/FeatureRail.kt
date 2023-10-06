package ui.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.StickyNote
import compose.icons.fontawesomeicons.solid.NetworkWired
import compose.icons.fontawesomeicons.solid.Robot
import compose.icons.fontawesomeicons.solid.Tv

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeatureRail(
    modifier: Modifier = Modifier,
    onPublicViewOpenChange: (Boolean) -> Unit,
    publicViewOpen: Boolean,
    onPublicViewSettingsClick: () -> Unit,
    publicViewAvailable: Boolean,
    onNetworkClick: () -> Unit,
    networkOpen: Boolean,
    onBotsClick: () -> Unit,
    botsOpen: Boolean,
    onLogClick: () -> Unit,
    logOpen: Boolean
) {
    NavigationRail(
        modifier = modifier
            .shadow(5.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        )
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (publicViewAvailable) {
                    Column {
                        TooltipArea(
                            tooltip = {
                                Surface(
                                    modifier = Modifier
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(10.dp),
                                        text = "Public View",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            },
                            tooltipPlacement = TooltipPlacement.ComponentRect(
                                anchor = Alignment.CenterEnd,
                                alignment = Alignment.CenterEnd,
                                offset = DpOffset(x = 7.dp, y = 0.dp)
                            )
                        ) {
                            PublicViewControls(
                                publicViewOpen = publicViewOpen,
                                onPublicViewOpenChange = onPublicViewOpenChange,
                                onPublicViewSettingsClick = onPublicViewSettingsClick
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                NavigationRailItem(
                    icon = {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            imageVector = FontAwesomeIcons.Solid.NetworkWired,
                            contentDescription = "Network"
                        )
                    },
                    label = {
                        Text(text = "Network")
                    },
                    selected = networkOpen,
                    onClick = onNetworkClick
                )
                NavigationRailItem(
                    icon = {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            imageVector = FontAwesomeIcons.Solid.Robot,
                            contentDescription = "Bots"
                        )
                    },
                    label = {
                        Text(text = "Bots")
                    },
                    selected = botsOpen,
                    onClick = onBotsClick
                )
                NavigationRailItem(
                    icon = {
                        Icon(
                            modifier = Modifier.size(25.dp),
                            imageVector = FontAwesomeIcons.Regular.StickyNote,
                            contentDescription = "Log"
                        )
                    },
                    label = {
                        Text(text = "Log")
                    },
                    selected = logOpen,
                    onClick = onLogClick
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PublicViewControls(
    modifier: Modifier = Modifier,
    publicViewOpen: Boolean,
    onPublicViewOpenChange: (Boolean) -> Unit,
    onPublicViewSettingsClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .onPointerEvent(PointerEventType.Enter) { expanded = true }
            .onPointerEvent(PointerEventType.Exit) { expanded = false }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (expanded)
                Checkbox(
                    checked = publicViewOpen,
                    onCheckedChange = onPublicViewOpenChange
                )
            OutlinedCard(
                colors = if (publicViewOpen)
                    CardDefaults.outlinedCardColors(containerColor = Color(0xFFC6EFD2))
                else CardDefaults.outlinedCardColors()
            ) {
                Box(
                    modifier = Modifier.defaultMinSize(
                        minWidth = 56.dp,
                        minHeight = 56.dp
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .size(27.dp),
                        imageVector = FontAwesomeIcons.Solid.Tv,
                        contentDescription = "Public View"
                    )
                }

            }
            if (expanded)
                IconButton(
                    onClick = onPublicViewSettingsClick
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Public view settings"
                    )
                }
        }

    }
}

@Preview
@Composable
fun PreviewFeatureRail() {
    MaterialTheme {
        FeatureRail(
            onPublicViewOpenChange = {},
            publicViewOpen = false,
            onPublicViewSettingsClick = {},
            publicViewAvailable = true,
            onNetworkClick = {},
            networkOpen = false,
            onBotsClick = {},
            botsOpen = false,
            onLogClick = {},
            logOpen = true
        )
    }
}