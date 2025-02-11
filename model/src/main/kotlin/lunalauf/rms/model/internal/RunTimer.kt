package lunalauf.rms.model.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import lunalauf.rms.model.common.RunTimerState
import org.slf4j.LoggerFactory
import java.util.*

class RunTimer internal constructor(
    runEnabled: Boolean,
    runDryPhase: Boolean,
    remainingTime: Long,
    state: RunTimerState
) {
    companion object {
        private const val RUN_DRY_TIME = 120L * 1000L
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private var timer: Timer? = null
    private var runFinishedTimer: Timer? = null
    private var runDryPhaseFinishedTeams: MutableSet<Team> = HashSet()
    private var runDryPhaseFinishedRunners: MutableSet<Runner> = HashSet()

    private val _runEnabled = MutableStateFlow(runEnabled)
    val runEnabled get() = _runEnabled.asStateFlow()

    private val _runDryPhase = MutableStateFlow(runDryPhase)
    val runDryPhase get() = _runDryPhase.asStateFlow()

    private val _remainingTime = MutableStateFlow(remainingTime) // sec
    val remainingTime get() = _remainingTime.asStateFlow()

    private val _state = MutableStateFlow(if (state == RunTimerState.RUNNING) RunTimerState.PAUSED else state)
    val state get() = _state.asStateFlow()

    constructor() : this(
        runEnabled = false,
        runDryPhase = false,
        remainingTime = 0,
        state = RunTimerState.EXPIRED
    )

    init {
        if (runDryPhase) {
            runFinishedTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        _runEnabled.value = false
                        _runDryPhase.value = false
                    }
                }, RUN_DRY_TIME)
            }
        }
    }

    @Synchronized
    private fun runTask() {
        val actRemTime = _remainingTime.getAndUpdate { it - 1 }
        val expired = actRemTime <= 0L

        if (expired) {
            _state.value = RunTimerState.EXPIRED
            stopTimer()
            scheduleRunFinished()
        }
    }

    private fun scheduleRunFinished() {
        _runEnabled.value = true
        _runDryPhase.value = true
        runDryPhaseFinishedTeams = HashSet()
        runDryPhaseFinishedRunners = HashSet()

        runFinishedTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    _runEnabled.value = false
                    _runDryPhase.value = false
                }
            }, RUN_DRY_TIME)
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    @Synchronized
    fun start(durationInSec: Long) {
        if (state.value == RunTimerState.EXPIRED) {
            _state.value = RunTimerState.RUNNING
            _remainingTime.value = durationInSec
            runFinishedTimer?.cancel()

            timer = Timer(true).apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runTask()
                    }
                }, 0, 1000)
            }

            _runEnabled.value = true
            _runDryPhase.value = false
        }
    }

    @Synchronized
    fun pause() {
        if (state.value == RunTimerState.RUNNING) {
            _state.value = RunTimerState.PAUSED

            stopTimer()

            _runEnabled.value = false
            _runDryPhase.value = false
        }
    }

    @Synchronized
    fun resume() {
        if (state.value == RunTimerState.PAUSED) {
            _state.value = RunTimerState.RUNNING

            timer = Timer(true).apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runTask()
                    }
                }, 0, 1000)
            }

            _runEnabled.value = true
            _runDryPhase.value = false
        }
    }

    @Synchronized
    fun reset() {
        if (state.value == RunTimerState.PAUSED) {
            _state.value = RunTimerState.EXPIRED
            _remainingTime.value = 0
        }
    }

    @Synchronized
    fun validateFinishedRunDryPhase(runner: Runner): Boolean {
        if (_runDryPhase.value) {
            val team = runner.team.value

            if (team != null) {
                if (team in runDryPhaseFinishedTeams) {
                    logFinishedRunDryPhase(runner)
                    return true
                }
                runDryPhaseFinishedTeams.add(team)
            } else {
                if (runner in runDryPhaseFinishedRunners) {
                    logFinishedRunDryPhase(runner)
                    return true
                }
                runDryPhaseFinishedRunners.add(runner)
            }
        }

        return false
    }

    private fun logFinishedRunDryPhase(runner: Runner) {
        logger.info("Tried to log round but last round is already logged: {}", runner)
    }
}
