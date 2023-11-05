package lunalauf.rms.utilities.network.server;

import LunaLaufLanguage.FunfactorResult;
import LunaLaufLanguage.Round;
import LunaLaufLanguage.Runner;
import LunaLaufLanguage.Team;
import lunalauf.rms.utilities.network.communication.ErrorType;
import lunalauf.rms.utilities.network.communication.message.request.MinigameRecordRequest;
import lunalauf.rms.utilities.network.communication.message.request.Request;
import lunalauf.rms.utilities.network.communication.message.request.RoundCountRequest;
import lunalauf.rms.utilities.network.communication.message.request.RunnerInfoRequest;
import lunalauf.rms.utilities.network.communication.message.response.Response;
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory;

import java.util.concurrent.Callable;

public class ModelUpdater implements Callable<Response> {

    private final LunaLaufAPI api;
    private final Request request;
    private final ResponseFactory responseFactory;

    private Response response = null;

    public ModelUpdater(LunaLaufAPI api, Request request, ResponseFactory responseFactory) {
        this.api = api;
        this.request = request;
        this.responseFactory = responseFactory;
    }

    @Override
    public Response call() throws Exception {
        synchronized (api) {
            handleRequest(this.request);
        }

        if (response == null)
            response = responseFactory.createErrorResponse(request.messageId, ErrorType.BAD_SERVER_STATE);
        return response;
    }

    private void handleRequest(Request request) {
        Result<?> result;
        if (request instanceof RoundCountRequest roundCountRequest) {
            result = handleRoundCountRequest(roundCountRequest);
        } else if (request instanceof MinigameRecordRequest minigameRecordRequest) {
            result = handleMinigameRecordRequest(minigameRecordRequest);
        } else if (request instanceof RunnerInfoRequest runnerInfoRequest) {
            result = handleRunnerInfoRequest(runnerInfoRequest);
        } else {
            response = responseFactory.createErrorResponse(request.messageId, ErrorType.UNEXPECTED_CLIENT_MESSAGE);
            result = new Result<Void>("Handle Incoming Request").failed("Unsupported request type", null);
        }
        result.log();
    }

    private Result<Round> handleRoundCountRequest(RoundCountRequest request) {
        Result<Round> res = new Result<>("Handle Round Count Request");

        Result<Runner> resRunner = res.makeSub(api.getRunner(request.runnerId));
        if (!resRunner.hasResult()) {
            response = responseFactory.createErrorResponse(request.messageId, ErrorType.UNKNOWN_ID);
            return res.failed("Request data and model data are inconsistent", null);
        }

        Runner runner = resRunner.getResult();
        Result<Round> resRound = res.makeSub(api.logRound(runner, LunaLaufAPI.DEFAULT_ROUND_POINTS, false));
        if (!resRound.hasResult()) {
            if (resRound.isFailed()) {
                response = responseFactory.createErrorResponse(request.messageId, ErrorType.BAD_SERVER_STATE);
                return res.failed("Failed applying data to model", null);
            } else if (resRound.getCode() == 4) {
                response = responseFactory.createRoundCountRejectedResponse(request.messageId, runner.getName(),
                        "Runden können nur innerhalb der Laufzeit gezählt werden!");
                return res.passed(null, 0, "Run is disabled", Lvl.WARN);
            } else if (resRound.getCode() == 5) {
                response = responseFactory.createRoundCountRejectedResponse(request.messageId, runner.getName(),
                        "Deine/Eure letzte Runde wurde bereits gezählt.");
                return res.passed(null, 0, "Last round already logged", Lvl.WARN);
            } else {
                response = responseFactory.createRoundCountRejectedResponse(request.messageId, runner.getName(),
                        "Lichtgeschwindigkeit nicht erlaubt!");
                return res.passed(null, 0, "Count interval is too short", Lvl.WARN);
            }
        }

        String name = "";
        int rounds = -1;
        if (runner.getTeam() != null) {
            name = runner.getTeam().getName();
            rounds = runner.getTeam().numOfRounds();
        } else {
            if (runner.isSetName())
                name = runner.getName();
            else
                name = String.valueOf(runner.getId());
            rounds = runner.numOfRounds();
        }
        response = responseFactory.createRoundCountAcceptedResponse(request.messageId, name, rounds);
        return res.passed(resRound.getResult(), 1, "Done", Lvl.INFO);
    }

    private Result<FunfactorResult> handleMinigameRecordRequest(MinigameRecordRequest request) {
        Result<FunfactorResult> res = new Result<>("Handle Round Request");

        Result<Runner> resRunner = res.makeSub(api.getRunner(request.runnerId));
        if (!resRunner.hasResult()) {
            response = responseFactory.createMinigameRecordFailedResponse(request.messageId, "Chip nicht registriert");
            return res.failed("Request data and model data are inconsistent", null);
        }

        Team team = resRunner.getResult().getTeam();
        if (team == null) {
            response = responseFactory.createMinigameRecordFailedResponse(request.messageId, "Kein Mitglied eines Teams");
            return res.failed("Runner representates no Team to attach Minigame to", null);
        }

        Result<FunfactorResult> resMinigameResult = res.makeSub(api.logMinigameResult(team, request.minigameId, request.points));
        if (!resMinigameResult.hasResult()) {
            response = responseFactory.createMinigameRecordFailedResponse(request.messageId, "Ergebnis konnte nicht eingetragen werden!");
            return res.failed("Failed applying data to model", null);
        }

        response = responseFactory.createMinigameRecordDoneResponse(request.messageId);
        return res.passed(resMinigameResult.getResult(), 1, "Done", Lvl.INFO);
    }

    private Result<Runner> handleRunnerInfoRequest(RunnerInfoRequest request) {
        Result<Runner> res = new Result<>("Handle Runner Info Request");

        Result<Runner> resRunner = res.makeSub(api.getRunner(request.runnerId));
        if (!resRunner.hasResult()) {
            response = responseFactory.createErrorResponse(request.messageId, ErrorType.UNKNOWN_ID);
            return res.failed("Request data and model data are inconsistent", null);
        }

        Runner runner = resRunner.getResult();

        String runnerName = "-";
        if (runner.getName() != null)
            runnerName = runner.getName();
        if (runner.getTeam() == null) {
            response = responseFactory.createRunnerInfoResponse(request.messageId, runnerName, request.runnerId, runner.numOfRounds());
        } else {
            String teamName = runner.getTeam().getName();
            int teamRounds = runner.getTeam().numOfRounds();
            int teamFunfactors = runner.getTeam().numOfFunfactorPoints();
            response = responseFactory.createTeamRunnerInfoResponse(request.messageId,
                    runnerName, request.runnerId, runner.numOfRounds(), teamName, teamRounds, teamFunfactors);
        }
        return res.passed(runner, 1, "Done", Lvl.INFO);
    }

}
