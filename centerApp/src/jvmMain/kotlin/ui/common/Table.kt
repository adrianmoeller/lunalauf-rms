package ui.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Sort
import compose.icons.fontawesomeicons.solid.SortDown
import compose.icons.fontawesomeicons.solid.SortUp

@Composable
fun Table(
    modifier: Modifier = Modifier,
    header: List<String>,
    data: List<List<String>>,
    weights: List<Float>,
    spacing: Dp = 10.dp
) {
    val state = rememberLazyListState()
    var sortMode by remember { mutableStateOf(0) }
    var sortIndex by remember { mutableStateOf(0) }

    val sortedData = when (sortMode) {
        1 -> data.sortedBy { it[sortIndex] }
        -1 -> data.sortedByDescending { it[sortIndex] }
        else -> data
    }

    Column(
        modifier = modifier
            .padding(spacing)
    ) {
        HeaderTableRow(
            header = header,
            weights = weights,
            spacing = spacing,
            onSortClicked = {
                if (it == sortIndex) {
                    sortMode = (sortMode + 1)
                    if (sortMode > 1)
                        sortMode = -1
                } else {
                    sortMode = 1
                }
                sortIndex = it

            },
            sortMode = sortMode,
            sortIndex = sortIndex
        )
        Box {
            LazyColumn(
                state = state
            ) {
                itemsIndexed(sortedData) { index, item ->
                    DataTableRow(
                        data = item,
                        weights = weights,
                        spacing = spacing
                    )
                    if (index < data.lastIndex)
                        ListItemDivider(spacing = spacing)
                }
            }
            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(state),
                style = customScrollbarStyle
            )
        }
    }
}

@Composable
private fun HeaderTableRow(
    header: List<String>,
    weights: List<Float>,
    spacing: Dp,
    onSortClicked: (Int) -> Unit,
    sortMode: Int,
    sortIndex: Int
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = spacing)
            .fillMaxWidth()
    ) {
        (header zip weights).forEachIndexed { index, it ->
            HeaderTableCell(
                text = it.first,
                textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                weight = it.second,
                spacing = spacing,
                onSortClicked = { onSortClicked(index) },
                sortMode = if (index == sortIndex) sortMode else 0
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RowScope.HeaderTableCell(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.Unspecified,
    weight: Float,
    spacing: Dp,
    onSortClicked: () -> Unit,
    sortMode: Int
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .weight(weight)
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = spacing)
                .weight(1f),
            text = text,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (hovered)
            Row {
                Icon(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSortClicked() }
                        .padding(3.dp)
                        .size(20.dp),
                    imageVector = when (sortMode) {
                        1 -> FontAwesomeIcons.Solid.SortDown
                        -1 -> FontAwesomeIcons.Solid.SortUp
                        else -> FontAwesomeIcons.Solid.Sort
                    },
                    tint = textColor,
                    contentDescription = "Sort"
                )
                Spacer(Modifier.padding(horizontal = spacing / 2))
            }
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DataTableRow(
    data: List<String>,
    weights: List<Float>,
    spacing: Dp
) {
    var hovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .onPointerEvent(PointerEventType.Enter) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .cond(hovered) {
                background(
                    color = listItemHoverColor,
                    shape = MaterialTheme.shapes.medium
                )
            }
            .padding(spacing)
            .fillMaxWidth()
    ) {
        (data zip weights).forEach {
            DataTableCell(
                text = it.first,
                weight = it.second
            )
        }
    }
}

@Composable
private fun RowScope.DataTableCell(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color.Unspecified,
    weight: Float
) {
    Box(
        modifier = modifier.weight(weight)
    ) {
        Text(
            text = text,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun PreviewTable() {
    MaterialTheme {
        Table(
            header = listOf("Header1", "Header2", "Header3"),
            data = listOf(
                listOf("a1", "a2", "a3"),
                listOf("a1", "a2", "a3"),
                listOf("a1", "a2", "a3"),
                listOf("c1", "c2", "c3"),
                listOf("a1", "a2", "a3"),
                listOf("f1", "a2", "a3"),
                listOf("a1", "a2", "a3"),
                listOf("a1", "a2", "d3"),
                listOf("a1", "a2", "a3"),
                listOf("b1", "r2", "b3"),
                listOf("b1", "b2", "b3"),
                listOf("a1", "a2", "a3"),
                listOf("b1", "b2", "b3"),
                listOf("b1", "b2", "b3")
            ),
            weights = listOf(2f, 1f, 1f)
        )
    }
}