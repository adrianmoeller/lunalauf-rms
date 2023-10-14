package lunalauf.rms.centralapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.ListAlt
import compose.icons.fontawesomeicons.solid.Running
import lunalauf.rms.centralapp.ui.common.FAIconSize
import lunalauf.rms.centralapp.ui.common.Table
import lunalauf.rms.centralapp.ui.screens.RunControlScreen
import lunalauf.rms.modelapi.ModelState

@Composable
fun ModelControl(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded
) {
    var selectedTab by remember { mutableStateOf(Tab.Run) }

    Column(
        modifier = modifier
    ) {
        NavigationBar {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.widthIn(max = 700.dp)
                ) {
                    NavigationBarItem(
                        label = { Text("Run") },
                        icon = {
                            Icon(
                                modifier = Modifier.size(FAIconSize.small),
                                imageVector = FontAwesomeIcons.Solid.ListAlt,
                                contentDescription = null
                            )
                        },
                        selected = selectedTab == Tab.Run,
                        onClick = { selectedTab = Tab.Run }
                    )
                    NavigationBarItem(
                        label = { Text("Competitors") },
                        icon = {
                            Icon(
                                modifier = Modifier.size(FAIconSize.small),
                                imageVector = FontAwesomeIcons.Solid.Running,
                                contentDescription = null
                            )
                        },
                        selected = selectedTab == Tab.Competitors,
                        onClick = { selectedTab = Tab.Competitors }
                    )
                    NavigationBarItem(
                        label = { Text("Funfactors") },
                        icon = {
                            Icon(
                                modifier = Modifier.size(FAIconSize.small),
                                imageVector = FontAwesomeIcons.Solid.Bolt,
                                contentDescription = null
                            )
                        },
                        selected = selectedTab == Tab.Funfactors,
                        onClick = { selectedTab = Tab.Funfactors }
                    )
                }
            }

        }
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when(selectedTab) {
                Tab.Run -> {
                    RunControlScreen(
                        modifier = Modifier.fillMaxSize(),
                        modelState = modelState
                    )
                }
                Tab.Competitors -> {
                    // TODO
                    Column {
                        val runnersState by modelState.runners.collectAsState()
                        val data = runnersState.runners.map {
                            listOf(
                                it.id.toString(),
                                it.name ?: "",
                                it.numOfRounds().toString(),
                                it.team?.name ?: "",
                                it.totalAmount().toString()
                            )
                        }
                        Table(
                            modifier = Modifier.weight(1f),
                            header = listOf("ID", "Name", "Rounds", "Team", "Total Amount"),
                            data = data,
                            weights = listOf(1f, 3f, 1f, 3f, 1.5f)
                        )
                    }
                }
                Tab.Funfactors -> {
                    // TODO
                }
            }
        }

    }
}

enum class Tab {
    Run, Competitors, Funfactors
}