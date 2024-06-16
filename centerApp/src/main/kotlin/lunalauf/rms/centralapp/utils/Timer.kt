package lunalauf.rms.centralapp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration

class Timer(
    private val name: String = "Undefined",
    private val launcher: (suspend CoroutineScope.() -> Unit) -> Unit,
    private val onError: (Throwable) -> Unit = {},
    private val action: suspend CoroutineScope.() -> Unit
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private var runningTimer = AtomicReference<RunningTimer>(null)

    fun restart(
        delay: Duration = Duration.ZERO,
        period: Duration? = null
    ) {
        val newRunningTimer = RunningTimer(delay, period, launcher, onError, action)
        runningTimer.getAndSet(newRunningTimer)?.cancel()
        logger.info("Started '$name' with period=$period")
    }

    fun stop() {
        runningTimer.getAndSet(null)?.cancel()
        logger.info("Stopped '$name'")
    }

    class RunningTimer(
        private val delay: Duration = Duration.ZERO,
        private val period: Duration? = null,
        launcher: (suspend CoroutineScope.() -> Unit) -> Unit,
        onError: (Throwable) -> Unit = {},
        action: suspend CoroutineScope.() -> Unit
    ) {
        private val keepRunning = AtomicBoolean(true)
        private val caughtAction: suspend CoroutineScope.() -> Unit = {
            try {
                action()
            } catch (e: Throwable) {
                keepRunning.set(false)
                try {
                    onError(e)
                } catch (_: Throwable) {
                }
            }
        }

        init {
            launcher {
                delay(delay)
                if (period == null) {
                    if (keepRunning.get())
                        caughtAction()
                } else {
                    while (keepRunning.get()) {
                        async { caughtAction() }
                        delay(period)
                    }
                }
            }
        }

        fun cancel() {
            keepRunning.set(false)
        }
    }
}
