package lunalauf.rms.utilities.network.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import lunalauf.rms.modelapi.LogMinigameResultResult
import lunalauf.rms.modelapi.LogRoundResult
import lunalauf.rms.modelapi.ModelAPI
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.request.MinigameRecordRequest
import lunalauf.rms.utilities.network.communication.message.request.Request
import lunalauf.rms.utilities.network.communication.message.request.RoundCountRequest
import lunalauf.rms.utilities.network.communication.message.request.RunnerInfoRequest
import lunalauf.rms.utilities.network.communication.message.response.Response
import lunalauf.rms.utilities.network.communication.message.response.ResponseFactory
import org.slf4j.LoggerFactory

class ModelUpdater(
    private val modelState: ModelState.Loaded,
    private val request: Request,
    private val responseFactory: ResponseFactory
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val modelAPI = modelState.modelAPI
    private var response: Response? = null

    private suspend fun <T> withModelContext(action: suspend CoroutineScope.() -> T): T? {
        return withContext(ModelAPI.modelContext) {
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
        return response ?: responseFactory.createErrorResponse(request.messageId, ErrorType.BAD_SERVER_STATE)
    }

    private suspend fun handleRequest(request: Request) {
        when (request) {
            is RoundCountRequest -> handleRoundCountRequest(request)
            is MinigameRecordRequest -> handleMinigameRecordRequest(request)
            is RunnerInfoRequest -> handleRunnerInfoRequest(request)
            else -> {
                response = responseFactory.createErrorResponse(
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
            val runner = modelState.runners.value.getRunner(request.runnerId)
            if (runner == null) {
                response = responseFactory.createErrorResponse(
                    requestId = request.messageId,
                    error = ErrorType.UNKNOWN_ID
                )
                logger.warn("Request data and model data are inconsistent: {}", request.runnerId)
                return@withModelContext
            }

            response = when (modelAPI.logRound(runner)) {
                LogRoundResult.RunDisabled -> responseFactory.createRoundCountRejectedResponse(
                    requestId = request.messageId, runner.name,
                    causeMessage = "Runden können nur innerhalb der Laufzeit gezählt werden!"
                )

                LogRoundResult.ValidationFailed -> responseFactory.createRoundCountRejectedResponse(
                    requestId = request.messageId, runner.name,
                    causeMessage = "Lichtgeschwindigkeit nicht erlaubt!"
                )

                LogRoundResult.LastRoundAlreadyLogged -> responseFactory.createRoundCountRejectedResponse(
                    requestId = request.messageId, runner.name,
                    causeMessage = "Deine/Eure letzte Runde wurde bereits gezählt."
                )

                is LogRoundResult.Logged -> {
                    val name: String
                    val rounds: Int
                    val team = runner.team
                    if (team != null) {
                        name = team.name
                        rounds = modelAPI.numOfRounds(team)
                    } else {
                        name = if (runner.name.isNullOrBlank()) runner.id.toString() else runner.name
                        rounds = modelAPI.numOfRounds(runner)
                    }
                    responseFactory.createRoundCountAcceptedResponse(
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
            val runner = modelState.runners.value.getRunner(request.runnerId)
            if (runner == null) {
                response = responseFactory.createMinigameRecordFailedResponse(
                    requestId = request.messageId,
                    causeMessage = "Chip nicht registriert"
                )
                logger.warn("Request data and model data are inconsistent: {}", request.runnerId)
                return@withModelContext
            }

            val team = runner.team
            if (team == null) {
                response = responseFactory.createMinigameRecordFailedResponse(
                    requestId = request.messageId,
                    causeMessage = "Kein Mitglied eines Teams"
                )
                logger.warn("Represents no Team to attach Minigame to: {}", runner)
                return@withModelContext
            }

            response = when (modelAPI.logMinigameResult(team, request.minigameId, request.points)) {
                LogMinigameResultResult.NoMinigameWithId -> responseFactory.createMinigameRecordFailedResponse(
                    requestId = request.messageId,
                    causeMessage = "Ergebnis konnte nicht eingetragen werden!"
                )

                is LogMinigameResultResult.Logged -> responseFactory.createMinigameRecordDoneResponse(
                    requestId = request.messageId
                )
            }
        }
    }

    private suspend fun handleRunnerInfoRequest(request: RunnerInfoRequest) {
        withModelContext {
            val runner = modelState.runners.value.getRunner(request.runnerId)
            if (runner == null) {
                response = responseFactory.createErrorResponse(
                    requestId = request.messageId,
                    error = ErrorType.UNKNOWN_ID
                )
                logger.warn("Request data and model data are inconsistent: {}", request.runnerId)
                return@withModelContext
            }

            val team = runner.team
            response = if (team == null) {
                responseFactory.createRunnerInfoResponse(
                    requestId = request.messageId,
                    runnerName = runner.name ?: "-",
                    runnerId = request.runnerId,
                    numRunnerRounds = modelAPI.numOfRounds(runner)
                )
            } else {
                responseFactory.createTeamRunnerInfoResponse(
                    requestId = request.messageId,
                    runnerName = runner.name ?: "-",
                    runnerId = request.runnerId,
                    numRunnerRounds = modelAPI.numOfRounds(runner),
                    teamName = team.name ?: "-",
                    numTeamRounds = modelAPI.numOfRounds(team),
                    teamFunfactorPoints = modelAPI.numOfFunfactorPoints(team)
                )
            }
        }
    }
}
