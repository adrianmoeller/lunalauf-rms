package lunalauf.rms.centralapp.components.commons

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.ExclamationTriangle

@Composable
fun RowScope.TabNavigationBarItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        label = { Text(tab.options.title) },
        icon = {
            Icon(
                modifier = Modifier.size(IconSize.small),
                painter = tab.options.icon ?: rememberVectorPainter(FontAwesomeIcons.Solid.ExclamationTriangle),
                contentDescription = null
            )
        },
        selected = tabNavigator.current == tab,
        onClick = { tabNavigator.current = tab }
    )
}