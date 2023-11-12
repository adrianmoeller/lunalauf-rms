package lunalauf.rms.modelapi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.runBlocking
import java.util.*

class RunTimer(
    private val modelAPI: ModelAPI
) {
    companion object {
        private const val RUN_DRY_TIME = 120L * 1000L
    }

    private var timer: Timer? = null
    private var runFinishedTimer: Timer? = null

    private val _remainingTime = MutableStateFlow(0L) // sec
    val remainingTime get() = _remainingTime.asStateFlow()
    private val _state = MutableStateFlow(State.EXPIRED)
    val state get() = _state.asStateFlow()

    @Synchronized
    private fun runTask() {
        val actRemTime = _remainingTime.getAndUpdate { it - 1 }
        val expired = actRemTime <= 0L

        if (expired) {
            _state.value = State.EXPIRED
            stopTimer()
            scheduleRunFinished()
        }
    }

    private fun scheduleRunFinished() {
        runBlocking(ModelAPI.modelContext) {
            modelAPI.startRunDryPhase()
        }
        runFinishedTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    runBlocking(ModelAPI.modelContext) {
                        modelAPI.disableRun()
                    }
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
        if (state.value == State.EXPIRED) {
            _state.value = State.RUNNING
            _remainingTime.value = durationInSec
            runFinishedTimer?.cancel()
            timer = Timer(true).apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runTask()
                    }
                }, 0, 1000)
            }
            runBlocking(ModelAPI.modelContext) {
                modelAPI.enableRun()
            }
        }
    }

    @Synchronized
    fun pause() {
        if (state.value == State.RUNNING) {
            _state.value = State.PAUSED
            stopTimer()
            runBlocking(ModelAPI.modelContext) {
                modelAPI.disableRun()
            }
        }
    }

    @Synchronized
    fun resume() {
        if (state.value == State.PAUSED) {
            _state.value = State.RUNNING
            timer = Timer(true).apply {
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runTask()
                    }
                }, 0, 1000)
            }
            runBlocking(ModelAPI.modelContext) {
                modelAPI.enableRun()
            }
        }
    }

    @Synchronized
    fun reset() {
        if (state.value == State.PAUSED) {
            _state.value = State.EXPIRED
            _remainingTime.value = 0
        }
    }

    enum class State {
        RUNNING, PAUSED, EXPIRED
    }
}
