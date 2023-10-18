package lunalauf.rms.centralapp

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object FontFamilies {
    val NotoSans = FontFamily(
        Font("fonts/NotoSans/NotoSans-Black.ttf", FontWeight.Black, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-ExtraBold.ttf", FontWeight.ExtraBold, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-ExtraLight.ttf", FontWeight.ExtraLight, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-Light.ttf", FontWeight.Light, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-Medium.ttf", FontWeight.Medium, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-Thin.ttf", FontWeight.Thin, FontStyle.Normal),
        Font("fonts/NotoSans/NotoSans-BlackItalic.ttf", FontWeight.Black, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-ExtraBoldItalic.ttf", FontWeight.Bold, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-ExtraLightItalic.ttf", FontWeight.ExtraLight, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-LightItalic.ttf", FontWeight.Light, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-MediumItalic.ttf", FontWeight.Medium, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-RegularItalic.ttf", FontWeight.Normal, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-SemiBoldItalic.ttf", FontWeight.SemiBold, FontStyle.Italic),
        Font("fonts/NotoSans/NotoSans-ThinItalic.ttf", FontWeight.Thin, FontStyle.Italic),
    )
}

val notoSansTextStyle = TextStyle(fontFamily = FontFamilies.NotoSans)

val publicViewTypography = Typography(
    displayLarge = notoSansTextStyle,
    displayMedium = notoSansTextStyle,
    displaySmall = notoSansTextStyle,
    headlineLarge = notoSansTextStyle,
    headlineMedium = notoSansTextStyle,
    headlineSmall = notoSansTextStyle,
    titleLarge = notoSansTextStyle,
    titleMedium = notoSansTextStyle,
    titleSmall = notoSansTextStyle,
    bodyLarge = notoSansTextStyle,
    bodyMedium = notoSansTextStyle,
    bodySmall = notoSansTextStyle,
    labelLarge = notoSansTextStyle,
    labelMedium = notoSansTextStyle,
    labelSmall = notoSansTextStyle
)
