package lunalauf.rms.utilities.network.server;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;

public class Client {

    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    /**
     * <p>
     * Connection status of this client.
     * </p>
     * -1: disconnected<br>
     * 0: connected, not listening<br>
     * 1: connected, listening
     */
    private final IntegerProperty status;

    private RequestHandler requestHandler = null;

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        status = new SimpleIntegerProperty(0);
    }

    public void send(String data) {
        writer.println(data);
    }

    public String receive() throws IOException {
        return reader.readLine();
    }

    public LinkedList<String> receiveAll() throws IOException {
        LinkedList<String> lines = new LinkedList<>();
        socket.setSoTimeout(100);
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null)
                    return lines;
                lines.add(line);
            }
        } catch (Exception e) {
            return lines;
        } finally {
            socket.setSoTimeout(0);
        }
    }

    public void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public void resetTimeout() throws SocketException {
        socket.setSoTimeout(0);
    }

    public void close() throws IOException {
        socket.close();
    }

    public final int getStatus() {
        return status.get();
    }

    public IntegerProperty statusProperty() {
        return status;
    }

    public void startListening(ExecutorService threadPool, LunaLaufAPI api) {
        requestHandler = new RequestHandler(this, api);
        status.bind(requestHandler.valueProperty());
        requestHandler.setOnFailed(value -> status.set(-1));
        threadPool.execute(requestHandler);
    }

    public void stopListening() {
        if (requestHandler == null)
            return;

        requestHandler.cancel();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public String getRemoteAddress() {
        return socket.getInetAddress().getHostAddress();
    }

}
