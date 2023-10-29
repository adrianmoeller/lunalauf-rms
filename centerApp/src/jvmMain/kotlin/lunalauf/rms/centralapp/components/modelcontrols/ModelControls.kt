package lunalauf.rms.centralapp.components.modelcontrols

import androidx.compose.foundation.layout.*
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Bolt
import compose.icons.fontawesomeicons.solid.ListAlt
import compose.icons.fontawesomeicons.solid.Running
import lunalauf.rms.centralapp.components.commons.TabNavigationBarItem
import lunalauf.rms.modelapi.ModelState

@Composable
fun ModelControls(
    modifier: Modifier = Modifier,
    modelState: ModelState.Loaded,
    snackBarHostState: SnackbarHostState
) {
    val tabs = remember {
        object {
            val run = RunControlTab(modelState, snackBarHostState)
            val competitors = CompetitorsControlTab(modelState, snackBarHostState)
            val funfactors = FunfactorsControlTab(modelState, snackBarHostState)
        }
    }

    TabNavigator(
        key = "ModelControls",
        tab = tabs.run
    ) {
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
                        TabNavigationBarItem(tabs.run)
                        TabNavigationBarItem(tabs.competitors)
                        TabNavigationBarItem(tabs.funfactors)
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

private class RunControlTab(
    private val modelState: ModelState.Loaded,
    private val snackBarHostState: SnackbarHostState
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
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }

}

private class CompetitorsControlTab(
    private val modelState: ModelState.Loaded,
    private val snackBarHostState: SnackbarHostState
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
            modelState = modelState,
            snackBarHostState = snackBarHostState
        )
    }
}

private class FunfactorsControlTab(
    private val modelState: ModelState.Loaded,
    private val snackBarHostState: SnackbarHostState
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