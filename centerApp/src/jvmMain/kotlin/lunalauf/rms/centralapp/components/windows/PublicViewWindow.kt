package lunalauf.rms.centralapp.components.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import lunalauf.rms.centralapp.components.commons.tables.PublicViewTable
import lunalauf.rms.centralapp.components.main.MainScreenModel
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import lunalauf.rms.centralapp.publicViewTypography
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.states.RunnersState
import lunalauf.rms.modelapi.states.TeamsState
import lunalauf.rms.utilities.publicviewprefs.PublicViewPrefState
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublicViewWindow(
    mainScreenModel: MainScreenModel,
    publicViewScreenModel: PublicViewScreenModel,
    modelState: ModelState.Loaded
) {
    if (publicViewScreenModel.open) {
        val pref = publicViewScreenModel.prefState
        val borderWidth = 4.dp
        val brightness = (255 * pref.cmn_borderBrightness).toInt()
        val borderColor = Color(
            red = brightness,
            green = brightness,
            blue = brightness
        )
        val windowState = rememberWindowState()
        val density = LocalDensity.current
        var screenWidth by remember { mutableStateOf(0.sp) }

        Window(
            onCloseRequest = { publicViewScreenModel.updateOpen(false) },
            title = "Luna-Lauf - Public View",
            state = windowState,
            onKeyEvent = {
                if (it.key == Key.F && it.type == KeyEventType.KeyDown) {
                    if (windowState.placement != WindowPlacement.Fullscreen)
                        windowState.placement = WindowPlacement.Fullscreen
                    else
                        windowState.placement = WindowPlacement.Floating
                    return@Window true
                }
                return@Window false
            }
        ) {
            MaterialTheme(
                colorScheme = lightColorScheme(),
                typography = publicViewTypography
            ) {
                Surface(
                    modifier = Modifier.onGloballyPositioned {
                        screenWidth = with(density) {
                            it.size.width.toSp()
                        }
                    },
                    color = borderColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(borderWidth)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f - pref.cmn_poolSponsorWidth),
                            verticalArrangement = Arrangement.spacedBy(borderWidth)
                        ) {
                            val teamsState by modelState.teams.collectAsState()
                            TeamPanel(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(pref.cmn_teamsHeight),
                                teamsState = teamsState,
                                baseFontSize = (screenWidth / 45) * pref.tms_fontScale,
                                pref = pref
                            )
                            val runnersState by modelState.runners.collectAsState()
                            RunnerPanel(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f - pref.cmn_teamsHeight),
                                runnersState = runnersState,
                                baseFontSize = (screenWidth / 55) * pref.rns_fontScale,
                                pref = pref
                            )
                        }
                        CommonPanel(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(pref.cmn_poolSponsorWidth),
                            mainScreenModel = mainScreenModel,
                            modelState = modelState,
                            padding = screenWidth.value.dp / 170,
                            baseFontSize = (screenWidth / 50) * pref.ps_fontScale,
                            borderColor = borderColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamPanel(
    modifier: Modifier = Modifier,
    teamsState: TeamsState,
    baseFontSize: TextUnit,
    pref: PublicViewPrefState
) {
    val data = teamsState.teams
        .sortedByDescending { it.numOfRounds() + it.numOfFunfactorPoints() }
        .map {
            val numOfRounds = it.numOfRounds()
            val numOfFunfactorPoints = it.numOfFunfactorPoints()
            listOf(
                it.name ?: "-",
                numOfRounds.toString(),
                numOfFunfactorPoints.toString(),
                (numOfRounds + numOfFunfactorPoints).toString(),
                Formats.germanEuroFormat(it.totalAmount())
            )
        }
    PublicViewTable(
        modifier = modifier,
        header = listOf("Team", "Runden", "Funfactors", "Summe", "Spendenbetrag"),
        data = data,
        weights = listOf(
            3.6f,
            1f * pref.tms_colWidth_rounds,
            1f * pref.tms_colWidth_funfactors,
            1f * pref.tms_colWidth_sum,
            1.4f * pref.tms_colWidth_contribution
        ),
        headerTextStyles = buildMap {
            put(0, TeamPanelTextStyles.header)
            put(1, TeamPanelTextStyles.header)
            put(2, TeamPanelTextStyles.header)
            put(3, TeamPanelTextStyles.headerSum)
            put(4, TeamPanelTextStyles.header)
        },
        dataTextStyles = buildMap {
            put(0, TeamPanelTextStyles.dataTeam)
            put(1, TeamPanelTextStyles.data)
            put(2, TeamPanelTextStyles.data)
            put(3, TeamPanelTextStyles.dataSum)
            put(4, TeamPanelTextStyles.data)
        },
        showPlacements = true,
        placementsWeight = .5f * pref.tms_colWidth_placement,
        baseFontSize = baseFontSize
    )
    MaterialTheme.typography
}

@Composable
private fun RunnerPanel(
    modifier: Modifier = Modifier,
    runnersState: RunnersState,
    baseFontSize: TextUnit,
    pref: PublicViewPrefState
) {
    val singleRunners = runnersState.runners
        .filter { it.team == null }
        .sortedByDescending { it.numOfRounds() }
    val splitRunners = split(singleRunners, pref.rns_numOfRows)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        splitRunners.forEachIndexed { index, runners ->
            var data = runners.map {
                listOf(
                    it.name ?: it.id.toString(),
                    it.numOfRounds().toString(),
                    Formats.germanEuroFormat(it.totalAmount())
                )
            }
            if (index == splitRunners.lastIndex && data.size < pref.rns_numOfRows) {
                data = data + 0.until(pref.rns_numOfRows - data.size)
                    .map { listOf("", "", "") }
            }
            PublicViewTable(
                modifier = Modifier.weight(1f),
                header = listOf("Läufer*in", "Runden", "Spendenbetrag"),
                data = data,
                weights = listOf(
                    2f,
                    1f * pref.rns_colWidth_rounds,
                    1.5f * pref.rns_colWidth_contribution
                ),
                headerTextStyles = buildMap {
                    put(0, RunnerPanelTextStyles.header)
                    put(1, RunnerPanelTextStyles.header)
                    put(2, RunnerPanelTextStyles.header)
                },
                dataTextStyles = buildMap {
                    put(0, RunnerPanelTextStyles.dataTeam)
                    put(1, RunnerPanelTextStyles.data)
                    put(2, RunnerPanelTextStyles.data)
                },
                showPlacements = false,
                baseFontSize = baseFontSize
            )
        }
    }
}

private fun <T> split(list: List<T>, subListSize: Int): List<List<T>> {
    val numOfSubLists = if (list.size <= 1) 1
    else ((list.size - 1) / subListSize) + 1

    return 0.until(numOfSubLists).map {
        val fromIndex = it * subListSize
        val toIndex = min((it + 1) * subListSize, list.size)
        list.subList(fromIndex, toIndex)
    }
}

@Composable
private fun CommonPanel(
    modifier: Modifier = Modifier,
    mainScreenModel: MainScreenModel,
    modelState: ModelState.Loaded,
    baseFontSize: TextUnit,
    padding: Dp,
    borderColor: Color
) {
    val density = LocalDensity.current
    val commonState by modelState.common.collectAsState()
    val overallRounds by modelState.overallRounds.collectAsState()
    val overallContribution by modelState.overallContribution.collectAsState()
    val currentSponsorPoolAmount by modelState.currentSponsorPoolAmount.collectAsState()
    val remainingTime by modelState.modelAPI.runTimer.remainingTime.collectAsState()
    val baseTextStyle = TextStyle(fontSize = baseFontSize)
    var clockHeight by remember { mutableStateOf(0.dp) }
    val cornerRadius = clockHeight / 2

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                )
                .onGloballyPositioned {
                    clockHeight = with(density) {
                        it.size.height.toDp()
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(vertical = padding / 3),
                text = Formats.clockFormat(remainingTime.seconds),
                style = TextStyle(fontSize = baseFontSize * 1.1).merge(CommonPanelTextStyles.clock)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(top = padding, bottom = padding * 1.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sponsorenpool",
                    style = TextStyle(fontSize = baseFontSize * .6).merge(CommonPanelTextStyles.header)
                )
                Text(
                    text = Formats.germanEuroFormat(currentSponsorPoolAmount),
                    style = TextStyle(fontSize = baseFontSize * .8).merge(CommonPanelTextStyles.amount)
                )
                Spacer(Modifier.height(padding))
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    VerticalPoolBar(
                        modifier = Modifier.fillMaxWidth(.25f),
                        level = (currentSponsorPoolAmount / commonState.sponsorPoolAmount).toFloat(),
                        contentColor = Color.Red,
                        backgroundColor = borderColor
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(vertical = padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gesamt",
                    style = TextStyle(fontSize = baseFontSize * .8).merge(CommonPanelTextStyles.all)
                )
                Text(
                    text = "Runden",
                    style = baseTextStyle.merge(CommonPanelTextStyles.header)
                )
                Text(
                    text = overallRounds.toString(),
                    style = baseTextStyle.merge(CommonPanelTextStyles.amount)
                )
                Text(
                    text = "Spenden",
                    style = baseTextStyle.merge(CommonPanelTextStyles.header)
                )
                Text(
                    text = Formats.germanEuroFormat(overallContribution),
                    style = baseTextStyle.merge(CommonPanelTextStyles.amount)
                )
            }
        }
    }
}

@Composable
private fun VerticalPoolBar(
    modifier: Modifier = Modifier,
    level: Float,
    contentColor: Color,
    backgroundColor: Color
) {
    val density = LocalDensity.current
    var barHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(level)
                .widthIn(max = barHeight)
                .fillMaxWidth()
                .onGloballyPositioned {
                    barHeight = with(density) {
                        it.size.height.toDp()
                    }
                }
                .background(
                    color = contentColor,
                    shape = CircleShape
                ),
        ) {
        }
    }
}

private object TeamPanelTextStyles {
    val header = TextStyle(fontWeight = FontWeight.Light)
    val headerSum = TextStyle(fontWeight = FontWeight.Normal)
    val data = TextStyle(fontWeight = FontWeight.Normal)
    val dataTeam = TextStyle(fontWeight = FontWeight.SemiBold)
    val dataSum = TextStyle(fontWeight = FontWeight.Bold)
}

private object RunnerPanelTextStyles {
    val header = TextStyle(fontWeight = FontWeight.Light)
    val data = TextStyle(fontWeight = FontWeight.Normal)
    val dataTeam = TextStyle(fontWeight = FontWeight.SemiBold)
}

private object CommonPanelTextStyles {
    val clock = TextStyle(fontWeight = FontWeight.SemiBold)
    val header = TextStyle(fontWeight = FontWeight.SemiBold)
    val amount = TextStyle(fontWeight = FontWeight.Normal)
    val all = TextStyle(fontWeight = FontWeight.Light)
}
