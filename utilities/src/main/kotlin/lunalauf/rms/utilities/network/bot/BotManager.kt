package lunalauf.rms.utilities.network.bot

import lunalauf.rms.modelapi.ModelState
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.BotSession
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

class BotManager(
    private val tokens: Array<String>,
    private val modelState: ModelState
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val api: TelegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
    private val botSessions: MutableList<BotSession> = mutableListOf()

    // Runner Info
    private var runnerInfoBotSession: BotSession? = null
    var runnerInfoBot: RunnerInfoBot? = null
        private set

    // Round Counter
    private var roundCounterBotSession: BotSession? = null
    var roundCounterBot: RoundCounterBot? = null
        private set

    fun terminate() {
        botSessions.forEach {
            if (it.isRunning) it.stop()
        }
    }

    @Throws(TelegramApiException::class)
    fun switchStateRunnerInfoBot(silentStart: Boolean, loadData: Boolean) {
        if (runnerInfoBotSession == null) {
            runnerInfoBot = RunnerInfoBot(tokens[0], modelState, silentStart, loadData)
            api.registerBot(runnerInfoBot)?.also {
                runnerInfoBotSession = it
                botSessions.add(it)
            }
        } else {
            runnerInfoBotSession?.run {
                if (isRunning) stop() else start()
            }
        }
        logger.info("Switched 'runner-info' bot running state to: {}", isRunnerInfoBotRunning)
    }

    val isRunnerInfoBotRunning: Boolean
        get() = runnerInfoBotSession?.isRunning ?: false

    @Throws(TelegramApiException::class)
    fun switchStateRoundCounterBot(silentStart: Boolean, loadData: Boolean) {
        if (roundCounterBotSession == null) {
            roundCounterBot = RoundCounterBot(tokens[1], modelState, silentStart, loadData)
            api.registerBot(roundCounterBot).also {
                roundCounterBotSession = it
                botSessions.add(it)
            }
        } else {
            roundCounterBotSession?.run {
                if (isRunning) stop() else start()
            }
        }
        logger.info("Switched 'round-counter' bot running state to: {}", isRoundCounterBotRunning)
    }

    val isRoundCounterBotRunning: Boolean
        get() = roundCounterBotSession?.isRunning ?: false
}
