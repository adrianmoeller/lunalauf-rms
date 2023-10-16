package lunalauf.rms.centralapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.*
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import compose.icons.fontawesomeicons.solid.ListAlt
import compose.icons.fontawesomeicons.solid.Running
import lunalauf.rms.centralapp.ui.common.IconSize
import lunalauf.rms.centralapp.ui.screens.CompetitorsControlScreen
import lunalauf.rms.centralapp.ui.screens.RunControlScreen
import lunalauf.rms.modelapi.ModelState

@Composable
fun ModelControl(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded
) {
    val tabs = remember {
        object {
            val run = RunControlTab(modelState)
            val competitors = CompetitorsControlTab(modelState)
            val funfactors = FunfactorsControlTab(modelState)
        }
    }

    TabNavigator(RunControlTab(modelState)) {
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
                        TabNavigationItem(tabs.run)
                        TabNavigationItem(tabs.competitors)
                        TabNavigationItem(tabs.funfactors)
                    }
                }
            }
            Box(
                modifier = Modifier.weight(1f)
            ) {
                CurrentTab()
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        label = { Text(tab.options.title) },
        icon = {
            Icon(
                modifier = Modifier.size(IconSize.small),
                painter = tab.options.icon ?: rememberVectorPainter(FontAwesomeIcons.Solid.ExclamationTriangle),
                contentDescription = null
            )
        },
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab }
    )
}

class RunControlTab(
    private val modelState: ModelState.Loaded
) : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.ListAlt)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "Run",
                    icon = icon

                )
            }
        }

    @Composable
    override fun Content() {
        RunControlScreen(
            modifier = Modifier.fillMaxSize(),
            modelState = modelState
        )
    }

}

class CompetitorsControlTab(
    private val modelState: ModelState.Loaded
) : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.Running)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "Competitors",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        CompetitorsControlScreen(
            modifier = Modifier.fillMaxSize(),
            modelState = modelState
        )
    }
}

class FunfactorsControlTab(
    private val modelState: ModelState.Loaded
) : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(FontAwesomeIcons.Solid.Bolt)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "Funfactors",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // TODO
    }
}