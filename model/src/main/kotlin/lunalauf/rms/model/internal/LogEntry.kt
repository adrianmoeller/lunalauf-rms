package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

sealed class LogEntry(
    points: Int,
    timestamp: LocalDateTime
) {
    private val _points = MutableStateFlow(points)
    val points get() = _points.asStateFlow()

    private val _timestamp = MutableStateFlow(timestamp)
    val timestamp get() = _timestamp.asStateFlow()
}