package lunalauf.rms.modelapi

import LunaLaufLanguage.Challenge
import LunaLaufLanguage.ChallengeState
import LunaLaufLanguage.Team
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ChallengeTimer(
    private val mutex: Mutex
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(Dispatchers.Default)

    fun start(
        challenge: Challenge,
        challengeHeader: String,
        onSendTeamMessage: (String) -> Boolean,
        onStartAcceptImages: ((Team) -> Boolean) -> Unit,
        onCompleted: () -> Unit
    ) {
        val teamsOfImageSent = mutableSetOf<Team>()
        onStartAcceptImages { teamsOfImageSent.add(it) }

        val duration = challenge.duration.minutes
        scope.launch {
            delay(duration - 30.seconds)

            val message = "$challengeHeader\nNoch 30 Sekunden übrig!"
            if (!onSendTeamMessage(message))
                logger.error("Could not send '30 sec left'-message: {}", challenge)
        }
        scope.launch {
            delay(duration)

            val message = "$challengeHeader\n${challenge.expireMsg}"
            withContext(ModelAPI.modelContext) {
                mutex.withLock {
                    challenge.state = ChallengeState.COMPLETED
                }
            }
            if (!onSendTeamMessage(message))
                logger.error("Could not send 'completed'-message: {}", challenge)
        }
        scope.launch {
            delay(duration + 2.seconds)

            onCompleted()
        }
    }
}
