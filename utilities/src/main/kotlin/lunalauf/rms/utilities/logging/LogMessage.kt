package lunalauf.rms.utilities.logging

import kotlinx.datetime.LocalDateTime

data class LogMessage(
    val level: Lvl,
    val message: String,
    val logger: String,
    val timestamp: LocalDateTime
)