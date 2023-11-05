package lunalauf.rms.utilities.network.server;

import com.google.gson.JsonParseException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lunalauf.rms.utilities.network.communication.ErrorType;
import lunalauf.rms.utilities.network.communication.MessageProcessor;
import lunalauf.rms.utilities.network.communication.message.Message;
import lunalauf.rms.utilities.network.communication.message.request.Request;
import lunalauf.rms.utilities.network.communication.message.response.Response;
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory;

import java.util.concurrent.FutureTask;

public class RequestHandler extends Task<Integer> {

    private final Client client;
    private final LunaLaufAPI api;
    private final ResponseFactory responseFactory;

    public RequestHandler(Client client, LunaLaufAPI api) {
        this.client = client;
        this.api = api;
        this.responseFactory = new ResponseFactory();
    }

    @Override
    protected Integer call() throws Exception {
        updateValue(0);

        try {
            executeProtocol();
        } catch (Exception ignored) {
        }

        updateValue(-1);
        return -1;
    }

    private void executeProtocol() throws Exception {
        updateValue(1);

        String messageString;
        while ((messageString = client.receive()) != null) {
            Response response = null;

            Message message = null;
            try {
                message = MessageProcessor.fromJsonString(messageString);
            } catch (JsonParseException e) {
                response = responseFactory.createErrorResponse(-1, ErrorType.CORRUPTED_CLIENT_MESSAGE);
            }

            if (message != null) {
                if (message instanceof Request request) {
                    FutureTask<Response> task = new FutureTask<>(new ModelUpdater(api, request, responseFactory));
                    Platform.runLater(task);
                    try {
                        response = task.get();
                    } catch (Exception e) {
                        response = responseFactory.createErrorResponse(message.messageId, ErrorType.BAD_SERVER_STATE);
                    }
                } else {
                    response = responseFactory.createErrorResponse(message.messageId, ErrorType.UNEXPECTED_CLIENT_MESSAGE);
                }
            }

            client.send(MessageProcessor.toJsonString(response));

            if (isCancelled())
                break;
        }
    }

}
