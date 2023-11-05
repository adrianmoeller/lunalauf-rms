package lunalauf.rms.utilities.network.server;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import network.util.ConnectionInitiationHelper;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;

public class ClientCatchService extends Service<Void> {

	private static final int timeout = 1000; // ms
	private final int maxIterations = 30;

	private final ClientHandler clientHandler;
	private final ServerSocket serverSocket;

	public ClientCatchService(ExecutorService threadPool, ClientHandler clientHandler, ServerSocket serverSocket) {
		this.clientHandler = clientHandler;
		this.serverSocket = serverSocket;

		this.setExecutor(threadPool);
		try {
			this.serverSocket.setSoTimeout(timeout);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<>() {
			@Override
			protected Void call() throws Exception {
				int it = 0;
				while (!isCancelled() && it < maxIterations) {
					try {
						Socket socket = serverSocket.accept();
						Client client = new Client(socket);
						clientHandler.handleClient(client);
					} catch (SocketTimeoutException eTimeout) {
						it++;
					}
				}
				return null;
			}
		};
	}

}
