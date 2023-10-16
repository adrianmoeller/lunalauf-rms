package lunalauf.rms.centralapp.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.FontAwesomeIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Shake
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Spinner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScanChipField(
    modifier: Modifier = Modifier,
    onNumberKeyEvent: (UInt) -> Unit,
    onEnterKeyEvent: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var readyForScanning by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier
            .onPreviewKeyEvent {
                if (it.type == KeyEventType.KeyUp) {
                    return@onPreviewKeyEvent when (it.key) {
                        Key.Enter -> {
                            onEnterKeyEvent()
                            true
                        }

                        else -> {
                            val parsedKey = parseNumberKey(it.key)
                            if (parsedKey == null) {
                                false
                            } else {
                                onNumberKeyEvent(parsedKey)
                                true
                            }
                        }
                    }
                }
                return@onPreviewKeyEvent false
            }
            .onFocusChanged { readyForScanning = it.isCaptured }
            .focusRequester(focusRequester)
            .focusable(true),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        vertical = 10.dp,
                        horizontal = 30.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    modifier = Modifier
                        .size(60.dp)
                        .alpha(if (readyForScanning) 1f else 0f),
                    imageVector = EvaIcons.Outline.Shake,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.alpha(if (readyForScanning) 1f else 0f),
                    text = "Scan chip"
                )
            }
            if (!readyForScanning)
                CircularProgressIndicator()
        }
    }

    coroutineScope.launch {
        while (true) {
            try {
                focusRequester.requestFocus()
                focusRequester.captureFocus()
                break
            } catch (_: IllegalStateException) {
            } catch (e: Exception) {
                break
            }
            delay(100)
        }
    }
}

private fun parseNumberKey(key: Key): UInt? {
    return when (key) {
        Key.One -> 1
        Key.Two -> 2
        Key.Three -> 3
        Key.Four -> 4
        Key.Five -> 5
        Key.Six -> 6
        Key.Seven -> 7
        Key.Eight -> 8
        Key.Nine -> 9
        Key.Zero -> 0
        else -> null
    }?.toUInt()
}
