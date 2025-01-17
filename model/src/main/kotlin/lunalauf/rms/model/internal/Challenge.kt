package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.model.common.ChallengeState

class Challenge internal constructor(
    name: String,
    description: String,
    expires: Boolean,
    expireMsg: String,
    duration: Int,
    state: ChallengeState,
    receiveImages: Boolean
) : Funfactor(
    name,
    description
) {
    private val _expires = MutableStateFlow(expires)
    val expires get() = _expires.asStateFlow()

    private val _expireMsg = MutableStateFlow(expireMsg)
    val expireMsg get() = _expireMsg.asStateFlow()

    private val _duration = MutableStateFlow(duration)
    val duration get() = _duration.asStateFlow()

    private val _state = MutableStateFlow(state)
    val state get() = _state.asStateFlow()

    private val _receiveImages = MutableStateFlow(receiveImages)
    val receiveImages get() = _receiveImages.asStateFlow()
}
