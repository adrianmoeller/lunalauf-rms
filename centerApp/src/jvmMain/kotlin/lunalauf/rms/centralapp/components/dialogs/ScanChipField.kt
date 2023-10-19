package lunalauf.rms.centralapp.components.dialogs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import compose.icons.EvaIcons
import compose.icons.FontAwesomeIcons
import compose.icons.evaicons.Outline
import compose.icons.evaicons.outline.Shake
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationTriangle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lunalauf.rms.centralapp.components.commons.IconSize

@Composable
fun ScanChipField(
    modifier: Modifier = Modifier,
    showError: Boolean,
    errorText: String = ScanChipFieldDefaults.errorText,
    onNumberKeyEvent: (UInt) -> Unit,
    onEnterKeyEvent: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    var readyForScanning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
        if (showError) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(IconSize.small),
                        imageVector = FontAwesomeIcons.Solid.ExclamationTriangle,
                        contentDescription = null
                    )
                    Text(errorText)
                }
            }
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

object ScanChipFieldDefaults {
    val errorText = "Invalid ID format"
}
