package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.RequestSubmitter
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.util.RepetitionHandler

abstract class AbstractOperator(
    val connection: Connection,
    private val onConnectionLost: () -> Unit
) {
    protected val requestSubmitter = RequestSubmitter(
        responseHandler = { handleResponse(it) },
        errorHandler = {
            mutableState.value = State.Error(it)
            if (it == ErrorType.DISCONNECTED) onConnectionLost()
        }
    )
    protected val requestFactory = RequestFactory()
    protected val repetitionHandler = RepetitionHandler(3000)

    protected val mutableState: MutableStateFlow<State> = MutableStateFlow(State.None)
    val state = mutableState.asStateFlow()

    protected abstract fun handleResponse(response: Response)

    fun shutdown() {
        requestSubmitter.shutdown()
    }

    abstract class State {
        data object None : State()
        class Error(val error: ErrorType) : State()
    }
}