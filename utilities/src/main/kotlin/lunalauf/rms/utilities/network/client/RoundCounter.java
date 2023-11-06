package lunalauf.rms.utilities.network.client;

import lunalauf.rms.utilities.network.communication.ErrorType;
import lunalauf.rms.utilities.network.communication.RequestSubmitter;
import lunalauf.rms.utilities.network.communication.message.request.RequestFactory;
import lunalauf.rms.utilities.network.communication.message.response.ErrorResponse;
import lunalauf.rms.utilities.network.communication.message.response.Response;
import lunalauf.rms.utilities.network.communication.message.response.RoundCountAcceptedResponse;
import lunalauf.rms.utilities.network.communication.message.response.RoundCountRejectedResponse;
import lunalauf.rms.utilities.network.util.FixedObservableQueue;
import lunalauf.rms.utilities.network.util.RepetitionHandler;

import java.util.function.Consumer;

public class RoundCounter {

    private Client client = null;
    private final RequestSubmitter requestSubmitter;
    private final RequestFactory requestFactory;
    private final RepetitionHandler repetitionHandler;

    private final FixedObservableQueue<RoundCountAcceptedResponse> successQueue;

    private Consumer<RoundCountAcceptedResponse> acceptedAction;
    private Consumer<RoundCountRejectedResponse> rejectedAction;
    private Consumer<ErrorType> failedAction;
    private Runnable onConnectionLost;

    public RoundCounter() {
        requestSubmitter = new RequestSubmitter(this::handleResponse, this::handleError);
        requestFactory = new RequestFactory();
        repetitionHandler = new RepetitionHandler(3000);
        successQueue = new FixedObservableQueue<>(4);
    }

    private void handleResponse(Response response) {
        if (response instanceof RoundCountAcceptedResponse acceptedResponse) {
            successQueue.push(acceptedResponse);
            if (acceptedAction != null)
                acceptedAction.accept(acceptedResponse);
        } else if (response instanceof RoundCountRejectedResponse rejectedResponse) {
            if (rejectedAction != null)
                rejectedAction.accept(rejectedResponse);
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
        requestSubmitter.submit(requestFactory.createRoundCountRequest(runnerId), client);
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

    public FixedObservableQueue<RoundCountAcceptedResponse> getSuccessQueue() {
        return successQueue;
    }

    public void setActions(Consumer<RoundCountAcceptedResponse> accepted, Consumer<RoundCountRejectedResponse> rejected, Consumer<ErrorType> failed) {
        this.acceptedAction = accepted;
        this.rejectedAction = rejected;
        this.failedAction = failed;
    }

    public void setOnConnectionLost(Runnable onConnectionLost) {
        this.onConnectionLost = onConnectionLost;
    }

    public void shutdown() {
        requestSubmitter.shutdown();
    }

}
