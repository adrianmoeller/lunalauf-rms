package lunalauf.rms.modelapi.resource

import LunaLaufLanguage.*
import kotlinx.datetime.toKotlinLocalDateTime
import lunalauf.rms.model.common.ContributionType
import lunalauf.rms.model.common.RunTimerState
import lunalauf.rms.model.serialization.*
import lunalauf.rms.modelapi.ModelAPI
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.RunTimer

class LunaLaufToSerializationModelMapper private constructor(
    modelState: ModelState.Loaded
) {
    private val model: LunaLauf = modelState.model
    private val modelApi: ModelAPI = modelState.modelAPI

    private var uidCounter = 0L
    private val runnerUidMapping = model.runners.associateWith { uidCounter++ }
    private val teamUidMapping = model.teams.associateWith { uidCounter++ }

    companion object {
        fun ModelState.Loaded.toSerializationModel(): EventSM {
            return LunaLaufToSerializationModelMapper(this).toEventSM()
        }
    }

    private fun toEventSM(): EventSM {
        return EventSM(
            year = model.year,
            runDuration = model.runDuration,
            sponsorPoolAmount = model.sponsorPoolAmount,
            sponsorPoolRounds = model.sponsorPoolRounds,
            additionalContribution = model.additionalContribution,
            roundThreshold = modelApi.roundThreshold,
            teams = model.teams.map { toTeamSM(it) },
            singleRunners = model.runners
                .filter { it.team == null }
                .map { toRunnerSM(it) },
            minigames = model.minigames.map { toMinigameSM(it) },
            challenges = model.challenges.map { toChallengesSM(it) },
            connections = model.connections.map { toConnectionSM(it) },
            runTimer = toRunTimerSM()
        )
    }

    private fun toTeamSM(team: Team): TeamSM {
        return TeamSM(
            uid = teamUidMapping.getValue(team),
            amountPerRound = team.amountPerRound,
            amountFix = team.amountFix,
            contributionType = toContributionType(team.contribution),
            name = team.name ?: "",
            members = team.members.map { toRunnerSM(it) }
        )
    }

    private fun toRunnerSM(runner: Runner): RunnerSM {
        return RunnerSM(
            uid = runnerUidMapping.getValue(runner),
            amountPerRound = runner.amountPerRound,
            amountFix = runner.amountFix,
            contributionType = toContributionType(runner.contribution),
            chipId = runner.id,
            name = runner.name ?: "",
            rounds = runner.rounds.map { toRoundSM(it) }
        )
    }

    private fun toContributionType(contributionType: ContrType): ContributionType {
        return when (contributionType) {
            ContrType.PERROUND -> ContributionType.PER_ROUND
            ContrType.FIXED -> ContributionType.FIXED
            ContrType.BOTH -> ContributionType.BOTH
            ContrType.NONE -> ContributionType.NONE
        }
    }

    private fun toRoundSM(round: Round): RoundSM {
        return RoundSM(
            points = round.points,
            timestamp = round.timestamp.toLocalDateTime().toKotlinLocalDateTime(),
            manuallyLogged = round.isManualLogged
        )
    }

    private fun toMinigameSM(minigame: Minigame): MinigameSM {
        return MinigameSM(
            name = minigame.name ?: "",
            description = minigame.description ?: "",
            results = minigame.funfactorResults.map { toFunfactorResultSM(it) },
            id = minigame.minigameID
        )
    }

    private fun toChallengesSM(challenge: Challenge): ChallengeSM {
        return ChallengeSM(
            name = challenge.name ?: "",
            description = challenge.description ?: "",
            results = challenge.funfactorResults.map { toFunfactorResultSM(it) },
            expires = challenge.isExpires,
            expireMsg = challenge.expireMsg ?: "",
            duration = challenge.duration,
            state = toChallengeState(challenge.state),
            receiveImages = challenge.isReceiveImages
        )
    }

    private fun toChallengeState(challengeState: ChallengeState): lunalauf.rms.model.common.ChallengeState {
        return when (challengeState) {
            ChallengeState.PENDING -> lunalauf.rms.model.common.ChallengeState.PENDING
            ChallengeState.STARTED -> lunalauf.rms.model.common.ChallengeState.STARTED
            ChallengeState.COMPLETED -> lunalauf.rms.model.common.ChallengeState.COMPLETED
        }
    }

    private fun toFunfactorResultSM(result: FunfactorResult): FunfactorResultSM {
        return FunfactorResultSM(
            points = result.points,
            timestamp = result.timestamp.toLocalDateTime().toKotlinLocalDateTime(),
            teamUid = teamUidMapping.getValue(result.team)
        )
    }

    private fun toConnectionSM(connection: ConnectionEntry): ConnectionEntrySM {
        return ConnectionEntrySM(
            chatId = connection.chatId,
            runnerUid = runnerUidMapping.getValue(connection.runner)
        )
    }

    private fun toRunTimerSM(): RunTimerSM {
        return RunTimerSM(
            runEnabled = modelApi.runEnabled.value,
            runDryPhase = modelApi.runDryPhase.value,
            remainingTime = modelApi.runTimer.remainingTime.value,
            state = toRunTimerState(modelApi.runTimer.state.value)
        )
    }

    private fun toRunTimerState(state: RunTimer.State): RunTimerState {
        return when (state) {
            RunTimer.State.RUNNING -> RunTimerState.RUNNING
            RunTimer.State.PAUSED -> RunTimerState.PAUSED
            RunTimer.State.EXPIRED -> RunTimerState.EXPIRED
        }
    }
}