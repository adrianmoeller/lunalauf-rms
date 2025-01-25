package lunalauf.rms.model.mapper

import lunalauf.rms.model.internal.*
import lunalauf.rms.model.serialization.*

class InternalModelMapper private constructor(
    private val eventSM: EventSM
) {
    private lateinit var runnerUidMapping: MutableMap<Long, Runner>
    private lateinit var teamUidMapping: Map<Long, Team>

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
            roundThreshold = eventSM.roundThreshold
        )

        runnerUidMapping = eventSM.singleRunners
            .associate { Pair(it.uid, toRunner(event, it)) }
            .toMutableMap()

        teamUidMapping = eventSM.teams
            .associate { Pair(it.uid, toTeam(event, it)) }

        event.initSetTeams(teamUidMapping.values.toList())
        event.initSetRunners(runnerUidMapping.values.toList())
        event.initSetMinigames(eventSM.minigames.map { toMinigame(event, it) })
        event.initSetChallenges(eventSM.challenges.map { toChallenge(event, it) })
        event.initSetConnections(eventSM.connections.map { toConnectionEntry(event, it) })
        event.initSetRunTimer(toRunTimer(eventSM.runTimer))

        event.teams.value.forEach { team ->
            team.initSetFunfactorResults(
                teamFunfactorResultsMapping[team]!!
                    .sortedBy { it.timestamp.value })
        }

        return event
    }

    private fun toTeam(event: Event, teamSM: TeamSM): Team {
        val team = Team(
            event = event,
            amountPerRound = teamSM.amountPerRound,
            amountFix = teamSM.amountFix,
            contributionType = teamSM.contributionType,
            name = teamSM.name
        )

        team.initSetMembers(teamSM.members.map { toRunner(event, it, team) })
        team.initSetRounds(team.members.value.flatMap { it.rounds.value })

        return team
    }

    private fun toRunner(event: Event, runnerSM: RunnerSM, team: Team? = null): Runner {
        val runner = Runner(
            event = event,
            amountPerRound = runnerSM.amountPerRound,
            amountFix = runnerSM.amountFix,
            contributionType = runnerSM.contributionType,
            chipId = runnerSM.chipId,
            name = runnerSM.name
        )

        runner.initSetTeam(team)
        runner.initSetRounds(runnerSM.rounds.map { toRound(event, it, runner, team) })

        runnerUidMapping[runnerSM.uid] = runner

        return runner
    }

    private fun toRound(event: Event, roundSM: RoundSM, runner: Runner, team: Team? = null): Round {
        val round = Round(
            event = event,
            points = roundSM.points,
            timestamp = roundSM.timestamp,
            manuallyLogged = roundSM.manuallyLogged,
            runner = runner
        )

        round.initSetTeam(team)

        return round
    }

    private fun toMinigame(event: Event, minigameSM: MinigameSM): Minigame {
        val minigame = Minigame(
            event = event,
            name = minigameSM.name,
            description = minigameSM.description,
            id = minigameSM.id
        )

        minigame.initSetResults(minigameSM.results.map { toFunfactorResult(event, it, minigame) })

        return minigame
    }

    private fun toChallenge(event: Event, challengeSM: ChallengeSM): Challenge {
        val challenge = Challenge(
            event = event,
            name = challengeSM.name,
            description = challengeSM.description,
            expires = challengeSM.expires,
            expireMsg = challengeSM.expireMsg,
            duration = challengeSM.duration,
            state = challengeSM.state,
            receiveImages = challengeSM.receiveImages
        )

        challenge.initSetResults(challengeSM.results.map { toFunfactorResult(event, it, challenge) })

        return challenge
    }

    private fun toFunfactorResult(
        event: Event,
        funfactorResultSM: FunfactorResultSM,
        funfactor: Funfactor
    ): FunfactorResult {
        val funfactorResult = FunfactorResult(
            event = event,
            points = funfactorResultSM.points,
            timestamp = funfactorResultSM.timestamp,
            team = teamUidMapping[funfactorResultSM.teamUid]!!,
            type = funfactor
        )

        teamFunfactorResultsMapping.computeIfAbsent(funfactorResult.team.value) { mutableListOf() }
            .add(funfactorResult)

        return funfactorResult
    }

    private fun toConnectionEntry(event: Event, connectionEntrySM: ConnectionEntrySM): ConnectionEntry {
        return ConnectionEntry(
            event = event,
            chatId = connectionEntrySM.chatId,
            runner = runnerUidMapping[connectionEntrySM.runnerUid]!!
        )
    }

    private fun toRunTimer(runTimerSM: RunTimerSM): RunTimer {
        return RunTimer(
            runEnabled = runTimerSM.runEnabled,
            runDryPhase = runTimerSM.runDryPhase,
            remainingTime = runTimerSM.remainingTime,
            state = runTimerSM.state
        )
    }
}