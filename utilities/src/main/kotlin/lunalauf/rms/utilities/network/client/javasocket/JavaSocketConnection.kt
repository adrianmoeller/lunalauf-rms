package lunalauf.rms.utilities.network.client.javasocket

import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.client.Connection
import lunalauf.rms.utilities.network.communication.MessageProcessor
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

class JavaSocketConnection(
    private val socket: Socket
) : Connection {
    companion object {
        private const val TIMEOUT = 1000 // ms
        private const val TIMEOUT_COMMUNICATION_TEST = 5000 // ms
        private const val MAX_ITERATIONS = 4
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val writer: PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private val reader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    private val _status = MutableStateFlow(1)
    private val _ping = MutableStateFlow(-1L)

    override val status: StateFlow<Int>
        get() = _status.asStateFlow()

    override val ping: StateFlow<Long>
        get() = _ping.asStateFlow()

    init {
        try {
            socket.setSoTimeout(TIMEOUT)
        } catch (e: SocketException) {
            logger.error("Could not set socket timeout. Behaviour unpredictable", e)
        }
    }

    override fun sendMessage(message: Message) {
        if (!isConnected()) {
            logger.error("Not connected to server")
            throw Connection.NoConnectionException()
        }

        send(MessageProcessor.toJsonString(message))
        logger.info("Message sent: {}", message)
    }

    override fun receiveMessage(): Message {
        val receivedString = receive()
        try {
            return MessageProcessor.fromJsonString(receivedString)
        } catch (e: JsonParseException) {
            logger.warn("Received corrupted server message", e)
            throw Connection.CorruptedMessageException(e)
        }
    }

    private fun isConnected(): Boolean {
        if (socket.isConnected && !socket.isClosed)
            return true
        _status.value = -1
        return false
    }

    private fun send(data: String) {
        writer.println(data)
    }

    @Throws(
        Connection.NoAnswerException::class,
        Connection.NoConnectionException::class
    )
    private fun receive(): String {
        val pingTick = System.currentTimeMillis()
        var it = 0
        while (it < MAX_ITERATIONS) {
            try {
                val line = reader.readLine()
                if (line == null) {
                    _status.value = -1
                    throw Connection.NoAnswerException()
                }
                _status.value = 1
                _ping.value = System.currentTimeMillis() - pingTick
                return line
            } catch (_: SocketTimeoutException) {
                _status.value = 0
                it++
            } catch (e: IOException) {
                _status.value = -1
                throw Connection.NoConnectionException(e)
            }
        }
        throw Connection.NoAnswerException()
    }

    @Throws(IOException::class)
    fun initiateCommunication(): Boolean {
        socket.soTimeout = TIMEOUT_COMMUNICATION_TEST
        return try {
            val synMessage: String = ConnectionInitiationHelper.synMessage
            val expectedAckMessage: String = ConnectionInitiationHelper.getAckMessage(synMessage)
            send(synMessage)
            val ackMessage = receive()
            if (expectedAckMessage != ackMessage) {
                logger.warn("Communication test failed")
                return false
            }
            val synAckMessage: String = ConnectionInitiationHelper.getAckMessage(ackMessage)
            send(synAckMessage)
            logger.info("Communication test successful")
            true
        } catch (e: Exception) {
            logger.warn("Communication test failed due to an exception", e)
            false
        } finally {
            socket.soTimeout = TIMEOUT
        }
    }

    @Throws(IOException::class)
    override fun close() {
        socket.close()
        logger.info("Connection closed")
    }
}