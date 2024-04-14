package lunalauf.rms.counterapp.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lunalauf.rms.counterapp.ralewayTextStyle
import lunalauf.rms.utilities.network.client.InfoDisplay
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.response.TeamRunnerInfoResponse

@Composable
fun InfoDisplayScreen(
    modifier: Modifier = Modifier,
    infoDisplay: InfoDisplay
) {
    val density = LocalDensity.current
    var containerHeightText by remember { mutableStateOf(0.sp) }
    var containerHeight by remember { mutableStateOf(0.dp) }

    val scanChipScreenModel = remember { ScanChipScreenModel() }
    val infoDisplayState by infoDisplay.state.collectAsState()

    val scope = rememberCoroutineScope()
    val alphaAnimation = remember { Animatable(0f) }
    LaunchedEffect(infoDisplayState) {
        when (val constIDState = infoDisplayState) {
            is InfoDisplay.State.Response -> {
                animateResponseStatus(
                    scope = scope,
                    alphaAnimation = alphaAnimation
                )
            }
            is InfoDisplay.State.Error -> {
                if (constIDState.error == ErrorType.UNKNOWN_ID) {
                    animateLongResponseStatus(
                        scope = scope,
                        alphaAnimation = alphaAnimation
                    )
                } else {
                    animateErrorResponseStatus(
                        scope = scope,
                        alphaAnimation = alphaAnimation
                    )
                }
            }
            InfoDisplay.State.None -> {
                alphaAnimation.snapTo(0f)
            }
        }
    }

    ScanChipField(
        modifier = modifier.onGloballyPositioned {
            containerHeightText = with(density) {
                it.size.height.toSp()
            }
            containerHeight = with(density) {
                it.size.height.toDp()
            }
        },
        onNumberKeyEvent = scanChipScreenModel::toIdBuffer,
        onEnterKeyEvent = {
            val chipId = scanChipScreenModel.getBufferedId()
            if (chipId != null) {
                infoDisplay.processInput(chipId)
            } else {
                // TODO show snack bar with error
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .alpha(alphaAnimation.value)
                    .widthIn(max = containerHeight * 0.9f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val constIDState = infoDisplayState) {
                    is InfoDisplay.State.Response -> {
                        val response = constIDState.response
                        if (response is TeamRunnerInfoResponse) {
                            TitleLine(
                                containerHeightText = containerHeightText,
                                containerHeight = containerHeight,
                                text = "Team"
                            )
                            InfoLine(
                                containerHeight = containerHeight,
                                containerHeightText = containerHeightText,
                                title = "Name",
                                value = response.teamName
                            )
                            InfoLine(
                                containerHeight = containerHeight,
                                containerHeightText = containerHeightText,
                                title = "Runden",
                                value = response.numTeamRounds.toString()
                            )
                            InfoLine(
                                containerHeight = containerHeight,
                                containerHeightText = containerHeightText,
                                title = "Funfactor-Punkte",
                                value = response.teamFunfactorPoints.toString()
                            )
                            InfoLine(
                                containerHeight = containerHeight,
                                containerHeightText = containerHeightText,
                                title = "Gesamt",
                                value = (response.numTeamRounds + response.teamFunfactorPoints).toString()
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = containerHeight * 0.05f)
                            )
                        }
                        TitleLine(
                            containerHeight = containerHeight,
                            containerHeightText = containerHeightText,
                            text = "Läufer*in"
                        )
                        InfoLine(
                            containerHeight = containerHeight,
                            containerHeightText = containerHeightText,
                            title = "Name",
                            value = response.runnerName.ifBlank { "-" }
                        )
                        InfoLine(
                            containerHeight = containerHeight,
                            containerHeightText = containerHeightText,
                            title = "Chip-ID",
                            value = response.runnerId.toString()
                        )
                        InfoLine(
                            containerHeight = containerHeight,
                            containerHeightText = containerHeightText,
                            title = "Runden",
                            value = response.numRunnerRounds.toString()
                        )
                    }

                    is InfoDisplay.State.Error -> {
                        Text(
                            text = constIDState.error.message,
                            style = ralewayTextStyle,
                            fontWeight = FontWeight.Bold,
                            fontSize = containerHeightText * 0.06,
                            textAlign = TextAlign.Center
                        )
                    }

                    InfoDisplay.State.None -> {}
                }
            }
        }
    }
}

@Composable
private fun TitleLine(
    containerHeightText: TextUnit,
    containerHeight: Dp,
    text: String
) {
    Text(
        text = text,
        style = ralewayTextStyle,
        fontWeight = FontWeight.Bold,
        fontSize = containerHeightText * 0.07,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(containerHeight * 0.01f))
}

@Composable
private fun InfoLine(
    containerHeight: Dp,
    containerHeightText: TextUnit,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(containerHeight * 0.01f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = ralewayTextStyle,
            fontWeight = FontWeight.Normal,
            fontSize = containerHeightText * 0.05
        )
        Text(
            text = value,
            style = ralewayTextStyle,
            fontWeight = FontWeight.Normal,
            fontSize = containerHeightText * 0.05
        )
    }
}

private fun animateResponseStatus(
    scope: CoroutineScope,
    alphaAnimation: Animatable<Float, AnimationVector1D>
) {
    scope.launch {
        alphaAnimation.snapTo(.7f)
        alphaAnimation.animateTo(1f, tween(100))
        delay(12000)
        alphaAnimation.animateTo(0f, tween(2000))
    }
}

private fun animateLongResponseStatus(
    scope: CoroutineScope,
    alphaAnimation: Animatable<Float, AnimationVector1D>
) {
    scope.launch {
        alphaAnimation.snapTo(.7f)
        alphaAnimation.animateTo(1f, tween(100))
        delay(2500)
        alphaAnimation.animateTo(0f, tween(600))
    }
}

private fun animateErrorResponseStatus(
    scope: CoroutineScope,
    alphaAnimation: Animatable<Float, AnimationVector1D>
) {
    scope.launch {
        alphaAnimation.snapTo(.7f)
        alphaAnimation.animateTo(1f, tween(100))
        delay(1500)
        alphaAnimation.animateTo(.7f, tween(1600))
    }
}