package lunalauf.rms.model.mapper

import lunalauf.rms.model.internal.*
import lunalauf.rms.model.serialization.*

class InternalModelMapper private constructor(
    private val eventSM: EventSM
) {
    private val runnerUidMapping: MutableMap<Long, Runner> = eventSM.singleRunners
        .associate { Pair(it.uid, toRunner(it)) }
        .toMutableMap()

    private val teamUidMapping = eventSM.teams
        .associate { Pair(it.uid, toTeam(it)) }

    private val teamFunfactorResultsMapping: MutableMap<Team, MutableList<FunfactorResult>> = mutableMapOf()

    companion object {
        fun EventSM.toInternalModel(): Event {
            return InternalModelMapper(this).toEvent()
        }
    }

    private fun toEvent(): Event {
        val event = Event(
            year = eventSM.year,
            runDuration = eventSM.runDuration,
            sponsorPoolAmount = eventSM.sponsorPoolAmount,
            sponsorPoolRounds = eventSM.sponsorPoolRounds,
            additionalContribution = eventSM.additionalContribution,
            teams = eventSM.teams.map { toTeam(it) },
            runners = runnerUidMapping.values.toList(),
            minigames = eventSM.minigames.map { toMinigame(it) },
            challenges = eventSM.challenges.map { toChallenge(it) },
            connections = eventSM.connections.map { toConnectionEntry(it) }
        )

        event.teams.value.forEach { team ->
            team.initSetFunfactorResults(
                teamFunfactorResultsMapping[team]!!
                    .sortedBy { it.timestamp.value })
        }

        return event
    }

    private fun toTeam(teamSM: TeamSM): Team {
        val team = Team(
            amountPerRound = teamSM.amountPerRound,
            amountFix = teamSM.amountFix,
            contributionType = teamSM.contributionType,
            name = teamSM.name
        )

        team.initSetMembers(teamSM.members.map { toRunner(it, team) })
        team.initSetRounds(team.members.value.flatMap { it.rounds.value })

        return team
    }

    private fun toRunner(runnerSM: RunnerSM, team: Team? = null): Runner {
        val runner = Runner(
            amountPerRound = runnerSM.amountPerRound,
            amountFix = runnerSM.amountFix,
            contributionType = runnerSM.contributionType,
            chipId = runnerSM.chipId,
            name = runnerSM.name
        )

        runner.initSetTeam(team)
        runner.initSetRounds(runnerSM.rounds.map { toRound(it, runner, team) })

        runnerUidMapping[runnerSM.uid] = runner

        return runner
    }

    private fun toRound(roundSM: RoundSM, runner: Runner, team: Team? = null): Round {
        val round = Round(
            points = roundSM.points,
            timestamp = roundSM.timestamp,
            manuallyLogged = roundSM.manuallyLogged,
            runner = runner
        )

        round.initSetTeam(team)

        return round
    }

    private fun toMinigame(minigameSM: MinigameSM): Minigame {
        val minigame = Minigame(
            name = minigameSM.name,
            description = minigameSM.description,
            id = minigameSM.id
        )

        minigame.initSetResults(minigameSM.results.map { toFunfactorResult(it, minigame) })

        return minigame
    }

    private fun toChallenge(challengeSM: ChallengeSM): Challenge {
        val challenge = Challenge(
            name = challengeSM.name,
            description = challengeSM.description,
            expires = challengeSM.expires,
            expireMsg = challengeSM.expireMsg,
            duration = challengeSM.duration,
            state = challengeSM.state,
            receiveImages = challengeSM.receiveImages
        )

        challenge.initSetResults(challengeSM.results.map { toFunfactorResult(it, challenge) })

        return challenge
    }

    private fun toFunfactorResult(funfactorResultSM: FunfactorResultSM, funfactor: Funfactor): FunfactorResult {
        val funfactorResult = FunfactorResult(
            points = funfactorResultSM.points,
            timestamp = funfactorResultSM.timestamp,
            team = teamUidMapping[funfactorResultSM.teamUid]!!,
            type = funfactor
        )

        teamFunfactorResultsMapping.computeIfAbsent(funfactorResult.team.value) { mutableListOf() }
            .add(funfactorResult)

        return funfactorResult
    }

    private fun toConnectionEntry(connectionEntrySM: ConnectionEntrySM): ConnectionEntry {
        return ConnectionEntry(
            chatId = connectionEntrySM.chatId,
            runner = runnerUidMapping[connectionEntrySM.runnerUid]!!
        )
    }
}