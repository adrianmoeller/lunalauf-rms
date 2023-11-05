package lunalauf.rms.centralapp.components.dialogs.preferences

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ThLarge
import compose.icons.fontawesomeicons.solid.User
import compose.icons.fontawesomeicons.solid.Users
import lunalauf.rms.centralapp.components.commons.IconSize
import lunalauf.rms.centralapp.components.commons.customScrollbarStyle
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicViewPrefSheet(
    modifier: Modifier = Modifier,
    screenModel: PublicViewScreenModel,
    onClose: () -> Unit
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = {
            onClose()
            screenModel.persistPrefState()
        },
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = "Public view preferences",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))
            var currentTab by remember { mutableStateOf(Tabs.Common) }
            NavigationBar(
                modifier = Modifier.clip(MaterialTheme.shapes.medium)
            ) {
                TabsNavigationBarItem(
                    tab = Tabs.Common,
                    currentTab = currentTab,
                    onClick = { currentTab = it }
                )
                TabsNavigationBarItem(
                    tab = Tabs.Teams,
                    currentTab = currentTab,
                    onClick = { currentTab = it }
                )
                TabsNavigationBarItem(
                    tab = Tabs.Runners,
                    currentTab = currentTab,
                    onClick = { currentTab = it }
                )
            }
            Spacer(Modifier.height(15.dp))
            Box(
                modifier = Modifier.weight(1f, false)
            ) {
                val density = LocalDensity.current
                val listState = rememberLazyListState()
                var listHeight by remember { mutableStateOf(0.dp) }
                LazyColumn(
                    modifier = Modifier.onGloballyPositioned {
                        listHeight = with(density) {
                            it.size.height.toDp()
                        }
                    },
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    state = listState
                ) {
                    currentTab.content(this, screenModel)
                }
                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(listHeight),
                    adapter = rememberScrollbarAdapter(listState),
                    style = customScrollbarStyle
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { screenModel.resetPrefState() }
                ) {
                    Text("Reset all")
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabsNavigationBarItem(
    tab: Tabs,
    currentTab: Tabs,
    onClick: (Tabs) -> Unit
) {
    NavigationBarItem(
        label = { Text(tab.title) },
        icon = {
            Icon(
                modifier = Modifier.size(IconSize.small),
                imageVector = tab.icon,
                contentDescription = null
            )
        },
        selected = currentTab == tab,
        onClick = { onClick(tab) }
    )
}

private enum class Tabs(
    val title: String,
    val icon: ImageVector,
    val content: LazyListScope.(PublicViewScreenModel) -> Unit
) {
    Common(
        title = "Common",
        icon = FontAwesomeIcons.Solid.ThLarge,
        content = { screenModel ->
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
                    ) {
                        ScaleSliderTile(
                            title = "Border brightness",
                            initValue = screenModel.prefState.cmn_borderBrightness,
                            onValueChange = { screenModel.updatePrefState { copy(cmn_borderBrightness = it) } },
                            valueRange = 0f..1f
                        )
                    }
                }
            }
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
                    ) {
                        ScaleSliderTile(
                            title = "Team panel height",
                            initValue = screenModel.prefState.cmn_teamsHeight,
                            onValueChange = { screenModel.updatePrefState { copy(cmn_teamsHeight = it) } },
                            valueRange = 0.1f..0.9f,
                            steps = 39
                        )
                        ScaleSliderTile(
                            title = "Common panel width",
                            initValue = screenModel.prefState.cmn_poolSponsorWidth,
                            onValueChange = { screenModel.updatePrefState { copy(cmn_poolSponsorWidth = it) } },
                            valueRange = 0.01f..0.25f,
                            steps = 23
                        )
                    }
                }
            }
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
                    ) {
                        Text(
                            text = "Font scale",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ScaleSliderTile(
                            title = "Team panel",
                            initValue = screenModel.prefState.tms_fontScale,
                            onValueChange = { screenModel.updatePrefState { copy(tms_fontScale = it) } },
                            valueRange = .5f..1.5f
                        )
                        ScaleSliderTile(
                            title = "Runner panel",
                            initValue = screenModel.prefState.rns_fontScale,
                            onValueChange = { screenModel.updatePrefState { copy(rns_fontScale = it) } },
                            valueRange = .5f..1.5f
                        )
                        ScaleSliderTile(
                            title = "Common panel",
                            initValue = screenModel.prefState.ps_fontScale,
                            onValueChange = { screenModel.updatePrefState { copy(ps_fontScale = it) } },
                            valueRange = .5f..1.5f
                        )
                    }
                }
            }
        }
    ),
    Teams(
        title = "Team panel",
        icon = FontAwesomeIcons.Solid.Users,
        content = { screenModel ->
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
                    ) {
                        Text(
                            text = "Column width",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ScaleSliderTile(
                            title = "Placement",
                            initValue = screenModel.prefState.tms_colWidth_placement,
                            onValueChange = { screenModel.updatePrefState { copy(tms_colWidth_placement = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                        ScaleSliderTile(
                            title = "Rounds",
                            initValue = screenModel.prefState.tms_colWidth_rounds,
                            onValueChange = { screenModel.updatePrefState { copy(tms_colWidth_rounds = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                        ScaleSliderTile(
                            title = "Funfactors",
                            initValue = screenModel.prefState.tms_colWidth_funfactors,
                            onValueChange = { screenModel.updatePrefState { copy(tms_colWidth_funfactors = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                        ScaleSliderTile(
                            title = "Sum",
                            initValue = screenModel.prefState.tms_colWidth_sum,
                            onValueChange = { screenModel.updatePrefState { copy(tms_colWidth_sum = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                        ScaleSliderTile(
                            title = "Contribution",
                            initValue = screenModel.prefState.tms_colWidth_contribution,
                            onValueChange = { screenModel.updatePrefState { copy(tms_colWidth_contribution = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                    }
                }
            }
        }
    ),
    Runners(
        title = "Single runner panel",
        icon = FontAwesomeIcons.Solid.User,
        content = { screenModel ->
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
                    ) {
                        NumberSliderTile(
                            title = "Number of rows",
                            initValue = screenModel.prefState.rns_numOfRows,
                            onValueChange = { screenModel.updatePrefState { copy(rns_numOfRows = it) } },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }
                }
            }
            item {
                OutlinedCard {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 15.dp,
                            vertical = 10.dp
                        )
                    ) {
                        Text(
                            text = "Column width",
                            style = MaterialTheme.typography.labelLarge
                        )
                        ScaleSliderTile(
                            title = "Rounds",
                            initValue = screenModel.prefState.rns_colWidth_rounds,
                            onValueChange = { screenModel.updatePrefState { copy(rns_colWidth_rounds = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                        ScaleSliderTile(
                            title = "Contribution",
                            initValue = screenModel.prefState.rns_colWidth_contribution,
                            onValueChange = { screenModel.updatePrefState { copy(rns_colWidth_contribution = it) } },
                            valueRange = .5f..1.5f,
                            steps = 19
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ScaleSliderTile(
    modifier: Modifier = Modifier,
    title: String,
    initValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 49
) {
    var currentValue by remember { mutableStateOf(initValue) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$title: ${round(currentValue * 100).toInt()}%")
        Spacer(Modifier.width(10.dp))
        Slider(
            modifier = Modifier.widthIn(max = 350.dp),
            value = currentValue,
            onValueChange = { currentValue = it },
            onValueChangeFinished = { onValueChange(currentValue) },
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
private fun NumberSliderTile(
    modifier: Modifier = Modifier,
    title: String,
    initValue: Int,
    onValueChange: (Int) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 9
) {
    var currentValue by remember { mutableStateOf(initValue.toFloat()) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$title: ${currentValue.toInt()}")
        Spacer(Modifier.width(10.dp))
        Slider(
            modifier = Modifier.widthIn(max = 350.dp),
            value = currentValue,
            onValueChange = { currentValue = it },
            onValueChangeFinished = { onValueChange(currentValue.toInt()) },
            valueRange = valueRange,
            steps = steps
        )
    }
}