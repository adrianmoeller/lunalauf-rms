package lunalauf.rms.centralapp.components.dialogs.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import lunalauf.rms.centralapp.components.commons.OptionTile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesSheet(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    autoSaveActive: Boolean,
    onAutoSaveActiveChange: (Boolean) -> Unit,
    autoSaveInterval: Float,
    onAutoSaveIntervalChange: (Float) -> Unit,
    onAutoSaveIntervalChangeFinished: () -> Unit,
    roundThreshold: Float,
    onRoundThresholdChange: (Float) -> Unit,
    onRoundThresholdChangeFinished: () -> Unit,
    saveConnectionsActive: Boolean,
    onSaveConnectionsActiveChange: (Boolean) -> Unit
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onClose,
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
                text = "Settings",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(20.dp))
            OutlinedCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                ) {
                    OptionTile(
                        text = "Autosave",
                        checked = autoSaveActive,
                        onCheckedChange = onAutoSaveActiveChange
                    )
                    if (autoSaveActive)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Interval: ${autoSaveInterval.toInt()} sec")
                            Spacer(Modifier.width(10.dp))
                            Slider(
                                modifier = Modifier.widthIn(max = 350.dp),
                                value = autoSaveInterval,
                                onValueChange = onAutoSaveIntervalChange,
                                onValueChangeFinished = onAutoSaveIntervalChangeFinished,
                                valueRange = 10f..30f,
                                steps = 3,
                            )
                        }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Round threshold: ${roundThreshold.toInt()} sec")
                        Spacer(Modifier.width(10.dp))
                        Slider(
                            modifier = Modifier.widthIn(max = 350.dp),
                            value = roundThreshold,
                            onValueChange = onRoundThresholdChange,
                            onValueChangeFinished = onRoundThresholdChangeFinished,
                            valueRange = 20f..80f,
                            steps = 11,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedCard {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 15.dp,
                        vertical = 10.dp
                    )
                ) {
                    OptionTile(
                        text = "Save connection data",
                        checked = saveConnectionsActive,
                        onCheckedChange = onSaveConnectionsActiveChange
                    )
                }
            }
        }
    }
}