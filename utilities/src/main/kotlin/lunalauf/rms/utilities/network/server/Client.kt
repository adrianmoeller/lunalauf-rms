package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.StateFlow

interface Client {
    /**
     * Connection status of this client.
     *
     * -1: disconnected<br></br>
     * 0: connected, not listening<br></br>
     * 1: connected, listening
     */
    val status: StateFlow<Int>
    val address: String
}