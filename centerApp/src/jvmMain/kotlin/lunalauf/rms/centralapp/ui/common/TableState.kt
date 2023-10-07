package lunalauf.rms.centralapp.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberTableState(
    header: List<String> = emptyList(),
    data: List<List<String>> = emptyList(),
    weights: List<Float> = emptyList()
): TableState {
    return rememberSaveable() {
        TableState(header, data, weights)
    }
}

data class TableState(
    val header: List<String> = emptyList(),
    val data: List<List<String>> = emptyList(),
    val weights: List<Float> = emptyList()
) {
    companion object {

    }
}
