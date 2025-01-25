package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime
import lunalauf.rms.model.api.DeleteElementResult

sealed class LogEntry(
    event: Event,
    points: Int,
    timestamp: LocalDateTime
) : EventChild(
    event
) {
    private val _points = MutableStateFlow(points)
    val points get() = _points.asStateFlow()

    private val _timestamp = MutableStateFlow(timestamp)
    val timestamp get() = _timestamp.asStateFlow()

    abstract suspend fun delete(): DeleteElementResult
}