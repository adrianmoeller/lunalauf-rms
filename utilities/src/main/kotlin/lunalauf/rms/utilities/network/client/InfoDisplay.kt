package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RunnerInfoResponse

class InfoDisplay(
    connection: Connection,
    onConnectionLost: () -> Unit
) : AbstractOperator(connection, onConnectionLost) {
    override fun handleResponse(response: Response) {
        when (response) {
            is ErrorResponse -> {
                mutableState.value = State.Error(response.error)
            }

            is RunnerInfoResponse -> {
                mutableState.value = Responded(response)
            }

            else -> {
                mutableState.value = State.Error(ErrorType.UNEXPECTED_SERVER_MESSAGE)
            }
        }
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRunnerInfoRequest(runnerId), connection)
    }

    class Responded(val response: RunnerInfoResponse) : State()
}
