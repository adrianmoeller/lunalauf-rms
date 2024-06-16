package lunalauf.rms.utilities.network.communication.message.response

import lunalauf.rms.utilities.network.communication.ErrorType

class ResponseFactory {
    fun createErrorResponse(requestId: Long, error: ErrorType): ErrorResponse {
        val response = ErrorResponse()
        response.messageId = requestId
        response.error = error
        return response
    }

    fun createMinigameRecordDoneResponse(requestId: Long): MinigameRecordDoneResponse {
        val response = MinigameRecordDoneResponse()
        response.messageId = requestId
        return response
    }

    fun createMinigameRecordFailedResponse(requestId: Long, causeMessage: String?): MinigameRecordFailedResponse {
        val response = MinigameRecordFailedResponse()
        response.messageId = requestId
        response.causeMessage = causeMessage
        return response
    }

    fun createRoundCountAcceptedResponse(
        requestId: Long,
        name: String,
        newNumRounds: Int
    ): RoundCountAcceptedResponse {
        val response = RoundCountAcceptedResponse()
        response.messageId = requestId
        response.name = name
        response.newNumRounds = newNumRounds
        return response
    }

    fun createRoundCountRejectedResponse(
        requestId: Long,
        name: String,
        causeMessage: String?
    ): RoundCountRejectedResponse {
        val response = RoundCountRejectedResponse()
        response.messageId = requestId
        response.name = name
        response.causeMessage = causeMessage
        return response
    }

    fun createRunnerInfoResponse(
        requestId: Long,
        runnerName: String,
        runnerId: Long,
        numRunnerRounds: Int
    ): RunnerInfoResponse {
        val response = RunnerInfoResponse()
        response.messageId = requestId
        response.runnerName = runnerName
        response.runnerId = runnerId
        response.numRunnerRounds = numRunnerRounds
        return response
    }

    fun createTeamRunnerInfoResponse(
        requestId: Long,
        runnerName: String,
        runnerId: Long,
        numRunnerRounds: Int,
        teamName: String,
        numTeamRounds: Int,
        teamFunfactorPoints: Int
    ): TeamRunnerInfoResponse {
        val response = TeamRunnerInfoResponse()
        response.messageId = requestId
        response.runnerName = runnerName
        response.runnerId = runnerId
        response.numRunnerRounds = numRunnerRounds
        response.teamName = teamName
        response.numTeamRounds = numTeamRounds
        response.teamFunfactorPoints = teamFunfactorPoints
        return response
    }
}
