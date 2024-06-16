package lunalauf.rms.utilities.logging

import java.sql.Timestamp

data class LogMessage(
    val level: Lvl,
    val message: String,
    val logger: String,
    val timestamp: Timestamp
)