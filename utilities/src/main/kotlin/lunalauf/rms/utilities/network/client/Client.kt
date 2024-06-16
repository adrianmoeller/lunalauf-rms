package lunalauf.rms.utilities.network.client

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class Client internal constructor(
    private val socket: Socket
) {
    companion object {
        private const val TIMEOUT = 1000 // ms
        private const val MAX_ITERATIONS = 4
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val writer: PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private val reader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private val _connectionStatus = MutableStateFlow(1)
    val connectionStatus get() = _connectionStatus.asStateFlow()

    private val _ping = MutableStateFlow(-1L)
    val ping get() = _ping.asStateFlow()

    init {
        try {
            socket.setSoTimeout(TIMEOUT)
        } catch (e: SocketException) {
            logger.error("Could not set socket timeout. Behaviour unpredictable", e)
        }
    }

    fun send(data: String) {
        writer.println(data)
    }

    @Throws(NoAnswerException::class, NoConnectionException::class)
    fun receive(): String {
        val pingTick = System.currentTimeMillis()
        var it = 0
        while (it < MAX_ITERATIONS) {
            try {
                val line = reader.readLine()
                if (line == null) {
                    _connectionStatus.value = -1
                    throw NoAnswerException()
                }
                _connectionStatus.value = 1
                _ping.value = System.currentTimeMillis() - pingTick
                return line
            } catch (_: SocketTimeoutException) {
                _connectionStatus.value = 0
                it++
            } catch (e: IOException) {
                _connectionStatus.value = -1
                throw NoConnectionException(e)
            }
        }
        throw NoAnswerException()
    }

    val isConnected: Boolean
        get() {
            if (socket.isConnected && !socket.isClosed)
                return true
            _connectionStatus.value = -1
            return false
        }

    @Throws(SocketException::class)
    fun setTimeout(timeout: Int) {
        socket.setSoTimeout(timeout)
    }

    @Throws(SocketException::class)
    fun resetTimeout() {
        socket.setSoTimeout(TIMEOUT)
    }

    @Throws(IOException::class)
    fun close() {
        socket.close()
    }

    val port get() = socket.getPort()

    val host get() = socket.getInetAddress()?.hostAddress ?: "None"

    class NoConnectionException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }

    class NoAnswerException : Exception {
        constructor() : super()
        constructor(cause: Throwable?) : super(cause)
    }
}
