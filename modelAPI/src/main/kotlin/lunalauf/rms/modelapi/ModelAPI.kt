package lunalauf.rms.modelapi

import LunaLaufLanguage.*
import LunaLaufLanguage.impl.LunaLaufLanguageFactoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import lunalauf.rms.modelapi.util.ProcessLogEntry.Lvl
import lunalauf.rms.modelapi.util.Result
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.LocalDateTime

class ModelAPI(
    private val mutex: Mutex,
    private val modelState: ModelState.Loaded
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val model = modelState.model

    val DEFAULT_ROUND_POINTS = 1

    var roundThreshold = 40
        private set

    private val _runEnabled = MutableStateFlow(false)
    val runEnabled get() = _runEnabled.asStateFlow()
    private val _runDryPhase = MutableStateFlow(false)
    val runDryPhase get() = _runDryPhase.asStateFlow()

    private var runDryPhaseFinishedTeams: MutableSet<Team>? = null
    private var runDryPhaseFinishedRunners: MutableSet<Runner>? = null

    suspend fun setRoundThreshold(threshold: Int) {
        mutex.withLock { roundThreshold = threshold }
        logger.info("Set round threshold to ${roundThreshold}s")
    }

    suspend fun enableRun() {
        mutex.withLock {
            _runEnabled.value = true
            _runDryPhase.value = false
        }
    }

    suspend fun disableRun() {
        mutex.withLock {
            _runEnabled.value = false
            _runDryPhase.value = false
        }
    }

    suspend fun startRunDryPhase() {
        mutex.withLock {
            _runEnabled.value = true
            _runDryPhase.value = true
            runDryPhaseFinishedTeams = HashSet()
            runDryPhaseFinishedRunners = HashSet()
        }
    }

    suspend fun setSponsoringPoolAmount(amount: Double) {
        mutex.withLock {
            model.sponsorPoolAmount = amount
        }
    }

    suspend fun setSponsoringPoolRounds(rounds: Int) {
        mutex.withLock {
            model.sponsorPoolRounds = rounds
        }
    }

    suspend fun updateAdditionalContribution(updateFunction: (Double) -> Double) {
        mutex.withLock {
            model.additionalContribution = updateFunction(model.additionalContribution)
        }
    }

    suspend fun createRunner(id: Long, name: String): CreateRunnerResult {
        mutex.withLock {
            getRunner(id)?.let {
                logger.warn("Missing UI check if ID already exists when creating a runner")
                return CreateRunnerResult.Exists(it)
            }

            val newRunner = LunaLaufLanguageFactoryImpl.eINSTANCE.createRunner()
            newRunner.id = id
            newRunner.name = name
            model.runners.add(newRunner)
            logger.info("Created {}", newRunner)
            return CreateRunnerResult.Created(newRunner)
        }
    }

    private fun getRunner(id: Long): Runner? {
        for (runner in model.runners) {
            if (runner.id == id)
                return runner
        }
        return null
    }

    suspend fun updateRunnerId(runner: Runner, newId: Long): UpdateRunnerIdResult {
        mutex.withLock {
            getRunner(newId)?.let {
                logger.warn("Missing UI check if ID already exists when updating a runner ID")
                return UpdateRunnerIdResult.Exists(it)
            }
            runner.id = newId
            return UpdateRunnerIdResult.Updated
        }
    }

    suspend fun updateRunnerName(runner: Runner, newName: String) {
        mutex.withLock {
            runner.name = newName
        }
    }

    suspend fun createTeam(name: String): CreateTeamResult {
        mutex.withLock {
            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when creating a team")
                return CreateTeamResult.BlankName
            }
            getTeam(name)?.let {
                logger.warn("Missing UI check if name already exists when creating a team")
                return CreateTeamResult.Exists(it)
            }
            val newTeam = LunaLaufLanguageFactoryImpl.eINSTANCE.createTeam()
            newTeam.name = name
            model.teams.add(newTeam)
            logger.info("Created {}", newTeam)
            return CreateTeamResult.Created(newTeam)
        }
    }

    private fun getTeam(name: String): Team? {
        for (team in model.teams) {
            if (team.name == name)
                return team
        }
        return null
    }

    suspend fun addRunnerToTeam(team: Team, runner: Runner): AddRunnerToTeamResult {
        mutex.withLock {
            if (runner.team == team) {
                logger.warn("Missing UI check if runner is already a team member")
                return AddRunnerToTeamResult.AlreadyMember
            }
            runner.team = team
            logger.info("Added {} to {}", runner, team)
            return AddRunnerToTeamResult.Added
        }
    }

    suspend fun removeRunnerFromTeam(runner: Runner): RemoveRunnerFromTeamResult {
        mutex.withLock {
            if (runner.team == null) {
                logger.warn("Missing UI check if runner is already in no team")
                return RemoveRunnerFromTeamResult.AlreadyInNoTeam
            }
            val oldTeam = runner.team
            runner.team = null
            logger.info("Removed {} from {}", runner, oldTeam)
            return RemoveRunnerFromTeamResult.Removed
        }
    }

    suspend fun updateTeamName(team: Team, name: String) {
        mutex.withLock {
            team.name = name
        }
    }

    suspend fun updateContribution(
        contributor: Contributor,
        type: ContrType,
        amountFix: Double,
        amountPerRound: Double
    ) {
        mutex.withLock {
            contributor.contribution = type
            contributor.amountFix = amountFix
            contributor.amountPerRound = amountPerRound
        }
    }

    private fun getMinigame(id: Int): Minigame? {
        model.minigames.forEach {
            if (it.minigameID == id)
                return it
        }
        return null
    }

    suspend fun createMinigame(name: String, id: Int): CreateMinigameResult {
        mutex.withLock {
            getMinigame(id)?.let {
                logger.warn("Missing UI check if minigame ID already exists when creating a minigame")
                return CreateMinigameResult.Exists(it)
            }
            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when creating a minigame")
                return CreateMinigameResult.BlankName
            }

            val minigame = LunaLaufLanguageFactory.eINSTANCE.createMinigame()
            minigame.name = name
            minigame.minigameID = id
            model.minigames.add(minigame)
            logger.info("Created {}", minigame)
            return CreateMinigameResult.Created(minigame)
        }
    }

    suspend fun updateMinigameId(minigame: Minigame, id: Int): UpdateMinigameIdResult {
        mutex.withLock {
            getMinigame(id)?.let {
                logger.warn("Missing UI check if minigame ID already exists when updating a minigame ID")
                return UpdateMinigameIdResult.Exists(it)
            }

            minigame.minigameID = id
            return UpdateMinigameIdResult.Updated
        }
    }

    suspend fun updateMinigameName(minigame: Minigame, name: String): UpdateMinigameNameResult {
        mutex.withLock {
            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when updating a minigame name")
                return UpdateMinigameNameResult.BlankName
            }

            minigame.name = name
            return UpdateMinigameNameResult.Updated
        }
    }

    private fun internalCreateChallenge(name: String, description: String): CreateChallengeResult {
        if (name.isBlank()) {
            logger.warn("Missing UI check if name is not blank when creating a challenge")
            return CreateChallengeResult.BlankName
        }

        val challenge = LunaLaufLanguageFactory.eINSTANCE.createChallenge()
        challenge.name = name
        challenge.description = description
        model.challenges.add(challenge)
        logger.info("Created {}", challenge)
        return CreateChallengeResult.Created(challenge)
    }

    suspend fun createChallenge(name: String, description: String): CreateChallengeResult {
        mutex.withLock {
            return internalCreateChallenge(name, description)
        }
    }

    suspend fun createChallenge(
        name: String, description: String, duration: Int, expireMsg: String, receiveImage: Boolean
    ): CreateChallengeResult {
        mutex.withLock {
            if (duration < 0) {
                logger.warn("Missing UI check if duration is positive when creating a challenge")
                return CreateChallengeResult.NegativeDuration
            }

            val result = internalCreateChallenge(name, description)
            if (result !is CreateChallengeResult.Created)
                return result

            val challenge = result.challenge
            challenge.isExpires = true
            challenge.duration = duration
            challenge.expireMsg = expireMsg
            challenge.isReceiveImages = receiveImage
            return result
        }
    }

    suspend fun updateChallengeName(challenge: Challenge, name: String): UpdateChallengeNameResult {
        mutex.withLock {
            if (name.isBlank()) {
                logger.warn("Missing UI check if name is not blank when updating a challenge name")
                return UpdateChallengeNameResult.BlankName
            }
            challenge.name = name
            return UpdateChallengeNameResult.Updated
        }
    }

    suspend fun updateChallengeDescription(challenge: Challenge, description: String) {
        mutex.withLock {
            challenge.description = description
        }
    }

    suspend fun updateChallengeExpires(challenge: Challenge, expires: Boolean) {
        mutex.withLock {
            challenge.isExpires = expires
        }
    }

    suspend fun updateChallengeDuration(challenge: Challenge, duration: Int): UpdateChallengeDurationResult {
        mutex.withLock {
            if (duration < 0) {
                logger.warn("Missing UI check if duration is positive when updating a challenge")
                return UpdateChallengeDurationResult.NegativeDuration
            }

            challenge.duration = duration
            return UpdateChallengeDurationResult.Updated
        }
    }

    suspend fun updateChallengeExpireMessage(challenge: Challenge, expireMessage: String) {
        mutex.withLock {
            challenge.expireMsg = expireMessage
        }
    }

    suspend fun updateChallengeReceiveImage(challenge: Challenge, receiveImage: Boolean) {
        mutex.withLock {
            challenge.isReceiveImages = receiveImage
        }
    }

    suspend fun logRound(runner: Runner, points: Int, manualLogged: Boolean): Result<Round> {
        val re = Result<Round>("Log Round")
        if (points < 0) {
            return re.failed("Points must be positive", null)
        }
        val currentTime = Timestamp.valueOf(LocalDateTime.now())

        mutex.withLock {
            if (!manualLogged) {
                if (!_runEnabled.value) {
                    return re.passed(null, 4, "Run is disabled", Lvl.WARN)
                }
                if (_runDryPhase.value) {
                    val team = runner.team
                    if (team == null) {
                        if (runDryPhaseFinishedRunners!!.contains(runner)) return re.passed(
                            null,
                            5,
                            "Last round already logged",
                            Lvl.WARN
                        )
                        runDryPhaseFinishedRunners!!.add(runner)
                    } else {
                        if (runDryPhaseFinishedTeams!!.contains(team)) return re.passed(
                            null,
                            5,
                            "Last round already logged",
                            Lvl.WARN
                        )
                        runDryPhaseFinishedTeams!!.add(team)
                    }
                }
                if (!RoundCountValidator.validate(runner, currentTime, roundThreshold)) {
                    return re.passed(null, 0, "Count interval is too short", Lvl.WARN)
                }
            }
            val team = runner.team
            val newLogEntry = LunaLaufLanguageFactoryImpl.eINSTANCE.createRound()
            newLogEntry.timestamp = currentTime
            newLogEntry.points = points
            newLogEntry.runner = runner
            if (team != null) newLogEntry.team = team
            newLogEntry.isManualLogged = manualLogged
            model.log.add(newLogEntry)
            model.rounds.add(newLogEntry)
            return re.passed(newLogEntry, 1, "Done", Lvl.INFO)
        }
    }

    private fun internalLogFunfactorResult(team: Team, type: Funfactor, points: Int): Result<FunfactorResult> {
        val re = Result<FunfactorResult>("Log Funfactor Result")
        if (points < 0) {
            return re.failed("Points must be positive", null)
        }
        val newLogEntry = LunaLaufLanguageFactoryImpl.eINSTANCE.createFunfactorResult()
        newLogEntry.timestamp = Timestamp.valueOf(LocalDateTime.now())
        newLogEntry.points = points
        newLogEntry.type = type
        newLogEntry.team = team
        model.log.add(newLogEntry)
        model.funfactorResults.add(newLogEntry)
        return re.passed(newLogEntry, 1, "Done", Lvl.INFO)
    }

    suspend fun logFunfactorResult(team: Team, type: Funfactor, points: Int): Result<FunfactorResult> {
        mutex.withLock {
            return internalLogFunfactorResult(team, type, points)
        }
    }

    suspend fun logMinigameResult(team: Team, minigameID: Int, points: Int): Result<FunfactorResult> {
        mutex.withLock {
            val re = Result<FunfactorResult>("Log Minigame Result")
            var minigame: Minigame? = null
            for (mg in model.minigames) {
                if (mg.minigameID == minigameID) minigame = mg
            }
            if (minigame == null) {
                return re.failed("There is no Minigame with this ID", null)
            }
            val logEntryRe: Result<FunfactorResult> = re.makeSub(internalLogFunfactorResult(team, minigame, points))
            return if (!logEntryRe.hasResult()) re.failed("Failed", null)
            else re.passed(logEntryRe.result, 1, "Done", Lvl.INFO)
        }
    }

    suspend fun <T : EObject> deleteElement(element: T): DeleteElementResult {
        mutex.withLock {
            when (element) {
                is Team -> {
                    if (element.funfactorResults.isNotEmpty())
                        return DeleteElementResult.NotDeleted("Team has Funfactor results")
                }

                is Runner -> {
                    if (element.rounds.isNotEmpty())
                        return DeleteElementResult.NotDeleted("Runner has counted rounds")
                }

                is Funfactor -> {
                    if (element.funfactorResults.isNotEmpty())
                        return DeleteElementResult.NotDeleted("Funfactor has results")
                }
            }

            EcoreUtil.delete(element, true)
            return DeleteElementResult.Deleted
        }
    }
}
