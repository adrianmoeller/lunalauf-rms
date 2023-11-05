package lunalauf.rms.utilities.network.server;

import lunalauf.rms.utilities.network.util.PortProvider;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetworkManager {

	private ServerSocket serverSocket;

	private final ExecutorService threadPool;

	private final ClientHandler clientHandler;
	private final ClientCatcher clientCatcher;

	public NetworkManager(LunaLaufAPI api) {
		createServerSocket();
		threadPool = Executors.newCachedThreadPool();

		clientHandler = new ClientHandler(threadPool, api);
		clientCatcher = new ClientCatcher(threadPool, clientHandler, serverSocket);
	}

	private void createServerSocket() {
		for (int port : PortProvider.getPreferredPorts()) {
			try {
				serverSocket = new ServerSocket(port);
				return;
			} catch (IOException ignored) {
			}
		}

		try {
			serverSocket = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void shutdown() {
		clientCatcher.stop();
		clientHandler.getClients().forEach(Client::stopListening);
		try {
			threadPool.shutdown();
			if (threadPool.awaitTermination(3, TimeUnit.SECONDS))
				threadPool.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}
	
	public String getLocalAddress() {
		return serverSocket.getInetAddress().getHostAddress();
	}

	public ClientHandler getClientHandler() {
		return clientHandler;
	}

	public ClientCatcher getClientCatcher() {
		return clientCatcher;
	}

}
