package lunalauf.rms.utilities.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javafx.beans.property.*;

public class Client {

    private static final int timeout = 1000; // ms
    private static final int maxIterations = 4;

    private final Socket socket;
    private final PrintWriter writer;
    private final BufferedReader reader;

    private final IntegerProperty connectionStatus;
    private final LongProperty ping;

    Client(Socket socket) throws IOException {
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        socket.setSoTimeout(timeout);

        connectionStatus = new SimpleIntegerProperty(1);
        ping = new SimpleLongProperty(-1);
    }

    public void send(String data) {
        writer.println(data);
    }

    public String receive() throws NoAnswerException, NoConnectionException {
        long pingTick = System.currentTimeMillis();

        int it = 0;
        while (it < maxIterations) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    connectionStatus.set(-1);
                    throw new NoAnswerException();
                }
                connectionStatus.set(1);
                ping.set(System.currentTimeMillis() - pingTick);
                return line;
            } catch (SocketTimeoutException e) {
                connectionStatus.set(0);
                it++;
            } catch (IOException e) {
                connectionStatus.set(-1);
                throw new NoConnectionException(e);
            }
        }
        throw new NoAnswerException();
    }

    public boolean isConnected() {
        if (socket.isConnected() && !socket.isClosed())
            return true;

        connectionStatus.set(-1);
        return false;
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public void resetTimeout() throws SocketException {
        socket.setSoTimeout(timeout);
    }

    public void close() throws IOException {
        socket.close();
    }

    public int getPort() {
        return socket.getPort();
    }

    public String getHost() {
        return socket.getInetAddress().getHostAddress();
    }

    public ReadOnlyIntegerProperty connectionStatusProperty() {
        return connectionStatus;
    }

    public ReadOnlyLongProperty pingProperty() {
        return ping;
    }

    public static class NoConnectionException extends Exception {
        public NoConnectionException() {
            super();
        }

        public NoConnectionException(Throwable cause) {
            super(cause);
        }
    }

    public static class NoAnswerException extends Exception {
        public NoAnswerException() {
            super();
        }

        public NoAnswerException(Throwable cause) {
            super(cause);
        }
    }

}
