package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.RequestSubmitter
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.util.RepetitionHandler

abstract class CounterOperationMode {
    protected val requestSubmitter = RequestSubmitter(
        responseHandler = { handleResponse(it) },
        errorHandler = { handleError(it) }
    )
    protected val requestFactory = RequestFactory()
    protected val repetitionHandler = RepetitionHandler(3000)

    protected val _state: MutableStateFlow<State> = MutableStateFlow(State.None)
    val state = _state.asStateFlow()

    protected abstract fun handleResponse(response: Response)
    protected abstract fun handleError(error: ErrorType)

    fun shutdown() {
        requestSubmitter.shutdown()
    }

    abstract class State {
        data object None : State()
        class Error(val error: ErrorType) : State()
    }
}