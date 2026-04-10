package lunalauf.rms.model.common

import kotlinx.serialization.Serializable

@Serializable
enum class RunTimerState {
    RUNNING, PAUSED, EXPIRED
}