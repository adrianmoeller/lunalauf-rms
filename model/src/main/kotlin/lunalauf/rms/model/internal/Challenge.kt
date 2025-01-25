package lunalauf.rms.model.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.api.ResetChallengeStateResult
import lunalauf.rms.model.api.StartChallengeResult
import lunalauf.rms.model.api.UpdateChallengeDurationResult
import lunalauf.rms.model.common.ChallengeState
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Challenge internal constructor(
    event: Event,
    name: String,
    description: String,
    expires: Boolean,
    expireMsg: String,
    duration: Int,
    state: ChallengeState,
    receiveImages: Boolean
) : Funfactor(
    event,
    name,
    description
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(Dispatchers.Default)

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

    suspend fun updateExpires(expires: Boolean) {
        event.mutex.withLock {
            _expires.update { expires }
        }
    }

    suspend fun updateDuration(duration: Int): UpdateChallengeDurationResult {
        event.mutex.withLock {
            if (duration < 0) {
                logger.warn("Missing UI check if duration is positive when updating a challenge")
                return UpdateChallengeDurationResult.NegativeDuration
            }

            _duration.update { duration }
            return UpdateChallengeDurationResult.Updated
        }
    }

    suspend fun updateExpireMessage(expireMsg: String) {
        event.mutex.withLock {
            _expireMsg.update { expireMsg }
        }
    }

    suspend fun updateReceiveImage(receiveImage: Boolean) {
        event.mutex.withLock {
            _receiveImages.update { receiveImage }
        }
    }

    suspend fun start(
        onSendTeamMessage: (String) -> Boolean,
        onStartAcceptImages: ((Team) -> Boolean) -> Unit,
        onCompleted: () -> Unit
    ): StartChallengeResult {
        event.mutex.withLock {
            if (state.value != ChallengeState.PENDING) {
                logger.warn("Missing UI check if state is pending when starting challenge")
                return StartChallengeResult.AlreadyStarted
            }

            val challengeHeader = "<b><u>Funfactor:</u> ${name.value}</b>"
            val challengeStartText = "$challengeHeader\n${description.value}"

            if (!onSendTeamMessage(challengeStartText))
                return StartChallengeResult.SendMessageFailed

            if (expires.value) {
                _state.update { ChallengeState.STARTED }
                startTimer(
                    challengeHeader = challengeHeader,
                    onSendTeamMessage = onSendTeamMessage,
                    onStartAcceptImages = onStartAcceptImages,
                    onCompleted = onCompleted
                )
            } else {
                _state.update { ChallengeState.COMPLETED }
            }

            return StartChallengeResult.Started
        }
    }

    private fun startTimer(
        challengeHeader: String,
        onSendTeamMessage: (String) -> Boolean,
        onStartAcceptImages: ((Team) -> Boolean) -> Unit,
        onCompleted: () -> Unit
    ) {
        val teamsOfImageSent = mutableSetOf<Team>()
        onStartAcceptImages { teamsOfImageSent.add(it) }

        val duration = duration.value.minutes
        scope.launch {
            delay(duration - 30.seconds)

            val message = "$challengeHeader\nNoch 30 Sekunden übrig!"
            if (!onSendTeamMessage(message))
                logger.error("Could not send '30 sec left'-message: {}", this)
        }
        scope.launch {
            delay(duration)

            val message = "$challengeHeader\n${expireMsg.value}"
            withContext(ModelState.modelContext) {
                event.mutex.withLock {
                    _state.update { ChallengeState.COMPLETED }
                }
            }
            if (!onSendTeamMessage(message))
                logger.error("Could not send 'completed'-message: {}", this)
        }
        scope.launch {
            delay(duration + 2.seconds)

            onCompleted()
        }
    }

    suspend fun resetState(): ResetChallengeStateResult {
        event.mutex.withLock {
            if (state.value != ChallengeState.COMPLETED) {
                logger.warn("Missing UI check if state is completed when resetting challenge state")
                return ResetChallengeStateResult.NotCompleted
            }

            _state.update { ChallengeState.PENDING }
            
            return ResetChallengeStateResult.Reset
        }
    }
}
