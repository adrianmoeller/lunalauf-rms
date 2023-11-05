package lunalauf.rms.centralapp.components.commons

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun PublicViewTable(
    modifier: Modifier = Modifier,
    header: List<String>,
    data: List<List<String>>,
    weights: List<Float>,
    headerTextStyles: Map<Int, TextStyle>,
    dataTextStyles: Map<Int, TextStyle>,
    showPlacements: Boolean,
    placementsWeight: Float = .5f,
    baseFontSize: TextUnit
) {
    val density = LocalDensity.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        var headerHeight by remember { mutableStateOf(0.dp) }
        val cornerRadius = headerHeight / 2

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
            placementsWeight = placementsWeight,
            cornerRadius = cornerRadius,
            spacing = 5.dp,
            fontSize = baseFontSize * 0.8
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
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
                    placementsWeight = placementsWeight,
                    cornerRadius = cornerRadius,
                    spacing = 0.dp,
                    fontSize = baseFontSize
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
    placementsWeight: Float,
    cornerRadius: Dp,
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
            Spacer(Modifier.weight(placementsWeight))
        else
            Spacer(Modifier.width(cornerRadius))

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
    placementsWeight: Float,
    cornerRadius: Dp,
    spacing: Dp,
    fontSize: TextUnit
) {
    val textStyle = TextStyle(fontSize = fontSize)
    val placementsTextStyle = TextStyle(fontSize = fontSize * 0.9)

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
                    weight = placementsWeight,
                    textStyle = placementsTextStyle.merge(TextStyle(fontWeight = FontWeight.SemiBold)),
                    alignment = Alignment.TopCenter
                )
            else
                Spacer(Modifier.weight(placementsWeight))
        } else {
            Spacer(Modifier.width(cornerRadius))
        }

        (data zip weights).forEachIndexed { index, it ->
            DataTableCell(
                text = it.first,
                weight = it.second,
                textStyle = textStyle.merge(textStyles.getOrDefault(index, LocalTextStyle.current))
            )
        }
    }
}

@Composable
private fun RowScope.DataTableCell(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = LocalTextStyle.current,
    weight: Float,
    alignment: Alignment = Alignment.TopStart
) {
    Box(
        modifier = modifier.weight(weight),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
