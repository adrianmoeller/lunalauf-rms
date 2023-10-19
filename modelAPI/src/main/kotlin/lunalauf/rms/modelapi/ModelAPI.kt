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

    suspend fun createRunner(id: ULong, name: String): CreateRunnerResult {
        mutex.withLock {
            getRunner(id)?.let {
                logger.warn("Missing UI check if ID already exists when creating a runner")
                return CreateRunnerResult.Exists(it)
            }

            val newRunner = LunaLaufLanguageFactoryImpl.eINSTANCE.createRunner()
            newRunner.id = id.toLong()
            newRunner.name = name
            model.runners.add(newRunner)
            logger.info("Created {}", newRunner)
            return CreateRunnerResult.Created(newRunner)
        }
    }

    private fun getRunner(id: ULong): Runner? {
        for (runner in model.runners) {
            if (runner.id == id.toLong())
                return runner
        }
        return null
    }

    suspend fun changeRunnerId(runner: Runner, newId: ULong): Result<Runner> {
        mutex.withLock {
            val re = Result<Runner>("Change Runner ID")
            val existingRunner = getRunner(newId)
            if (existingRunner != null) {
                return re.passed(runner, 2, "This ID already exists: $existingRunner", Lvl.WARN)
            }
            runner.id = newId.toLong()
            return re.passed(runner, 1, "Done", Lvl.INFO)
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
                AddRunnerToTeamResult.AlreadyMember
            }
            runner.team = team
            logger.info("Added {} to {}", runner, team)
            return AddRunnerToTeamResult.Added
        }
    }

    suspend fun removeRunnerFromTeam(runner: Runner): Result<Team> {
        mutex.withLock {
            val re = Result<Team>("Remove Runner from Team")
            if (runner.team == null) {
                return re.passed(null, 0, "Runner is already in no Team", Lvl.WARN)
            }
            val oldRunnersTeam = runner.team
            try {
                runner.team = null
            } catch (e: Exception) {
                return re.failed("Failed removing Rummer From Team", e)
            }
            return re.passed(oldRunnersTeam, 1, "Done", Lvl.INFO)
        }
    }

    suspend fun addNewMinigame(name: String, id: Int): Result<Minigame> {
        mutex.withLock {
            val re = Result<Minigame>("Add New Minigame")
            if (name.isBlank()) {
                return re.failed("Name is empty", null)
            }
            val minigame = LunaLaufLanguageFactory.eINSTANCE.createMinigame()
            minigame.name = name
            minigame.minigameID = id
            model.minigames.add(minigame)
            return re.passed(minigame, 1, "Done", Lvl.INFO)
        }
    }

    private fun internalAddNewChallenge(name: String, description: String): Result<Challenge> {
        val re = Result<Challenge>("Add New Challenge")
        if (name.isBlank()) {
            return re.failed("Name is empty", null)
        }
        val challenge = LunaLaufLanguageFactory.eINSTANCE.createChallenge()
        challenge.name = name
        challenge.description = description
        model.challenges.add(challenge)
        return re.passed(challenge, 1, "Done", Lvl.INFO)
    }

    suspend fun addNewChallenge(name: String, description: String): Result<Challenge> {
        mutex.withLock {
            return internalAddNewChallenge(name, description)
        }
    }

    suspend fun addNewChallenge(
        name: String, description: String, duration: Int, expireMsg: String, receiveImage: Boolean
    ): Result<Challenge> {
        mutex.withLock {
            val re = internalAddNewChallenge(name, description)
            if (!re.hasResult()) {
                return re
            }
            val challenge = re.result
            challenge!!.isExpires = true
            challenge.duration = duration
            challenge.expireMsg = expireMsg
            challenge.isReceiveImages = receiveImage
            return re
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

    suspend fun <T : EObject?> deleteElement(element: T): Result<T> {
        mutex.withLock {
            val re = Result<T>("Delete Element")
            if (element is Team) {
                if (!element.funfactorResults.isEmpty()) return re.passed(
                    element,
                    2,
                    "Not deleted (Team has relevant content)",
                    Lvl.WARN
                )
            }
            if (element is Runner) {
                if (!element.rounds.isEmpty()) return re.passed(
                    element,
                    2,
                    "Not deleted (Runner has relevant content)",
                    Lvl.WARN
                )
            }
            if (element is Funfactor) {
                if (!element.funfactorResults.isEmpty()) return re.passed(
                    element,
                    2,
                    "Not deleted (Funfactor has relevant content)",
                    Lvl.WARN
                )
            }
            EcoreUtil.delete(element, true)
            return re.passed(element, 1, "Done", Lvl.INFO)
        }
    }
}
