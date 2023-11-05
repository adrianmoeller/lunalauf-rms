package lunalauf.rms.utilities.network.client;

import lunalauf.rms.utilities.network.communication.ErrorType;
import lunalauf.rms.utilities.network.communication.RequestSubmitter;
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory;
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse;
import lunalauf.rms.utilities.network.communication.message.response.Response;
import lunalauf.rms.utilities.network.communication.message.response.RunnerInfoResponse;
import lunalauf.rms.utilities.network.util.RepetitionHandler;

import java.util.function.Consumer;

public class InfoDisplay {

    private Client client = null;

    private final RequestSubmitter requestSubmitter;
    private final RequestFactory requestFactory;
    private final RepetitionHandler repetitionHandler;

    private Consumer<RunnerInfoResponse> succeededAction;
    private Consumer<ErrorType> failedAction;
    private Runnable onConnectionLost;

    public InfoDisplay() {
        requestSubmitter = new RequestSubmitter(this::handleResponse, this::handleError);
        requestFactory = new RequestFactory();
        repetitionHandler = new RepetitionHandler(3000);
    }

    private void handleResponse(Response response) {
        if (response instanceof RunnerInfoResponse runnerInfoResponse) {
            if (succeededAction != null)
                succeededAction.accept(runnerInfoResponse);
        } else if (response instanceof ErrorResponse errorResponse) {
            if (failedAction != null)
                failedAction.accept(errorResponse.error);
        } else {
            if (failedAction != null)
                failedAction.accept(ErrorType.UNEXPECTED_SERVER_MESSAGE);
        }
    }

    private void handleError(ErrorType error) {
        if (failedAction != null)
            failedAction.accept(error);
        if (error == ErrorType.DISCONNECTED && onConnectionLost != null)
            onConnectionLost.run();
    }

    public void processInput(long runnerId) {
        if (repetitionHandler.isUnwantedRepetition(runnerId))
            return;
        requestSubmitter.submit(requestFactory.createRunnerInfoRequest(runnerId), client);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public boolean hasClient() {
        return client != null;
    }

    public void setActions(Consumer<RunnerInfoResponse> succeeded, Consumer<ErrorType> failed) {
        this.succeededAction = succeeded;
        this.failedAction = failed;
    }

    public void setOnConnectionLost(Runnable onConnectionLost) {
        this.onConnectionLost = onConnectionLost;
    }

    public void shutdown() {
        requestSubmitter.shutdown();
    }
}
