package lunalauf.rms.counterapp

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

object FontFamilies {
    val Raleway = FontFamily(
        Font("fonts/Raleway/Raleway-Regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("fonts/Raleway/Raleway-Bold.ttf", FontWeight.Bold, FontStyle.Normal),
        Font("fonts/Raleway/Raleway-ExtraBold.ttf", FontWeight.ExtraBold, FontStyle.Normal)
    )

    val Nunito = FontFamily(
        Font("fonts/Nunito/NunitoSans_7pt-Black.ttf", FontWeight.Black, FontStyle.Normal)
    )
}

val ralewayTextStyle = TextStyle(fontFamily = FontFamilies.Raleway)
val nunitoTextStyle = TextStyle(fontFamily = FontFamilies.Nunito)