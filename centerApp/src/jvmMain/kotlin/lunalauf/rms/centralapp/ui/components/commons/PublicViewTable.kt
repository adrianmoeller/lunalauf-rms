package lunalauf.rms.centralapp.ui.components.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PublicViewTable(
    modifier: Modifier = Modifier,
    header: List<String>,
    data: List<List<String>>,
    weights: List<Float>,
    headerTextStyles: Map<Int, TextStyle>,
    dataTextStyles: Map<Int, TextStyle>,
    showPlacements: Boolean
) {
    val density = LocalDensity.current
    var tableWidth by remember { mutableStateOf(0.sp) }
    val baseFontSize = tableWidth / 40

    Column(
        modifier = modifier
            .onGloballyPositioned {
                tableWidth = with(density) {
                    it.size.width.toSp()
                }
            },
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        var headerHeight by remember { mutableStateOf(0.dp) }

        HeaderTableRow(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape
                )
                .onGloballyPositioned {
                    headerHeight = with(density) {
                        it.size.height.toDp()
                    }
                },
            header = header,
            weights = weights,
            textStyles = headerTextStyles,
            showPlacements = showPlacements,
            spacing = 5.dp,
            fontSize = baseFontSize * 0.8
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val cornerRadius = headerHeight / 2

            data.forEachIndexed { index, item ->
                val placement = when (index) {
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    else -> null
                }
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                    data.lastIndex -> RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                    else -> RectangleShape
                }

                DataTableRow(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = shape
                        ),
                    data = item,
                    weights = weights,
                    textStyles = dataTextStyles,
                    showPlacements = showPlacements,
                    placement = placement,
                    spacing = 5.dp
                )
            }
        }
    }
}

@Composable
private fun HeaderTableRow(
    modifier: Modifier = Modifier,
    header: List<String>,
    weights: List<Float>,
    textStyles: Map<Int, TextStyle>,
    showPlacements: Boolean,
    spacing: Dp,
    fontSize: TextUnit
) {
    val textStyle = TextStyle(fontSize = fontSize)

    Row(
        modifier = modifier
            .padding(vertical = spacing)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPlacements)
            Spacer(Modifier.weight(1f))

        (header zip weights).forEachIndexed { index, it ->
            HeaderTableCell(
                text = it.first,
                weight = it.second,
                textStyle = textStyle.merge(textStyles.getOrDefault(index, LocalTextStyle.current))
            )
        }
    }
}

@Composable
private fun RowScope.HeaderTableCell(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = LocalTextStyle.current,
    weight: Float
) {
    Box(
        modifier = modifier
            .weight(weight),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DataTableRow(
    modifier: Modifier = Modifier,
    data: List<String>,
    weights: List<Float>,
    textStyles: Map<Int, TextStyle>,
    showPlacements: Boolean,
    placement: Int? = null,
    spacing: Dp
) {
    Row(
        modifier = modifier
            .padding(vertical = spacing)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showPlacements) {
            if (placement != null)
                DataTableCell(
                    text = placement.toString(),
                    weight = 1f
                )
            else
                Spacer(Modifier.weight(1f))
        }

        (data zip weights).forEachIndexed { index, it ->
            DataTableCell(
                text = it.first,
                weight = it.second,
                textStyle = textStyles.getOrDefault(index, LocalTextStyle.current)
            )
        }
    }
}

@Composable
private fun RowScope.DataTableCell(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = LocalTextStyle.current,
    weight: Float
) {
    Box(
        modifier = modifier.weight(weight)
    ) {
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
