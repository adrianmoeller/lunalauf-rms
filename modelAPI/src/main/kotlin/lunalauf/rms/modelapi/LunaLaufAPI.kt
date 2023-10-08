package lunalauf.rms.modelapi

import LunaLaufLanguage.*
import LunaLaufLanguage.impl.LunaLaufLanguageFactoryImpl
import lunalauf.rms.modelapi.ProcessLogEntry.Lvl
import lunalauf.rms.modelapi.util.Result
import org.eclipse.emf.common.notify.Notification
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.util.EContentAdapter
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*
import java.util.function.Consumer

object LunaLaufAPI {
    const val fileExtension = "ll"
    const val DEFAULT_ROUND_POINTS = 1

    var processLogger: ProcessLogger = ProcessLogger.initLogger()
    private var resSet: ResourceSet? = null
    var resource: Resource? = null
        private set

    private var runEnabled = false
    private var runDryPhase = false
    private var runDryPhaseFinishedTeams: MutableSet<Team>? = null
    private var runDryPhaseFinishedRunners: MutableSet<Runner>? = null
    private var preSaveProcessing: Runnable? = null

    init {
        try {
            resSet = initResourceSet()
        } catch (e: IOException) {
            ProcessLogger.log(ProcessLogEntry("'API initialisation' - init ResourceSet failed", e, Lvl.ERROR))
        }
    }

    @Throws(IOException::class)
    private fun initResourceSet(): ResourceSet {
        val rs: ResourceSet = ResourceSetImpl()
        rs.packageRegistry[LunaLaufLanguagePackage.eINSTANCE.nsURI] = LunaLaufLanguagePackage.eINSTANCE
        rs.resourceFactoryRegistry.extensionToFactoryMap[fileExtension] = XMIResourceFactoryImpl()
        val conv = rs.uriConverter
        conv.uriMap[URI.createPlatformResourceURI("", true)] =
            URI.createFileURI(File("./../").getCanonicalPath() + "\\")
        rs.uriConverter = conv
        return rs
    }

