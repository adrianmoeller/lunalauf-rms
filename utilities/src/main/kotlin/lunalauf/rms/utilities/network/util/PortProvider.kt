package lunalauf.rms.utilities.network.util

object PortProvider {
    private val preferredPorts: List<Int> = (50916..50930).toList()

    fun getPreferredPorts(): List<Int> {
        return preferredPorts
    }
}
