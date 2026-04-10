package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import lunalauf.rms.model.api.LogMinigameResultResult
import lunalauf.rms.model.api.LogRoundResult
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.request.MinigameRecordRequest
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.request.RoundCountRequest
import lunalauf.rms.utilities.network.communication.message.request.RunnerInfoRequest
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import org.slf4j.LoggerFactory

class ModelUpdater(
    modelState: ModelState.Loaded,
    private val request: Request
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val event = modelState.event
    private var response: Response? = null

    private suspend fun <T> withModelContext(action: suspend CoroutineScope.() -> T): T? {
        return withContext(ModelState.modelContext) {
            try {
                action()
            } catch (e: Throwable) {
                logger.error("Exception occurred inside coroutine.", e)
                null
            }
        }
    }

    suspend fun run(): Response {
        handleRequest(request)
        return response ?: ResponseFactory.createErrorResponse(request.messageId, ErrorType.BAD_SERVER_STATE)
    }

    private suspend fun handleRequest(request: Request) {
        when (request) {
            is RoundCountRequest -> handleRoundCountRequest(request)
            is MinigameRecordRequest -> handleMinigameRecordRequest(request)
            is RunnerInfoRequest -> handleRunnerInfoRequest(request)
            else -> {
                response = ResponseFactory.createErrorResponse(
                    requestId = request.messageId,
                    error = ErrorType.UNEXPECTED_CLIENT_MESSAGE
                )
                logger.error("Incoming request type is unsupported: {}", request)
            }
        }
        logger.info("Handled: {}", request)
    }

    private suspend fun handleRoundCountRequest(request: RoundCountRequest) {
        withModelContext {
            val runner = event.getRunner(request.runnerId)
            if (runner == null) {
                response = ResponseFactory.createErrorResponse(
                    requestId = request.messageId,
                    error = ErrorType.UNKNOWN_ID
                )

                logger.warn("Request data and model data are inconsistent: {}", request.runnerId)
                return@withModelContext
            }

            response = when (runner.logRound()) {
                LogRoundResult.RunDisabled -> ResponseFactory.createRoundCountRejectedResponse(
                    requestId = request.messageId, runner.name.value,
                    causeMessage = "Runden können nur innerhalb der Laufzeit gezählt werden!"
                )

                LogRoundResult.ValidationFailed -> ResponseFactory.createRoundCountRejectedResponse(
                    requestId = request.messageId, runner.name.value,
                    causeMessage = "Lichtgeschwindigkeit nicht erlaubt!"
                )

                LogRoundResult.LastRoundAlreadyLogged -> ResponseFactory.createRoundCountRejectedResponse(
                    requestId = request.messageId, runner.name.value,
                    causeMessage = "Deine/Eure letzte Runde wurde bereits gezählt."
                )

                is LogRoundResult.Logged -> {
                    val name: String
                    val rounds: Int
                    val team = runner.team.value

                    if (team != null) {
                        name = team.name.value
                        rounds = team.numOfRounds.value
                    } else {
                        name = if (runner.name.value.isBlank()) runner.chipId.toString() else runner.name.value
                        rounds = runner.numOfRounds.value
                    }

                    ResponseFactory.createRoundCountAcceptedResponse(
                        requestId = request.messageId,
                        name,
                        rounds
                    )
                }
            }
        }
    }

    private suspend fun handleMinigameRecordRequest(request: MinigameRecordRequest) {
        withModelContext {
            val runner = event.getRunner(request.runnerId)
            if (runner == null) {
                response = ResponseFactory.createMinigameRecordFailedResponse(
                    requestId = request.messageId,
                    causeMessage = "Chip nicht registriert"
                )
                logger.warn("Request data and model data are inconsistent: {}", request.runnerId)
                return@withModelContext
            }

            val team = runner.team.value
            if (team == null) {
                response = ResponseFactory.createMinigameRecordFailedResponse(
                    requestId = request.messageId,
                    causeMessage = "Kein Mitglied eines Teams"
                )
                logger.warn("Represents no Team to attach Minigame to: {}", runner)
                return@withModelContext
            }

            response = when (team.logMinigameResult(request.minigameId, request.points)) {
                LogMinigameResultResult.NoMinigameWithId -> ResponseFactory.createMinigameRecordFailedResponse(
                    requestId = request.messageId,
                    causeMessage = "Ergebnis konnte nicht eingetragen werden!"
                )

                is LogMinigameResultResult.Logged -> ResponseFactory.createMinigameRecordDoneResponse(
                    requestId = request.messageId
                )
            }
        }
    }

    private suspend fun handleRunnerInfoRequest(request: RunnerInfoRequest) {
        withModelContext {
            val runner = event.getRunner(request.runnerId)
            if (runner == null) {
                response = ResponseFactory.createErrorResponse(
                    requestId = request.messageId,
                    error = ErrorType.UNKNOWN_ID
                )
                logger.warn("Request data and model data are inconsistent: {}", request.runnerId)
                return@withModelContext
            }

            val team = runner.team.value
            response = if (team == null) {
                ResponseFactory.createRunnerInfoResponse(
                    requestId = request.messageId,
                    runnerName = runner.name.value.ifBlank { "-" },
                    runnerId = request.runnerId,
                    numRunnerRounds = runner.numOfRounds.value
                )
            } else {
                ResponseFactory.createTeamRunnerInfoResponse(
                    requestId = request.messageId,
                    runnerName = runner.name.value.ifBlank { "-" },
                    runnerId = request.runnerId,
                    numRunnerRounds = runner.numOfRounds.value,
                    teamName = team.name.value.ifBlank { "-" },
                    numTeamRounds = team.numOfRounds.value,
                    teamFunfactorPoints = team.numOfFunfactorPoints.value
                )
            }
        }
    }
}
