package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.util.ConnectionInitiationHelper
import org.slf4j.LoggerFactory
import java.net.SocketException

class ClientHandler(
    private val modelState: ModelState
) {
    companion object {
        private const val TIMEOUT_COMMUNICATION_TEST = 2000 // ms
    }

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val scope = CoroutineScope(Dispatchers.IO)
    private val _clients = MutableStateFlow<ArrayList<Client>>(arrayListOf())
    val clients get() = _clients.asStateFlow()

    fun handleClient(client: Client) {
        scope.launch {
            if (initialCommunicationTest(client)) {
                registerClient(client)
                client.startListening(modelState)
            } else {
                client.close()
            }
        }
    }

    private fun initialCommunicationTest(client: Client): Boolean {
        return try {
            client.setTimeout(TIMEOUT_COMMUNICATION_TEST)
            val synMessage = client.receive() ?: return false
            val ackMessage = ConnectionInitiationHelper.getAckMessage(synMessage)
            client.send(ackMessage)
            val synAckMessage = client.receive()
            val expectedSynAckMessage: String = ConnectionInitiationHelper.getAckMessage(ackMessage)
            val result = expectedSynAckMessage == synAckMessage
            logger.info("Communication test {}: {}", if (result) "successful" else "failed", client.remoteAddress)
            result
        } catch (e: Exception) {
            logger.warn("Communication test failed due to an exception: {}", client.remoteAddress, e)
            false
        } finally {
            try {
                client.resetTimeout()
            } catch (e: SocketException) {
                logger.error("Failed to reset socket timeout. Behaviour unpredictable: {}", client.remoteAddress, e)
            }
        }
    }

    private fun registerClient(client: Client) {
        var existing: Client? = null
        _clients.value.forEach {
            if (it.remoteAddress == client.remoteAddress)
                if (it.status.value == -1)
                    existing = it
        }
        _clients.update {
            it.apply {
                if (existing != null) set(it.indexOf(existing), client)
                else add(client)
            }
        }
        logger.info("Client registered: {}", client.remoteAddress)
    }

    fun onLostClients(listener: (List<Client>) -> Unit) {
        scope.launch {
            clients.collect { newClients ->
                val lostClients = newClients.filter { it.status.value < 0 }
                if (lostClients.isNotEmpty())
                    listener(lostClients)
            }
        }
    }
}
