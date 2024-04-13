package lunalauf.rms.utilities.network.client

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
    private var succeededAction: (RunnerInfoResponse) -> Unit = {}
    private var failedAction: (ErrorType) -> Unit = {}
    private var onConnectionLost: () -> Unit = {}

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
            is ErrorResponse -> failedAction(response.error)
            is RunnerInfoResponse -> succeededAction(response)
            else -> failedAction(ErrorType.UNEXPECTED_SERVER_MESSAGE)
        }
    }

    private fun handleError(error: ErrorType) {
        failedAction(error)
        if (error == ErrorType.DISCONNECTED) onConnectionLost()
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRunnerInfoRequest(runnerId), client)
    }

    fun setActions(
        succeeded: (RunnerInfoResponse) -> Unit,
        failed: (ErrorType) -> Unit
    ) {
        succeededAction = succeeded
        failedAction = failed
    }

    fun setOnConnectionLost(onConnectionLost: () -> Unit) {
        this.onConnectionLost = onConnectionLost
    }

    fun shutdown() {
        requestSubmitter.shutdown()
    }
}
