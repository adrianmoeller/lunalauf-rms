package lunalauf.rms.utilities.network.client

import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.RequestSubmitter
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.RunnerInfoResponse
import lunalauf.rms.utilities.network.util.RepetitionHandler
import java.util.function.Consumer

class InfoDisplay {
    var client: Client? = null
    private val requestSubmitter: RequestSubmitter
    private val requestFactory: RequestFactory
    private val repetitionHandler: RepetitionHandler
    private var succeededAction: Consumer<RunnerInfoResponse>? = null
    private var failedAction: Consumer<ErrorType>? = null
    private var onConnectionLost: Runnable? = null

    init {
        requestSubmitter =
            RequestSubmitter({ response: Response -> handleResponse(response) }) { error: ErrorType -> handleError(error) }
        requestFactory = RequestFactory()
        repetitionHandler = RepetitionHandler(3000)
    }

    private fun handleResponse(response: Response) {
        if (response is RunnerInfoResponse) {
            if (succeededAction != null) succeededAction!!.accept(response)
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
        requestSubmitter.submit(requestFactory.createRunnerInfoRequest(runnerId), client)
    }

    fun hasClient(): Boolean {
        return client != null
    }

    fun setActions(succeeded: Consumer<RunnerInfoResponse>?, failed: Consumer<ErrorType>?) {
        succeededAction = succeeded
        failedAction = failed
    }

    fun setOnConnectionLost(onConnectionLost: Runnable?) {
        this.onConnectionLost = onConnectionLost
    }

    fun shutdown() {
        requestSubmitter.shutdown()
    }
}
