package lunalauf.rms.utilities.network.communication

enum class ErrorType(
    val message: String
) {
    DISCONNECTED("Keine Verbindung zum Server!\nWende dich bitte an das Orga-Team."),
    RESPONSE_TIMEOUT("Server antwortet nicht!\nWende dich bitte an das Orga-Team."),
    CORRUPTED_SERVER_MESSAGE("Fehlerhafte Server-Nachricht!\nWende dich bitte an das Orga-Team."),
    CORRUPTED_CLIENT_MESSAGE("Fehlerhafte Client-Nachricht!\nWende dich bitte an das Orga-Team."),
    UNWANTED_TERMINATION("Sendeprozess abgestürzt!\nWende dich bitte an das Orga-Team."),
    UNEXPECTED_SERVER_MESSAGE("Unerwartete Server-Antwort!\nWende dich bitte an das Orga-Team."),
    UNEXPECTED_CLIENT_MESSAGE("Unerwartete Client-Nachricht!\nWende dich bitte an das Orga-Team."),
    BAD_SERVER_STATE("Fehlerhafter Server-Zustand!\nWende dich bitte an das Orga-Team."),
    UNKNOWN_ID("Chip nicht registriert!\nWende dich bitte an das Orga-Team."),
    UNKNOWN_ERROR("Unbekannter Fehler!\nWende dich bitte an das Orga-Team.")
}
