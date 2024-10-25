package lunalauf.rms.utilities.network.server.javasocket

import com.google.gson.JsonParseException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.MessageProcessor
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import lunalauf.rms.utilities.network.server.Connection
import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException

class JavaSocketConnection(
    private val socket: Socket
): Connection {
    companion object {
        private const val TIMEOUT_COMMUNICATION_TEST = 2000 // ms
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    private val _status = MutableStateFlow(0)
    private var communicationInitiationDone = false

    private val writer: PrintWriter = PrintWriter(socket.getOutputStream(), true)
    private val reader: BufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))

    override val status: StateFlow<Int>
        get() = _status.asStateFlow()
    override val address: String
        get() = socket.getInetAddress()?.hostAddress ?: "None"

    fun initiateCommunication(): Boolean {
        val initiationSuccessful = try {
            socket.setSoTimeout(TIMEOUT_COMMUNICATION_TEST)
            val synMessage = receive() ?: return false
            val ackMessage = ConnectionInitiationHelper.getAckMessage(synMessage)
            send(ackMessage)
            val synAckMessage = receive()
            val expectedSynAckMessage: String = ConnectionInitiationHelper.getAckMessage(ackMessage)
            val result = expectedSynAckMessage == synAckMessage
            logger.info("Communication test {}: {}", if (result) "successful" else "failed", address)
            result
        } catch (e: Exception) {
            logger.warn("Communication test failed due to an exception: {}", address, e)
            false
        } finally {
            try {
                socket.setSoTimeout(0)
            } catch (e: SocketException) {
                logger.error("Failed to reset socket timeout. Behaviour unpredictable: {}", address, e)
            }
        }

        if (initiationSuccessful)
            communicationInitiationDone = true
        return initiationSuccessful
    }

    fun startListening(onMessageReceived: suspend (Message) -> Message?) {
        if (!communicationInitiationDone) {
            logger.error("Connection initiation is missing: {}", address)
            return
        }

        job = scope.launch {
            try {
                receiveMessages(onMessageReceived)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                logger.error("Exception occurred while receiving from client", e)
            } finally {
                _status.value = -1
            }
        }
    }

    private suspend fun CoroutineScope.receiveMessages(onMessageReceived: suspend (Message) -> Message?) {
        _status.value = 1
        while (true) {
            if (!this.isActive)
                return

            val messageString = receive()
                ?: return

            val answer = try {
                val message = MessageProcessor.fromJsonString(messageString)
                onMessageReceived(message)
            } catch (_: JsonParseException) {
                logger.warn("Received corrupted client message from: {}", address)
                ResponseFactory.createErrorResponse(-1, ErrorType.CORRUPTED_CLIENT_MESSAGE)
            }

            if (answer != null) {
                send(MessageProcessor.toJsonString(answer))
                logger.info("Message sent: {} to: {}", answer, address)
            }
        }
    }

    fun stopListening() {
        job?.cancel()

        try {
            if (!socket.isClosed)
                socket.close()
        } catch (_: IOException) {
            logger.error("Failed to close socket: {}", address)
        }
        logger.info("Stopped listening to: {}", address)
    }

    private fun send(data: String) {
        writer.println(data)
    }

    @Throws(IOException::class)
    private fun receive(): String? {
        return reader.readLine()
    }

    @Throws(IOException::class)
    fun close() {
        socket.close()
    }

    fun setOnConnectionLost(action: () -> Unit) {
        scope.launch {
            status.collect {
                if (it < 0) action()
            }
        }
    }
}