package lunalauf.rms.utilities.network.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.bottokens.BotTokenContainer
import lunalauf.rms.utilities.persistence.PersistenceManager
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.BotSession
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

private val logger = LoggerFactory.getLogger(BotManager::class.java)

sealed class BotManager {
    companion object {
        private val persistenceManager = PersistenceManager()

        fun initialize(modelState: StateFlow<ModelState>): BotManager {
            return try {
                val botTokens = persistenceManager.load(BotTokenContainer::class.java)
                if (botTokens.roundCounter.isBlank() || botTokens.runnerInfo.isBlank()) {
                    InitializationError(
                        message = "No bot tokens provided.\nPlease edit '${botTokens.fileName}'-file"
                    )
                } else {
                    Available(
                        tokens = botTokens,
                        modelState = modelState
                    )
                }
            } catch (e: PersistenceManager.PersistenceException) {
                InitializationError(
                    message = e.message,
                    exception = e
                )
            }
        }
    }

    class InitializationError internal constructor(
        val message: String,
        val exception: Exception? = null
    ) : BotManager() {
        init {
            if (exception == null)
                logger.error(message)
            else
                logger.error(message, exception)
        }
    }

    class Available internal constructor(
        private val tokens: BotTokenContainer,
        private val modelState: StateFlow<ModelState>
    ) : BotManager() {
        private val scope = CoroutineScope(Dispatchers.IO)

        private val api: TelegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        private val botSessions: MutableList<BotSession> = mutableListOf()

        private val _silentStart = MutableStateFlow(true)
        val silentStart get() = _silentStart.asStateFlow()
        private val _loadConnections = MutableStateFlow(true)
        val loadConnections get() = _loadConnections.asStateFlow()

        // Runner Info
        private var runnerInfoBotSession: BotSession? = null
        private var runnerInfoBot: RunnerInfoBot? = null
        private val _runnerInfoBotState = MutableStateFlow(BotState.STOPPED)
        val runnerInfoBotState get() = _runnerInfoBotState.asStateFlow()

        // Round Counter
        private var roundCounterBotSession: BotSession? = null
        private var roundCounterBot: RoundCounterBot? = null
        private val _roundCounterBotState = MutableStateFlow(BotState.STOPPED)
        val roundCounterBotState get() = _roundCounterBotState.asStateFlow()

        fun silentStart(silentStart: Boolean) {
            _silentStart.value = silentStart
        }

        fun loadConnections(loadConnections: Boolean) {
            _loadConnections.value = loadConnections
        }

        @Throws(TelegramApiException::class)
        fun switchStateRunnerInfoBot() {
            _runnerInfoBotState.value = if (isRunnerInfoBotRunning)
                BotState.TERMINATING
            else
                BotState.INITIALIZING

            scope.launch {
                try {
                    if (runnerInfoBotSession == null) {
                        runnerInfoBot = RunnerInfoBot(
                            token = tokens.runnerInfo,
                            modelState = modelState,
                            silentStart = silentStart.value,
                            loadData = loadConnections.value
                        )
                        api.registerBot(runnerInfoBot)?.also {
                            runnerInfoBotSession = it
                            botSessions.add(it)
                        }

                        _runnerInfoBotState.value = BotState.RUNNING
                    } else {
                        runnerInfoBotSession?.run {
                            if (isRunning) {
                                stop()
                                _runnerInfoBotState.value = BotState.STOPPED
                            } else {
                                start()
                                _runnerInfoBotState.value = BotState.RUNNING
                            }
                        }
                    }
                    logger.info(
                        "'Runner-Info' bot state to: {}",
                        if (isRunnerInfoBotRunning) "running" else "stopped"
                    )
                } catch (e: Throwable) {
                    logger.error("Switching 'Runner-Info' bot failed due to exception", e)
                }
            }
        }

        private val isRunnerInfoBotRunning: Boolean
            get() = runnerInfoBotSession?.isRunning ?: false

        fun switchStateRoundCounterBot() {
            _roundCounterBotState.value = if (isRoundCounterBotRunning)
                BotState.TERMINATING
            else
                BotState.INITIALIZING

            scope.launch {
                try {
                    if (roundCounterBotSession == null) {
                        roundCounterBot = RoundCounterBot(
                            token = tokens.roundCounter,
                            modelState = modelState,
                            silentStart = silentStart.value,
                            loadData = loadConnections.value
                        )
                        api.registerBot(roundCounterBot).also {
                            roundCounterBotSession = it
                            botSessions.add(it)
                        }

                        _roundCounterBotState.value = BotState.RUNNING
                    } else {
                        roundCounterBotSession?.run {
                            if (isRunning) {
                                stop()
                                _roundCounterBotState.value = BotState.STOPPED
                            } else {
                                start()
                                _roundCounterBotState.value = BotState.RUNNING
                            }
                        }
                    }
                    logger.info(
                        "'Round-Counter' bot state to: {}",
                        if (isRoundCounterBotRunning) "running" else "stopped"
                    )
                } catch (e: Throwable) {
                    logger.error("Switching 'Round-Counter' bot failed due to exception", e)
                }
            }
        }

        private val isRoundCounterBotRunning: Boolean
            get() = roundCounterBotSession?.isRunning ?: false

        fun shutdown() {
            botSessions.forEach {
                if (it.isRunning) it.stop()
            }
        }
    }
}
