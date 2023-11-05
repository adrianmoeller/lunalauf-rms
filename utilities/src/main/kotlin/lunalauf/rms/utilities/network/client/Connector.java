package lunalauf.rms.utilities.network.client;

import javafx.concurrent.Task;
import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper;
import lunalauf.rms.utilities.network.util.PortProvider;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Connector {

    private static final int timeout = 200; // ms
    private final ExecutorService executor;
    private ConnectionTask currentTask = null;

    public Connector() {
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void connect(String host, int port, Consumer<Client> onConnected, Runnable onAborted) {
        cancel();
        currentTask = new ConnectionTask(host, port);
        currentTask.setOnSucceeded(event -> {
            try {
                Client client = currentTask.get();
                if (client != null)
                    onConnected.accept(client);
                else
                    onAborted.run();
            } catch (Exception e) {
                onAborted.run();
            }
        });
        currentTask.setOnCancelled(event -> onAborted.run());
        currentTask.setOnFailed(event -> onAborted.run());
        executor.execute(currentTask);
    }

    public void cancel() {
        if (currentTask != null)
            currentTask.cancel();
    }

    public void shutdown() {
        cancel();
        try {
            executor.shutdown();
            if (!executor.awaitTermination(3, TimeUnit.SECONDS))
                executor.shutdownNow();
        } catch (InterruptedException ignored) {
        }
    }

    private class ConnectionTask extends Task<Client> {
        private final String host;
        private final int port;

        private ConnectionTask(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        protected Client call() throws Exception {
            while (true) {
                if (isCancelled())
                    return null;

                if (this.port > 0 && this.port <= 65535) {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(host, port), timeout * 2);
                        Client client = new Client(socket);
                        if (initialCommunicationTest(client))
                            return client;
                        else {
                            client.close();
                            throw new Exception("Communication test failed");
                        }
                    } catch (IOException ignored) {
                    }
                } else {
                    for (int prefPort : PortProvider.getPreferredPorts()) {
                        if (isCancelled())
                            return null;
                        try {
                            Socket socket = new Socket();
                            socket.connect(new InetSocketAddress(host, prefPort), timeout);
                            Client client = new Client(socket);
                            if (initialCommunicationTest(client))
                                return client;
                            else
                                client.close();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private boolean initialCommunicationTest(Client client) throws Exception {
        client.setTimeout(5000);
        try{
            String synMessage = ConnectionInitiationHelper.getSynMessage();
            String expectedAckMessage = ConnectionInitiationHelper.getAckMessage(synMessage);

            client.send(synMessage);
            String ackMessage = client.receive();

            if (!expectedAckMessage.equals(ackMessage))
                return false;

            String synAckMessage = ConnectionInitiationHelper.getAckMessage(ackMessage);
            client.send(synAckMessage);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            client.resetTimeout();
        }
    }

}
