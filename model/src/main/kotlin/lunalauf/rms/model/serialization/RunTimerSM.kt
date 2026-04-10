package lunalauf.rms.model.serialization

import kotlinx.serialization.Serializable
import lunalauf.rms.model.common.RunTimerState

@Serializable
data class RunTimerSM(
    val runEnabled: Boolean,
    val runDryPhase: Boolean,
    val remainingTime: Long,
    val state: RunTimerState
)