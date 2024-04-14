package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.client.RoundCounter.State
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.RequestSubmitter
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RunnerInfoResponse
import lunalauf.rms.utilities.network.util.RepetitionHandler

class InfoDisplay(
    val client: Client
) {
    private val requestSubmitter: RequestSubmitter
    private val requestFactory: RequestFactory
    private val repetitionHandler: RepetitionHandler
    private var onConnectionLost: () -> Unit = {}

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.None)
    val state = _state.asStateFlow()

    init {
        requestSubmitter = RequestSubmitter(
            responseHandler = { handleResponse(it) },
            errorHandler = { handleError(it) }
        )
        requestFactory = RequestFactory()
        repetitionHandler = RepetitionHandler(3000)
    }

    private fun handleResponse(response: Response) {
        when (response) {
            is ErrorResponse -> {
                _state.value = State.Error(response.error)
            }
            is RunnerInfoResponse -> {
                _state.value = State.Response(response)
            }
            else -> {
                _state.value = State.Error(ErrorType.UNEXPECTED_SERVER_MESSAGE)
            }
        }
    }

    private fun handleError(error: ErrorType) {
        _state.value = State.Error(error)
        if (error == ErrorType.DISCONNECTED) onConnectionLost()
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRunnerInfoRequest(runnerId), client)
    }

    fun setOnConnectionLost(onConnectionLost: () -> Unit) {
        this.onConnectionLost = onConnectionLost
    }

    fun shutdown() {
        requestSubmitter.shutdown()
    }

    sealed class State {
        data object None : State()
        class Response(val response: RunnerInfoResponse) : State()
        class Error(val error: ErrorType) : State()
    }
}
