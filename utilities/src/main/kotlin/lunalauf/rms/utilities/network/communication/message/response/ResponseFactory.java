package lunalauf.rms.utilities.network.communication.message.response;

import lunalauf.rms.utilities.network.communication.ErrorType;

public class ResponseFactory {

    public ErrorResponse createErrorResponse(long requestId, ErrorType error) {
        ErrorResponse response = new ErrorResponse();
        response.messageId = requestId;
        response.error = error;
        return response;
    }

    public MinigameRecordDoneResponse createMinigameRecordDoneResponse(long requestId) {
        MinigameRecordDoneResponse response = new MinigameRecordDoneResponse();
        response.messageId = requestId;
        return response;
    }

    public MinigameRecordFailedResponse createMinigameRecordFailedResponse(long requestId, String causeMessage) {
        MinigameRecordFailedResponse response = new MinigameRecordFailedResponse();
        response.messageId = requestId;
        response.causeMessage = causeMessage;
        return response;
    }

    public RoundCountAcceptedResponse createRoundCountAcceptedResponse(long requestId, String name, int newNumRounds) {
        RoundCountAcceptedResponse response = new RoundCountAcceptedResponse();
        response.messageId = requestId;
        response.name = name;
        response.newNumRounds = newNumRounds;
        return response;
    }

    public RoundCountRejectedResponse createRoundCountRejectedResponse(long requestId, String name, String causeMessage) {
        RoundCountRejectedResponse response = new RoundCountRejectedResponse();
        response.messageId = requestId;
        response.name = name;
        response.causeMessage = causeMessage;
        return response;
    }

    public RunnerInfoResponse createRunnerInfoResponse(long requestId, String runnerName, long runnerId, int numRunnerRounds) {
        RunnerInfoResponse response = new RunnerInfoResponse();
        response.messageId = requestId;
        response.runnerName = runnerName;
        response.runnerId = runnerId;
        response.numRunnerRounds = numRunnerRounds;
        return response;
    }

    public TeamRunnerInfoResponse createTeamRunnerInfoResponse(long requestId, String runnerName, long runnerId,
                                                               int numRunnerRounds, String teamName, int numTeamRounds,
                                                               int teamFunfactorPoints) {
        TeamRunnerInfoResponse response = new TeamRunnerInfoResponse();
        response.messageId = requestId;
        response.runnerName = runnerName;
        response.runnerId = runnerId;
        response.numRunnerRounds = numRunnerRounds;
        response.teamName = teamName;
        response.numTeamRounds = numTeamRounds;
        response.teamMinigamePoints = teamFunfactorPoints;
        return response;
    }

}
