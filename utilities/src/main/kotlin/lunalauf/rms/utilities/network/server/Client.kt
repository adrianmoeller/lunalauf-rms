package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lunalauf.rms.modelapi.ModelState
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import java.util.*

class Client(
    private val socket: Socket
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val writer: PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private val reader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private val _status = MutableStateFlow(0)

    /**
     * Connection status of this client.
     *
     * -1: disconnected<br></br>
     * 0: connected, not listening<br></br>
     * 1: connected, listening
     */
    val status get() = _status.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var requestHandler: RequestHandler? = null

    fun send(data: String) {
        writer.println(data)
    }

    @Throws(IOException::class)
    fun receive(): String? {
        return reader.readLine()
    }

    @Throws(IOException::class)
    fun receiveAll(): LinkedList<String> {
        val lines: LinkedList<String> = LinkedList<String>()
        socket.setSoTimeout(100)
        try {
            while (true) {
                val line: String = reader.readLine() ?: return lines
                lines.add(line)
            }
        } catch (e: Exception) {
            return lines
        } finally {
            socket.setSoTimeout(0)
        }
    }

    @Throws(SocketException::class)
    fun setTimeout(timeout: Int) {
        socket.setSoTimeout(timeout)
    }

    @Throws(SocketException::class)
    fun resetTimeout() {
        socket.setSoTimeout(0)
    }

    @Throws(IOException::class)
    fun close() {
        socket.close()
    }

    fun startListening(modelState: StateFlow<ModelState>) {
        requestHandler = RequestHandler(
            client = this,
            modelState = modelState,
            status = _status,
            scope = scope,
        )
        logger.info("Started listening to: {}", remoteAddress)
    }

    fun stopListening() {
        requestHandler?.cancel()
        try {
            if (!socket.isClosed)
                socket.close()
        } catch (_: IOException) {
            logger.error("Failed to close socket: {}", remoteAddress)
        }
        logger.info("Stopped listening to: {}", remoteAddress)
    }

    val remoteAddress: String
        get() = socket.getInetAddress()?.hostAddress ?: "None"

    fun onLost(action: () -> Unit) {
        scope.launch {
            status.collect {
                if (it < 0) action()
            }
        }
    }
}
