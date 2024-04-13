package lunalauf.rms.counterapp.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lunalauf.rms.counterapp.ralewayTextStyle
import lunalauf.rms.utilities.network.client.RoundCounter
import lunalauf.rms.utilities.network.communication.ErrorType

@Composable
fun RoundCounterScreen(
    modifier: Modifier = Modifier,
    roundCounter: RoundCounter
) {
    val density = LocalDensity.current
    var containerHeight by remember { mutableStateOf(0.sp) }

    val scanChipScreenModel = remember { ScanChipScreenModel() }

    var responseStatus by remember { mutableStateOf(ResponseStatus.Failed) }
    var acceptedName by remember { mutableStateOf("") }
    var acceptedRound by remember { mutableStateOf("") }
    var rejectedMessage by remember { mutableStateOf("") }
    var failedMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val alphaAnimation = remember { Animatable(0f) }

    LaunchedEffect(roundCounter) {
        alphaAnimation.snapTo(0f)
        roundCounter.setActions(
            accepted = {
                acceptedName = it.name
                acceptedRound = it.newNumRounds.toString()
                responseStatus = ResponseStatus.Accepted
                animateResponseStatus(
                    scope = scope,
                    alphaAnimation = alphaAnimation
                )
            },
            rejected = {
                rejectedMessage = it.causeMessage ?: ""
                responseStatus = ResponseStatus.Rejected
                animateLongResponseStatus(
                    scope = scope,
                    alphaAnimation = alphaAnimation
                )
            },
            failed = {
                failedMessage = it.message
                responseStatus = ResponseStatus.Failed
                if (it == ErrorType.UNKNOWN_ID) {
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
        )
    }

    ScanChipField(
        modifier = modifier.onGloballyPositioned {
            containerHeight = with(density) {
                it.size.height.toSp()
            }
        },
        onNumberKeyEvent = scanChipScreenModel::toIdBuffer,
        onEnterKeyEvent = {
            val chipId = scanChipScreenModel.getBufferedId()
            if (chipId != null) {
                roundCounter.processInput(chipId)
            } else {
                failedMessage = "Scan-Fehler"
                responseStatus = ResponseStatus.Failed
                animateResponseStatus(
                    scope = scope,
                    alphaAnimation = alphaAnimation
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(fraction = .95f)
                .alpha(alphaAnimation.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (responseStatus) {
                ResponseStatus.Accepted -> {
                    Image(
                        modifier = Modifier.weight(1f),
                        painter = painterResource("signals/signal_logo_green.png"),
                        contentDescription = "Status signal",
                        contentScale = ContentScale.FillHeight
                    )
                    Text(
                        text = acceptedName,
                        style = ralewayTextStyle,
                        fontWeight = FontWeight.Bold,
                        fontSize = containerHeight * 0.06
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Runde ",
                            style = ralewayTextStyle,
                            fontWeight = FontWeight.Normal,
                            fontSize = containerHeight * 0.08
                        )
                        Text(
                            text = acceptedRound,
                            style = ralewayTextStyle,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = containerHeight * 0.12
                        )
                    }
                }

                ResponseStatus.Rejected -> {
                    Image(
                        modifier = Modifier.weight(1f),
                        painter = painterResource("signals/signal_logo_orange.png"),
                        contentDescription = "Status signal",
                        contentScale = ContentScale.FillHeight
                    )
                    Text(
                        text = rejectedMessage,
                        style = ralewayTextStyle,
                        fontWeight = FontWeight.Bold,
                        fontSize = containerHeight * 0.06,
                        textAlign = TextAlign.Center
                    )
                }

                ResponseStatus.Failed -> {
                    Image(
                        modifier = Modifier.weight(1f),
                        painter = painterResource("signals/signal_logo_red.png"),
                        contentDescription = "Status signal",
                        contentScale = ContentScale.FillHeight
                    )
                    Text(
                        text = failedMessage,
                        style = ralewayTextStyle,
                        fontWeight = FontWeight.Bold,
                        fontSize = containerHeight * 0.06,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

}

private enum class ResponseStatus {
    Accepted, Rejected, Failed
}

private fun animateResponseStatus(
    scope: CoroutineScope,
    alphaAnimation: Animatable<Float, AnimationVector1D>
) {
    scope.launch {
        alphaAnimation.snapTo(.7f)
        alphaAnimation.animateTo(1f, tween(100))
        delay(1500)
        alphaAnimation.animateTo(0f, tween(600))
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
        alphaAnimation.animateTo(.8f, tween(1600))
    }
}