package lunalauf.rms.utilities.network.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

abstract class Service<S, T>(
    context: CoroutineContext = Dispatchers.Default
) {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(context)
    private var job: Job? = null

    private val _state = MutableStateFlow(State.Idling)
    val state get() = _state.asStateFlow()

    abstract suspend fun CoroutineScope.run(input: S): T

    @Synchronized
    fun start(
        input: S,
        onResultAvailable: (T) -> Unit = {}
    ) {
        _state.value = State.Transitioning
        scope.launch {
            job?.cancelAndJoin()

            job = scope.launch {
                _state.value = State.Running
                try {
                    val result = run(input)
                    onResultAvailable(result)
                } catch (e: Throwable) {
                    logger.error("Exception occurred during service run", e)
                }
                _state.value = State.Idling
            }
        }
    }

    @Synchronized
    fun stop() {
        _state.value = State.Transitioning
        scope.launch {
            job?.cancelAndJoin()
            _state.value = State.Idling
        }
    }

    enum class State {
        Running, Idling, Transitioning
    }
}