package lunalauf.rms.model.mapper

import lunalauf.rms.model.internal.*
import lunalauf.rms.model.serialization.*

class SerializationModelMapper private constructor(
    private val event: Event
) {
    private var uidCounter = 0L
    private val runnerUidMapping = event.runners.value.associateWith { uidCounter++ }
    private val teamUidMapping = event.teams.value.associateWith { uidCounter++ }

    companion object {
        fun Event.toSerializationModel(): EventSM {
            return SerializationModelMapper(this).toEventSM()
        }
    }

    private fun toEventSM(): EventSM {
        return EventSM(
            year = event.year.value,
            runDuration = event.runDuration.value,
            sponsorPoolAmount = event.sponsorPoolAmount.value,
            sponsorPoolRounds = event.sponsorPoolRounds.value,
            additionalContribution = event.additionalContribution.value,
            teams = event.teams.value.map { toTeamSM(it) },
            singleRunners = event.runners.value
                .filter { it.team.value != null }
                .map { toRunnerSM(it) },
            minigames = event.minigames.value.map { toMinigameSM(it) },
            challenges = event.challenges.value.map { toChallengesSM(it) },
            connections = event.connections.value.map { toConnectionSM(it) },
            runTimer = toRunTimerSM(event.runTimer)
        )
    }

    private fun toTeamSM(team: Team): TeamSM {
        return TeamSM(
            uid = teamUidMapping.getValue(team),
            amountPerRound = team.amountPerRound.value,
            amountFix = team.amountFix.value,
            contributionType = team.contributionType.value,
            name = team.name.value,
            members = team.members.value.map { toRunnerSM(it) }
        )
    }

    private fun toRunnerSM(runner: Runner): RunnerSM {
        return RunnerSM(
            uid = runnerUidMapping.getValue(runner),
            amountPerRound = runner.amountPerRound.value,
            amountFix = runner.amountFix.value,
            contributionType = runner.contributionType.value,
            chipId = runner.chipId.value,
            name = runner.name.value,
            rounds = runner.rounds.value.map { toRoundSM(it) }
        )
    }

    private fun toRoundSM(round: Round): RoundSM {
        return RoundSM(
            points = round.points.value,
            timestamp = round.timestamp.value,
            manuallyLogged = round.manuallyLogged.value
        )
    }

    private fun toMinigameSM(minigame: Minigame): MinigameSM {
        return MinigameSM(
            name = minigame.name.value,
            description = minigame.description.value,
            results = minigame.results.value.map { toFunfactorResultSM(it) },
            id = minigame.id.value
        )
    }

    private fun toChallengesSM(challenge: Challenge): ChallengeSM {
        return ChallengeSM(
            name = challenge.name.value,
            description = challenge.description.value,
            results = challenge.results.value.map { toFunfactorResultSM(it) },
            expires = challenge.expires.value,
            expireMsg = challenge.expireMsg.value,
            duration = challenge.duration.value,
            state = challenge.state.value,
            receiveImages = challenge.receiveImages.value
        )
    }

    private fun toFunfactorResultSM(result: FunfactorResult): FunfactorResultSM {
        return FunfactorResultSM(
            points = result.points.value,
            timestamp = result.timestamp.value,
            teamUid = teamUidMapping.getValue(result.team.value)
        )
    }

    private fun toConnectionSM(connection: ConnectionEntry): ConnectionEntrySM {
        return ConnectionEntrySM(
            chatId = connection.chatId.value,
            runnerUid = runnerUidMapping.getValue(connection.runner.value)
        )
    }

    private fun toRunTimerSM(runTimer: RunTimer): RunTimerSM {
        return RunTimerSM(
            runEnabled = runTimer.runEnabled.value,
            runDryPhase = runTimer.runDryPhase.value,
            remainingTime = runTimer.remainingTime.value,
            state = runTimer.state.value
        )
    }
}