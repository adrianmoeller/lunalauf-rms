package lunalauf.rms.centralapp.components.windows

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import de.lunalauf_rms.centerapp.generated.resources.Res
import de.lunalauf_rms.centerapp.generated.resources.icon
import lunalauf.rms.centralapp.components.commons.tables.PublicViewTable
import lunalauf.rms.centralapp.components.main.PublicViewScreenModel
import lunalauf.rms.centralapp.publicViewTypography
import lunalauf.rms.centralapp.utils.Formats
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.states.RunnersState
import lunalauf.rms.modelapi.states.TeamsState
import lunalauf.rms.utilities.publicviewprefs.PublicViewPrefState
import org.jetbrains.compose.resources.painterResource
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun PublicViewWindow(
    publicViewScreenModel: PublicViewScreenModel,
    modelState: ModelState.Loaded
) {
    val icon = painterResource(Res.drawable.icon)

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
            icon = icon,
            state = windowState,
            undecorated = true,
            onKeyEvent = {
                if (it.key == Key.F && it.type == KeyEventType.KeyDown) {
                    publicViewScreenModel.switchPresentMode(windowState)
                    return@Window true
                }
                return@Window false
            }
        ) {
            WindowDraggableArea {
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
                        val commonState by modelState.common.collectAsState()
                        val overallRounds by modelState.overallRounds.collectAsState()
                        val lastYearRoundsReached = commonState.sponsorPoolRounds in 1..overallRounds
                        val celebrating = rememberCelebrationState(lastYearRoundsReached)

                        Box(modifier = Modifier.fillMaxSize()) {
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
                                    modelState = modelState,
                                    padding = screenWidth.value.dp / 170,
                                    baseFontSize = (screenWidth / 50) * pref.ps_fontScale,
                                    borderColor = borderColor
                                )
                            }
                            if (celebrating) {
                                CelebrationOverlay(
                                    modifier = Modifier.fillMaxSize(),
                                    text = "Vorjahresrunden erreicht!",
                                    fontSize = screenWidth / 20
                                )
                            }
                        }
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
                Formats.germanEuroFormat(it.totalAmount(), true)
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
                    Formats.germanEuroFormat(it.totalAmount(), true)
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
    modelState: ModelState.Loaded,
    baseFontSize: TextUnit,
    padding: Dp,
    borderColor: Color
) {
    val density = LocalDensity.current
    val commonState by modelState.common.collectAsState()
    val overallRounds by modelState.overallRounds.collectAsState()
    val overallContribution by modelState.overallContribution.collectAsState()
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
                    text = "Vorjahresrunden",
                    style = TextStyle(fontSize = baseFontSize * .6).merge(CommonPanelTextStyles.header)
                )
                Text(
                    text = commonState.sponsorPoolRounds.toString(),
                    style = TextStyle(fontSize = baseFontSize * .8).merge(CommonPanelTextStyles.amount)
                )
                Spacer(Modifier.height(padding))
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val poolBarLevel = if (commonState.sponsorPoolRounds <= 0) 0.0
                    else min(1.0, overallRounds.toDouble() / commonState.sponsorPoolRounds)
                    VerticalPoolBar(
                        modifier = Modifier.fillMaxWidth(.25f),
                        level = poolBarLevel.toFloat(),
                        contentColor = Color(156, 17, 51),
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
                    text = "Gesamt:",
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

private const val CELEBRATION_DURATION_SECONDS = 10L

/**
 * Turns true for [CELEBRATION_DURATION_SECONDS] whenever [reached] switches from false to true.
 * A state that is already reached when the public view opens does not trigger a celebration.
 */
@Composable
private fun rememberCelebrationState(reached: Boolean): Boolean {
    var celebrating by remember { mutableStateOf(false) }
    var previouslyReached by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(reached) {
        val justReached = previouslyReached == false && reached
        previouslyReached = reached
        if (justReached) {
            celebrating = true
            delay(CELEBRATION_DURATION_SECONDS.seconds)
            celebrating = false
        }
    }

    return celebrating
}

@Composable
private fun CelebrationOverlay(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Confetti(modifier = Modifier.fillMaxSize())
        Text(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(percent = 20)
                )
                .padding(horizontal = fontSize.value.dp / 2, vertical = fontSize.value.dp / 4),
            text = text,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color(156, 17, 51)
            )
        )
    }
}

private val confettiColors = listOf(
    Color(156, 17, 51),
    Color(240, 180, 40),
    Color(40, 140, 200),
    Color(60, 170, 110),
    Color(230, 120, 60)
)

private class ConfettiParticle(random: Random) {
    /** Horizontal position as a fraction of the canvas width. */
    var x = random.nextFloat()

    /** Vertical position as a fraction of the canvas height. Starts above the canvas. */
    var y = -random.nextFloat() * 1.5f
    var rotation = random.nextFloat() * 360f

    val fallSpeed = .18f + random.nextFloat() * .32f
    val driftSpeed = (random.nextFloat() - .5f) * .12f
    val spinSpeed = (random.nextFloat() - .5f) * 360f
    val widthFraction = .004f + random.nextFloat() * .006f
    val aspectRatio = .4f + random.nextFloat() * .8f
    val color = confettiColors[random.nextInt(confettiColors.size)]

    fun advance(deltaSeconds: Float) {
        y += fallSpeed * deltaSeconds
        x += driftSpeed * deltaSeconds
        rotation += spinSpeed * deltaSeconds
        if (y > 1.2f) {
            y = -.2f
            x = Random.nextFloat()
        }
        if (x < -.1f) x = 1.1f
        if (x > 1.1f) x = -.1f
    }
}

@Composable
private fun Confetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 250
) {
    val particles = remember { List(particleCount) { ConfettiParticle(Random) } }
    var frame by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        var lastFrameNanos = withFrameNanos { it }
        while (true) {
            withFrameNanos { nanos ->
                val deltaSeconds = ((nanos - lastFrameNanos) / 1_000_000_000.0).toFloat()
                lastFrameNanos = nanos
                particles.forEach { it.advance(deltaSeconds.coerceAtMost(.1f)) }
                frame++
            }
        }
    }

    Canvas(modifier = modifier) {
        @Suppress("UNUSED_EXPRESSION")
        frame // read the frame counter to invalidate the drawing on every animation frame

        particles.forEach { particle ->
            val width = particle.widthFraction * size.width
            val height = width / particle.aspectRatio
            val topLeft = Offset(
                x = particle.x * size.width - width / 2,
                y = particle.y * size.height - height / 2
            )
            rotate(
                degrees = particle.rotation,
                pivot = Offset(topLeft.x + width / 2, topLeft.y + height / 2)
            ) {
                drawRect(
                    color = particle.color,
                    topLeft = topLeft,
                    size = Size(width, height)
                )
            }
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
