package lunalauf.rms.centralapp.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration

object Formats {
    val dayTimeFormat = SimpleDateFormat("HH:mm:ss")

    fun minutesFormat(duration: Duration): String {
        var seconds = duration.inWholeSeconds
        if (seconds <= 60)
            return "$seconds sec"
        val minutes = duration.inWholeMinutes
        seconds %= 60
        return "$minutes:${seconds.toString().padStart(2, '0')} min"
    }

    fun germanEuroFormat(value: Double): String {
        return try {
            val string = value.toString()
            val split = string.split(".")
            var padded = split.last().padEnd(2, '0')
            if (padded.length > 2)
                padded = padded.substring(0..1)
            "${split.first()},$padded €"
        } catch (_: Exception) {
            "None"
        }
    }

    fun clockFormat(duration: Duration): String {
        val clockFormatter = SimpleDateFormat("HH:mm:ss")
            .also { it.timeZone = TimeZone.getTimeZone("GMT") }
        return clockFormatter.format(Date(duration.inWholeMilliseconds))
    }
}