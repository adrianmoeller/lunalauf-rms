package lunalauf.rms.model.helper

import kotlinx.datetime.*

internal object Timestamps {
    fun now() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

    fun LocalDateTime.toMilliseconds() = this.toInstant(TimeZone.UTC).toEpochMilliseconds()
}