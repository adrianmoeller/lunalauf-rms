package lunalauf.rms.counterapp.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.delay

@Composable
fun ScanChipField(
    modifier: Modifier = Modifier,
    onNumberKeyEvent: (Int) -> Unit,
    onEnterKeyEvent: () -> Unit,
    content: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var readyForScanning by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(
            modifier = Modifier
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
            if (readyForScanning)
                content()
            else
                CircularProgressIndicator()
        }
    }

    LaunchedEffect(true) {
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

private fun parseNumberKey(key: Key): Int? {
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
    }
}