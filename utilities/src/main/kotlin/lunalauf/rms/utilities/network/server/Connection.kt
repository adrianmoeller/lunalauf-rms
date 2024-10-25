package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.flow.StateFlow

interface Connection {
    /**
     * Status of this connection.
     *
     * -1: disconnected<br></br>
     * 0: connected, not listening<br></br>
     * 1: connected, listening
     */
    val status: StateFlow<Int>
    val address: String
}