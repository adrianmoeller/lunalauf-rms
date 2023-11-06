package lunalauf.rms.utilities.network.communication.message.type

enum class ResponseType {
    ERROR,
    ROUND_COUNT_ACCEPTED,
    ROUND_COUNT_REJECTED,
    RUNNER_INFO,
    TEAMRUNNER_INFO,
    MINIGAME_RECORD_DONE,
    MINIGAME_RECORD_FAILED
}
