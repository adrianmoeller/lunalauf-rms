package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.RequestSubmitter
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RoundCountAcceptedResponse
import lunalauf.rms.utilities.network.communication.message.response.RoundCountRejectedResponse
import lunalauf.rms.utilities.network.util.FixedObservableQueue
import lunalauf.rms.utilities.network.util.RepetitionHandler
import java.util.function.Consumer

class RoundCounter(
    private val client: Client
) {
    private val requestSubmitter: RequestSubmitter
    private val requestFactory: RequestFactory
    private val repetitionHandler: RepetitionHandler
    val successQueue: FixedObservableQueue<RoundCountAcceptedResponse>
    private var acceptedAction: Consumer<RoundCountAcceptedResponse>? = null
    private var rejectedAction: Consumer<RoundCountRejectedResponse>? = null
    private var failedAction: Consumer<ErrorType>? = null
    private var onConnectionLost: Runnable? = null

    init {
        requestSubmitter = RequestSubmitter(
            responseHandler = { handleResponse(it) },
            errorHandler = { handleError(it) }
        )
        requestFactory = RequestFactory()
        repetitionHandler = RepetitionHandler(3000)
        successQueue = FixedObservableQueue(4)
    }

    private fun handleResponse(response: Response) {
        if (response is RoundCountAcceptedResponse) {
            successQueue.push(response)
            if (acceptedAction != null) acceptedAction!!.accept(response)
        } else if (response is RoundCountRejectedResponse) {
            if (rejectedAction != null) rejectedAction!!.accept(response)
        } else if (response is ErrorResponse) {
            if (failedAction != null) failedAction!!.accept(response.error)
        } else {
            if (failedAction != null) failedAction!!.accept(ErrorType.UNEXPECTED_SERVER_MESSAGE)
        }
    }

    private fun handleError(error: ErrorType) {
        if (failedAction != null) failedAction!!.accept(error)
        if (error == ErrorType.DISCONNECTED && onConnectionLost != null) onConnectionLost!!.run()
    }

    fun processInput(runnerId: Long) {
        if (repetitionHandler.isUnwantedRepetition(runnerId)) return
        requestSubmitter.submit(requestFactory.createRoundCountRequest(runnerId), client)
    }

    fun setActions(
        accepted: Consumer<RoundCountAcceptedResponse>?,
        rejected: Consumer<RoundCountRejectedResponse>?,
        failed: Consumer<ErrorType>?
    ) {
        acceptedAction = accepted
        rejectedAction = rejected
        failedAction = failed
    }

    fun setOnConnectionLost(onConnectionLost: Runnable?) {
        this.onConnectionLost = onConnectionLost
    }

    fun shutdown() {
        requestSubmitter.shutdown()
    }
}
