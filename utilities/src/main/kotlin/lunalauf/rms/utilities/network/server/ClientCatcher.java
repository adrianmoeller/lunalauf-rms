package lunalauf.rms.utilities.network.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class ClientCatcher {

    private final ClientCatchService service;

    public ClientCatcher(ExecutorService threadPool, ClientHandler clientHandler, ServerSocket serverSocket) {
        this.service = new ClientCatchService(threadPool, clientHandler, serverSocket);
        clientHandler.setOnLostClients(lostClients -> {
			Platform.runLater(this::start);
        });
    }

    public void start() {
        if (service.getState().equals(State.RUNNING))
            service.restart();
        else {
            service.reset();
            service.start();
        }
    }

    public void stop() {
        service.cancel();
    }

    public void setOnStarted(EventHandler<WorkerStateEvent> event) {
        service.setOnRunning(event);
    }

    public void setOnStopped(EventHandler<WorkerStateEvent> event) {
        service.setOnSucceeded(event);
        service.setOnCancelled(event);
        service.setOnFailed(event);
    }
}
