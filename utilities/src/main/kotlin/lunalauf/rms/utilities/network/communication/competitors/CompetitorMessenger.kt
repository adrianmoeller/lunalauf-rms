package lunalauf.rms.utilities.network.communication.competitors

import LunaLaufLanguage.Team

sealed interface CompetitorMessenger {
    data object Unavailable : CompetitorMessenger
    interface Available : CompetitorMessenger {
        fun sendToAll(message: String)
        fun sendToTeams(message: String)
        fun startReceiveImagesFromTeams(validator: (Team) -> Boolean)
        fun stopReceiveImagesFromTeams()
    }
}