    @Synchronized
    fun newFile(uri: URI, year: Int): Result<LunaLauf> {
        val re = Result<LunaLauf>("Create New File")
        if (resource != null) {
            if (!re.makeSub(save()).hasResult()) {
                return re.failed("Not possible", null)
            }
        }
        try {
            resSet!!.resources.clear()
        } catch (e: Exception) {
            re.subEntry("Failed clearing resources in the resource set", e, Lvl.WARN).setCode(3)
        }
        resource = resSet!!.createResource(uri)
        if (resource == null) {
            return re.failed("Failed creating Resource", null)
        }
        val llRe: Result<LunaLauf> = re.makeSub(createLunaLauf(year))
        if (!llRe.hasResult()) {
            return re.failed("Failed creating LunaLauf model", null)
        }
        val lunalauf = llRe.result!!
        try {
            resource!!.contents.add(lunalauf)
        } catch (e: Exception) {
            return re.failed("Failed adding LunaLauf model", e)
        }
        return re.passed(lunalauf, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun load(uri: URI): Result<LunaLauf> {
        val re = Result<LunaLauf>("Load File")
        if (resource != null) {
            if (!re.makeSub(save()).hasResult()) {
                return re.failed("Not possible", null)
            }
        }
        try {
            resSet!!.resources.clear()
        } catch (e: Exception) {
            re.subEntry("Failed clearing resources in the resource set", e, Lvl.WARN).setCode(3)
        }
        resource = resSet!!.createResource(uri)
        if (resource == null) {
            return re.failed("Failed creating resource", null)
        }
        try {
            resource!!.load(null)
        } catch (e: IOException) {
            re.failed("Failed loading resource", e)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("File is corrupted", null)
        }
        return re.passed(llRe.result, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun save(): Result<URI> {
        val re = Result<URI>("Save File")
        try {
            if (preSaveProcessing != null) preSaveProcessing!!.run()
        } catch (ignored: Exception) {
        }
        try {
            resource!!.save(null)
        } catch (e: IOException) {
            return re.failed("Failed", e)
        } catch (e: NullPointerException) {
            return re.failed("No file to save", e)
        }
        return re.passed(resource!!.uri, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun removePreSaveProcessing() {
        preSaveProcessing = null
    }

    @Synchronized
    fun setPreSaveProcessing(preSaveProcessing: Runnable?) {
        this.preSaveProcessing = preSaveProcessing
    }

    @Synchronized
    fun close(): Result<Void> {
        val res = Result<Void>("Close File")
        resource = try {
            resSet!!.resources.clear()
            null
        } catch (e: Exception) {
            return res.failed("Failed", e)
        }
        return res.passed(null, 0, "Done", Lvl.INFO)
    }

    @get:Synchronized
    val isFileOpen: Boolean
        get() = getLunaLauf().hasResult()

    private fun createLunaLauf(year: Int): Result<LunaLauf> {
        val re = Result<LunaLauf>("Create LunaLauf Instance")
        val lunalauf: LunaLauf = LunaLaufLanguageFactoryImpl.eINSTANCE.createLunaLauf()
        lunalauf.year = year
        return re.passed(lunalauf, 1, "Done: $lunalauf", Lvl.INFO)
    }

    @Synchronized
    fun enableRun() {
        runEnabled = true
        runDryPhase = false
    }

    @Synchronized
    fun disableRun() {
        runEnabled = false
        runDryPhase = false
    }

    @Synchronized
    fun startRunDryPhase() {
        runEnabled = true
        runDryPhase = true
        runDryPhaseFinishedTeams = HashSet()
        runDryPhaseFinishedRunners = HashSet()
    }

    @Synchronized
    fun getLunaLauf(): Result<LunaLauf> {
        val re = Result<LunaLauf>("Get LunaLauf Instance")
        val lunalauf: LunaLauf = try {
            resource!!.contents[0] as LunaLauf
        } catch (index: IndexOutOfBoundsException) {
            return re.failed("Failed (no contents found in the actual resource)", index)
        } catch (nullp: NullPointerException) {
            return re.failed("Failed (resource does not exist)", nullp)
        } catch (e: Exception) {
            return re.failed("Failed", e)
        }
        return re.passed(lunalauf, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun addNewRunner(id: Long, name: String?): Result<Runner> {
        val re = Result<Runner>("Add New Runner")
        if (id < 0) {
            return re.failed("ID must be a positive number", null)
        }
        val rRe: Result<Runner> = re.makeSub(getRunner(id))
        if (rRe.hasResult()) {
            return re.passed(rRe.result, 2, "A Runner with this ID already exists: " + rRe.result.toString(), Lvl.WARN)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        val newRunner = LunaLaufLanguageFactoryImpl.eINSTANCE.createRunner()
        newRunner.id = id
        if (name != null && name != "") newRunner.name = name
        lunalauf!!.runners.add(newRunner)
        return re.passed(newRunner, 1, "Done, $newRunner", Lvl.INFO)
    }

    @Synchronized
    fun getRunner(id: Long): Result<Runner> {
        val re = Result<Runner>("Get Runner")
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        for (runner in lunalauf!!.runners) {
            if (runner.id == id) {
                return re.passed(runner, 1, "Done, $runner", Lvl.INFO)
            }
        }
        return re.passed(null, 0, "No existing Runner with ID: $id", Lvl.INFO)
    }

    @Synchronized
    fun changeRunnerID(runner: Runner, newId: Long): Result<Runner> {
        val re = Result<Runner>("Change Runner ID")
        val rRe: Result<Runner> = re.makeSub(getRunner(newId))
        if (rRe.hasResult()) {
            return re.passed(runner, 2, "This ID already exists: " + rRe.result.toString(), Lvl.WARN)
        }
        runner.id = newId
        return re.passed(runner, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun addNewTeam(name: String?): Result<Team> {
        val re = Result<Team>("Add New Team")
        if (name == null || name == "") {
            return re.failed("Name is 'null' or empty", null)
        }
        val tRe: Result<Team> = re.makeSub(getTeam(name))
        if (tRe.hasResult()) {
            return re.passed(tRe.result, 2, "A Team with this name already exists: " + tRe.result.toString(), Lvl.WARN)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        val newTeam = LunaLaufLanguageFactoryImpl.eINSTANCE.createTeam()
        newTeam.name = name
        lunalauf!!.teams.add(newTeam)
        return re.passed(newTeam, 1, "Done, $newTeam", Lvl.INFO)
    }

    @Synchronized
    fun getTeam(name: String): Result<Team> {
        val re = Result<Team>("Get Team")
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        for (team in lunalauf!!.teams) {
            if (team.name == name) {
                return re.passed(team, 1, "Done, $team", Lvl.INFO)
            }
        }
        return re.passed(null, 0, "No existing Team with name: $name", Lvl.INFO)
    }

    @Synchronized
    fun addRunnerToTeam(team: Team?, runner: Runner?): Result<Runner> {
        val re = Result<Runner>("Add Runner To Team")
        if (team == null || runner == null) {
            return re.failed("At least one parameter is 'null'", null)
        }
        if (runner.team != null) {
            return if (team.members.contains(runner)) {
                re.passed(runner, 2, "Runner is already member of this Team", Lvl.WARN)
            } else {
                re.failed("Runner is still member of another Team: " + runner.team.name, null)
            }
        }
        try {
            runner.team = team
        } catch (e: Exception) {
            return re.failed("Failed adding Runner to Team", e)
        }
        return re.passed(runner, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun removeRunnerFromTeam(runner: Runner?): Result<Team> {
        val re = Result<Team>("Remove Runner from Team")
        if (runner == null) {
            return re.failed("Parameter is 'null'", null)
        }
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

    @Synchronized
    fun addNewMinigame(name: String?, id: Int): Result<Minigame> {
        val re = Result<Minigame>("Add New Minigame")
        if (name.isNullOrEmpty()) {
            return re.failed("Name is not set", null)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        val minigame = LunaLaufLanguageFactory.eINSTANCE.createMinigame()
        minigame.name = name
        minigame.minigameID = id
        lunalauf!!.minigames.add(minigame)
        return re.passed(minigame, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun addNewChallenge(name: String?, description: String?): Result<Challenge> {
        val re = Result<Challenge>("Add New Challenge")
        if (name.isNullOrEmpty()) {
            return re.failed("Name is not set", null)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        val challenge = LunaLaufLanguageFactory.eINSTANCE.createChallenge()
        challenge.name = name
        challenge.description = description
        lunalauf!!.challenges.add(challenge)
        return re.passed(challenge, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun addNewChallenge(
        name: String?, description: String?, duration: Int, expireMsg: String?, receiveImage: Boolean
    ): Result<Challenge> {
        val re = addNewChallenge(name, description)
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

    @get:Synchronized
    val funfactors: Result<List<Funfactor>>
        get() {
            val re = Result<List<Funfactor>>("Get Funfactors")
            val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
            if (!llRe.hasResult()) {
                return re.failed("Failed accessing LunaLauf model", null)
            }
            val lunalauf = llRe.result
            val funfactors: MutableList<Funfactor> = LinkedList()
            funfactors.addAll(lunalauf!!.challenges)
            funfactors.addAll(lunalauf.minigames)
            return re.passed(funfactors, 1, "Done", Lvl.INFO)
        }

    @Synchronized
    fun logRound(runner: Runner?, points: Int, manualLogged: Boolean, roundThreshold: Int): Result<Round> {
        val re = Result<Round>("Log Round")
        if (runner == null) {
            return re.failed("Parameter runner is 'null'", null)
        }
        if (points < 0) {
            return re.failed("Points must be positive", null)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        val currentTime = Timestamp.valueOf(LocalDateTime.now())
        if (!manualLogged) {
            if (!runEnabled) {
                return re.passed(null, 4, "Run is disabled", Lvl.WARN)
            }
            if (runDryPhase) {
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
        lunalauf!!.log.add(newLogEntry)
        lunalauf.rounds.add(newLogEntry)
        return re.passed(newLogEntry, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun logFunfactorResult(team: Team?, type: Funfactor?, points: Int): Result<FunfactorResult> {
        val re = Result<FunfactorResult>("Log Funfactor Result")
        if (team == null || type == null) {
            return re.failed("At least one parameter is 'null'", null)
        }
        if (points < 0) {
            return re.failed("Points must be positive", null)
        }
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        val newLogEntry = LunaLaufLanguageFactoryImpl.eINSTANCE.createFunfactorResult()
        newLogEntry.timestamp = Timestamp.valueOf(LocalDateTime.now())
        newLogEntry.points = points
        newLogEntry.type = type
        newLogEntry.team = team
        lunalauf!!.log.add(newLogEntry)
        lunalauf.funfactorResults.add(newLogEntry)
        return re.passed(newLogEntry, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun logMinigameResult(team: Team?, minigameID: Int, points: Int): Result<FunfactorResult> {
        val re = Result<FunfactorResult>("Log Minigame Result")
        val llRe: Result<LunaLauf> = re.makeSub(getLunaLauf())
        if (!llRe.hasResult()) {
            return re.failed("Failed accessing LunaLauf model", null)
        }
        val lunalauf = llRe.result
        var minigame: Minigame? = null
        for (mg in lunalauf!!.minigames) {
            if (mg.minigameID == minigameID) minigame = mg
        }
        if (minigame == null) {
            re.failed("There is no Minigame with this ID", null)
        }
        val logEntryRe: Result<FunfactorResult> = re.makeSub(logFunfactorResult(team, minigame, points))
        return if (!logEntryRe.hasResult()) {
            re.failed("Failed", null)
        } else re.passed(logEntryRe.result, 1, "Done", Lvl.INFO)
    }

    @Synchronized
    fun <T : EObject?> deleteElement(element: T?): Result<T> {
        val re = Result<T>("Delete Element")
        if (element == null) {
            return re.failed("Parameter is 'null'", null)
        }
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
