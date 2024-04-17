package lunalauf.rms.utilities.network.bot

import LunaLaufLanguage.Runner
import kotlinx.coroutines.flow.StateFlow
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.modelapi.states.RunnersState
import lunalauf.rms.utilities.network.bot.util.reply.CommandReply
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class RunnerInfoBot(
    token: String,
    modelState: StateFlow<ModelState>,
    silentStart: Boolean,
    loadData: Boolean
) : AbstractBot(token, modelState, silentStart) {
    companion object {
        /* COMMANDS */
        private const val CMD_START = "/start"
        private const val CMD_SETCHIPID = "/setchipid"
        private const val CMD_STATS = "/stats"
        private const val CMD_ROUNDS = "/rounds"

        /* BOT ANSWERS */
        private val AWR_START = """
            *Herzlich Willkommen beim Luna-Lauf!*🏃‍♀️🏃‍♂️🌙
            Bitte gib deine Chip-ID ein (hinten auf den Chip gepresst):
            """.trimIndent()
        private const val AWR_SETCHIPID = "Bitte gib deine Chip-ID ein:"
        private val AWR_NAN = """
            Die Chip-Id muss eine Nummer sein!
            Bitte gib sie erneut ein:
            """.trimIndent()
        private val AWR_FAILURE = """
            Ein interner Fehler ist aufgetreten😵
            Bitte versuche es erneut oder wende dich an das Orga-Team.
            """.trimIndent()
        private val AWR_NO_MODEL = """
            Unsere Software ist noch nicht bereit...
            Versuche es später noch einmal oder wende dich an das Orga-Team.
            """.trimIndent()
        private val AWR_ID_SET = """
            deine Chip-ID wurde gespeichert!*🙌
            Du kannst nun folgende Befehle nutzen:
            
            /stats - Runden- & Teamstatistik anzeigen
            /rounds - gelaufene Runden anzeigen
            /setchipid - die Chip-ID ändern
            """.trimIndent()
        private val AWR_NO_RUNNER = """
            Dieser Chip ist noch nicht bei uns registriert☹️ ...oder du hast dich vertippt😋
            Bitte gib die Chip-ID erneut ein:
            """.trimIndent()
        private val AWR_ID_NOT_SET = """
            Wir benötigen zuerst deine Chip-ID.
            Bitte gib sie hier ein:
            """.trimIndent()
        private const val AWR_ROUNDS = "Deine gelaufenen Runden: "
    }

    private val chatId2runner: MutableMap<Long, Runner> = HashMap()

    override fun saveConnectionData() {
        // NO-OP
    }

    override fun getBotUsername() = "lunalauf_bot"

    override fun onUpdateReceived(update: Update) {
        if (beSilent()) return
        try {
            if (!update.hasMessage()) return
            val msg = update.message
            if (msg.isCommand) {
                handleCommand(msg)
            } else {
                handleCommandReply(msg)
            }
        } catch (e: TelegramApiException) {
            logger.error("Exception occurred while processing received update", e)
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleCommand(msg: Message) {
        when (msg.text) {
            CMD_START -> handleStartCommand(msg)
            CMD_SETCHIPID -> handleSetChipIdCommand(msg)
            CMD_STATS -> handleStatsCommand(msg)
            CMD_ROUNDS -> handleRoundsCommand(msg)
            else -> {}
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleStartCommand(msg: Message) {
        val sMsg = SendMessage(msg.chatId.toString(), AWR_START)
        sMsg.enableMarkdown(true)
        execute(sMsg)
        pendingReplies.setCommandReply(msg.chatId, CMD_START)
    }

    @Throws(TelegramApiException::class)
    private fun handleSetChipIdCommand(msg: Message) {
        val sMsg = SendMessage(msg.chatId.toString(), AWR_SETCHIPID)
        sMsg.enableMarkdown(true)
        execute(sMsg)
        pendingReplies.setCommandReply(msg.chatId, CMD_SETCHIPID)
    }

    private fun handleStatsCommand(msg: Message) {
        // TODO handle /stats command
    }

    @Throws(TelegramApiException::class)
    private fun handleRoundsCommand(msg: Message) {
        val sMsg = if (!chatId2runner.containsKey(msg.chatId)) {
            SendMessage(msg.chatId.toString(), AWR_ID_NOT_SET)
        } else {
            var rounds: Int
            synchronized(modelState) { rounds = chatId2runner[msg.chatId]!!.numOfRounds() }
            SendMessage(msg.chatId.toString(), AWR_ROUNDS + rounds)
        }
        sMsg.enableMarkdown(true)
        execute(sMsg)
    }

    @Throws(TelegramApiException::class)
    private fun handleCommandReply(msg: Message) {
        if (pendingReplies.hasReply(msg.chatId)) {
            val reply = pendingReplies.getReply(msg.chatId)
            if (reply is CommandReply) {
                when (reply.command) {
                    CMD_START -> handleStartReply(msg)
                    CMD_SETCHIPID -> handleSetChipIdReply(msg)
                }
            } else {
                pendingReplies.removeReply(msg.chatId)
                registerChipId(msg)
            }
        } else {
            registerChipId(msg)
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleStartReply(msg: Message) {
        registerChipId(msg)
    }

    @Throws(TelegramApiException::class)
    private fun handleSetChipIdReply(msg: Message) {
        registerChipId(msg)
    }

    @Throws(TelegramApiException::class)
    private fun registerChipId(msg: Message) {
        val typingAction = SendChatAction()
        typingAction.chatId = msg.chatId.toString()
        typingAction.setAction(ActionType.TYPING)
        execute(typingAction)

        val constModelState = modelState.value
        val sMsg = if (constModelState is ModelState.Loaded) {
            val runnersState: RunnersState = constModelState.runners.value
            try {
                val chipId = msg.text.toLong()
                val runner = runnersState.getRunner(chipId)
                if (runner != null) {
                    chatId2runner[msg.chatId] = runner
                    val preAddress = "*Hallo " + runner.name + ", "
                    SendMessage(msg.chatId.toString(), preAddress + AWR_ID_SET)
                } else {
                    SendMessage(msg.chatId.toString(), AWR_NO_RUNNER)
                }
            } catch (e: NumberFormatException) {
                SendMessage(msg.chatId.toString(), AWR_NAN)
            } finally {
                pendingReplies.processedReply(msg.chatId)
            }
        } else {
            SendMessage(msg.chatId.toString(), AWR_NO_MODEL)
        }

        sMsg.enableMarkdown(true)
        execute(sMsg)
    }
}
