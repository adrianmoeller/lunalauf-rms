package lunalauf.rms.utilities.network.communication;

import com.google.gson.JsonParseException;
import javafx.concurrent.Task;
import lunalauf.rms.utilities.network.client.Client;
import lunalauf.rms.utilities.network.communication.message.Message;
import lunalauf.rms.utilities.network.communication.message.request.Request;
import lunalauf.rms.utilities.network.communication.message.response.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class RequestSubmitter {

    private final Consumer<Response> responseHandler;
    private final Consumer<ErrorType> errorHandler;
    private final ExecutorService executor;

    private final int numMessagesToDump = 5;

    public RequestSubmitter(Consumer<Response> responseHandler, Consumer<ErrorType> errorHandler) {
        this.responseHandler = responseHandler;
        this.errorHandler = errorHandler;
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void submit(Request request, Client client) {
        SubmissionTask task = new SubmissionTask(request, client);

        task.setOnSucceeded(event -> {
            try {
                Response response = task.get();
                responseHandler.accept(response);
            } catch (Exception e) {
                errorHandler.accept(ErrorType.UNWANTED_TERMINATION);
            }
        });
        task.setOnCancelled(event -> errorHandler.accept(ErrorType.UNWANTED_TERMINATION));
        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            ErrorType error;
            if (exception instanceof Client.NoAnswerException)
                error = ErrorType.RESPONSE_TIMEOUT;
            else if (exception instanceof Client.NoConnectionException)
                error = ErrorType.DISCONNECTED;
            else if (exception instanceof CorruptedMessageException)
                error = ErrorType.CORRUPTED_SERVER_MESSAGE;
            else
                error = ErrorType.CORRUPTED_SERVER_MESSAGE;

            errorHandler.accept(error);
        });

        executor.execute(task);
    }

    public void shutdown() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(3, TimeUnit.SECONDS))
                executor.shutdownNow();
        } catch (InterruptedException ignored) {
        }
    }

    private class SubmissionTask extends Task<Response> {
        private final Request request;
        private final Client client;

        SubmissionTask(Request request, Client client) {
            this.request = request;
            this.client = client;
        }

        @Override
        protected Response call() throws Client.NoAnswerException, Client.NoConnectionException, CorruptedMessageException {
            if (client == null || !client.isConnected())
                throw new Client.NoConnectionException();

            client.send(MessageProcessor.toJsonString(request));
            Response response = getMatchingResponse(request);
            if (response == null)
                throw new CorruptedMessageException();
            return response;
        }

        private Response getMatchingResponse(Request request) throws Client.NoAnswerException, Client.NoConnectionException, CorruptedMessageException {
            int it = 0;
            while (it < numMessagesToDump) {
                String receivedString = client.receive();
                try {
                    Message receivedMessage = MessageProcessor.fromJsonString(receivedString);
                    if (receivedMessage instanceof Response response) {
                        if (response.messageId == request.messageId || response.messageId == -1)
                            return response;
                    }
                } catch (JsonParseException e) {
                    throw new CorruptedMessageException(e);
                }
                it++;
            }
            throw new Client.NoAnswerException();
        }
    }

    public static class CorruptedMessageException extends Exception {
        public CorruptedMessageException() {
            super();
        }

        public CorruptedMessageException(Throwable cause) {
            super(cause);
        }
    }

}
