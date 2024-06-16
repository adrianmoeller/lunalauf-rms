package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.RequestSubmitter
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RoundCountAcceptedResponse
import lunalauf.rms.utilities.network.communication.message.response.RoundCountRejectedResponse
import lunalauf.rms.utilities.network.util.FixedQueue
import lunalauf.rms.utilities.network.util.RepetitionHandler

class RoundCounter(
    val client: Client
) {
    private val requestSubmitter: RequestSubmitter
    private val requestFactory: RequestFactory
    private val repetitionHandler: RepetitionHandler
    private var onConnectionLost: () -> Unit = {}

    val successQueue: FixedQueue<RoundCountAcceptedResponse>
    private val _state: MutableStateFlow<State> = MutableStateFlow(State.None)
    val state = _state.asStateFlow()

    init {
        requestSubmitter = RequestSubmitter(
            responseHandler = { handleResponse(it) },
            errorHandler = { handleError(it) }
        )
        requestFactory = RequestFactory()
        repetitionHandler = RepetitionHandler(3000)
        successQueue = FixedQueue(4)
    }

    private fun handleResponse(response: Response) {
        when (response) {
            is RoundCountRejectedResponse -> {
                _state.value = State.ResponseRejected(response)
            }

            is ErrorResponse -> {
                _state.value = State.Error(response.error)
            }

            is RoundCountAcceptedResponse -> {
                successQueue.push(response)
                _state.value = State.ResponseAccepted(response)
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
        if (repetitionHandler.isUnwantedRepetition(runnerId))
            return
        requestSubmitter.submit(requestFactory.createRoundCountRequest(runnerId), client)
    }

    fun setOnConnectionLost(onConnectionLost: () -> Unit) {
        this.onConnectionLost = onConnectionLost
    }

    fun shutdown() {
        requestSubmitter.shutdown()
    }

    sealed class State {
        data object None : State()
        class ResponseAccepted(val response: RoundCountAcceptedResponse) : State()
        class ResponseRejected(val response: RoundCountRejectedResponse) : State()
        class Error(val error: ErrorType) : State()
    }
}
