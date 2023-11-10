package lunalauf.rms.utilities.network.client

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
    private val client: Client
) {
    private val requestSubmitter: RequestSubmitter
    private val requestFactory: RequestFactory
    private val repetitionHandler: RepetitionHandler
    val successQueue: FixedQueue<RoundCountAcceptedResponse>
    private var acceptedAction: (RoundCountAcceptedResponse) -> Unit = {}
    private var rejectedAction: (RoundCountRejectedResponse) -> Unit = {}
    private var failedAction: (ErrorType) -> Unit = {}
    private var onConnectionLost: () -> Unit = {}

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
            is RoundCountRejectedResponse -> rejectedAction(response)
            is ErrorResponse -> failedAction(response.error)
            is RoundCountAcceptedResponse -> {
                successQueue.push(response)
                acceptedAction(response)
            }

            else -> failedAction(ErrorType.UNEXPECTED_SERVER_MESSAGE)
        }
    }

    private fun handleError(error: ErrorType) {
        failedAction(error)
        if (error == ErrorType.DISCONNECTED) onConnectionLost()
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId))
            return
        requestSubmitter.submit(requestFactory.createRoundCountRequest(runnerId), client)
    }

    fun setActions(
        accepted: (RoundCountAcceptedResponse) -> Unit,
        rejected: (RoundCountRejectedResponse) -> Unit,
        failed: (ErrorType) -> Unit
    ) {
        acceptedAction = accepted
        rejectedAction = rejected
        failedAction = failed
    }

    fun setOnConnectionLost(onConnectionLost: () -> Unit) {
        this.onConnectionLost = onConnectionLost
    }

    fun shutdown() {
        requestSubmitter.shutdown()
    }
}
