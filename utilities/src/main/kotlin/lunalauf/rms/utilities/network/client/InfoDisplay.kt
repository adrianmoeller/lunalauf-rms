package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RunnerInfoResponse

class InfoDisplay(
    val connection: Connection
) : CounterOperationMode() {
    private var onConnectionLost: () -> Unit = {}

    override fun handleResponse(response: Response) {
        when (response) {
            is ErrorResponse -> {
                _state.value = State.Error(response.error)
            }
            is RunnerInfoResponse -> {
                _state.value = Responded(response)
            }
            else -> {
                _state.value = State.Error(ErrorType.UNEXPECTED_SERVER_MESSAGE)
            }
        }
    }

    override fun handleError(error: ErrorType) {
        _state.value = State.Error(error)
        if (error == ErrorType.DISCONNECTED) onConnectionLost()
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRunnerInfoRequest(runnerId), connection)
    }

    fun setOnConnectionLost(onConnectionLost: () -> Unit) {
        this.onConnectionLost = onConnectionLost
    }

    class Responded(val response: RunnerInfoResponse) : State()
}
