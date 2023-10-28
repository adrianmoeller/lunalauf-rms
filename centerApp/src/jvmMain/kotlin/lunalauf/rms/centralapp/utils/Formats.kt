package lunalauf.rms.centralapp.utils

import java.text.SimpleDateFormat
import kotlin.time.Duration

object Formats {
    val dayTimeFormat = SimpleDateFormat("HH:mm:ss")
    fun minutesFormat(duration: Duration): String {
        var seconds = duration.inWholeSeconds
        if (seconds <= 60)
            return "$seconds sec"
        val minutes = duration.inWholeMinutes
        seconds %= 60
        return "$minutes:$seconds min"
    }
}