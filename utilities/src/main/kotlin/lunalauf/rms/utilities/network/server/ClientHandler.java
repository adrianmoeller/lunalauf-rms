package lunalauf.rms.utilities.network.server;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ClientHandler {

    private final ExecutorService threadPool;
    private final LunaLaufAPI api;
    private final ObservableList<Client> clients;

    public ClientHandler(ExecutorService threadPool, LunaLaufAPI api) {
        this.threadPool = threadPool;
        this.api = api;
        this.clients = FXCollections.observableList(new ArrayList<>(),
                client -> new Observable[]{client.statusProperty()});
    }

    public void handleClient(Client client) {
        CommunicationInitiationTask task = new CommunicationInitiationTask(client);
        threadPool.execute(task);
    }

    private class CommunicationInitiationTask extends Task<Void> {

        private static final int comTestTimeout = 2000; // ms
        private final Client client;

        private CommunicationInitiationTask(Client client) {
            this.client = client;
        }

        @Override
        protected Void call() throws Exception {
            if (initialCommunicationTest(client)) {
                Platform.runLater(() -> ClientHandler.this.registerClient(client));
                client.startListening(threadPool, api);
            } else {
                client.close();
            }
            return null;
        }

        private boolean initialCommunicationTest(Client client) {
            try {
                client.setTimeout(comTestTimeout);
                String synMessage = client.receive();

                String ackMessage = ConnectionInitiationHelper.getAckMessage(synMessage);
                client.send(ackMessage);

                String synAckMessage = client.receive();
                String expectedSynAckMessage = ConnectionInitiationHelper.getAckMessage(ackMessage);

                return expectedSynAckMessage.equals(synAckMessage);
            } catch (Exception e) {
                return false;
            } finally {
                try {
                    client.resetTimeout();
                } catch (SocketException ignored) {
                }
            }
        }
    }

    private void registerClient(Client client) {
        Client existing = null;
        for (Client otherClient : clients) {
            if (otherClient.getRemoteAddress().equals(client.getRemoteAddress()))
                if (otherClient.getStatus() == -1)
                    existing = otherClient;
        }
        if (existing != null)
            clients.set(clients.indexOf(existing), client);
        else
            clients.add(client);
    }

    public ObservableList<Client> getClients() {
        return clients;
    }

    public void setOnLostClients(Consumer<List<? extends Client>> listener) {
        this.clients.addListener((ListChangeListener<Client>) change -> {
            if (change.next() && change.wasUpdated()) {
                var lostClients = change.getList().stream().filter(c -> c.getStatus() < 0).toList();
                if (!lostClients.isEmpty())
                    listener.accept(lostClients);
            }
        });
    }

}
