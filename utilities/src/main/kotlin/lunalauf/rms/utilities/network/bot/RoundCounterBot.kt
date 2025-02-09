package lunalauf.rms.utilities.network.bot

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import lunalauf.rms.model.api.LogRoundResult
import lunalauf.rms.model.api.ModelManager
import lunalauf.rms.model.api.ModelState
import lunalauf.rms.model.internal.Runner
import lunalauf.rms.model.internal.Team
import lunalauf.rms.utilities.network.bot.util.ImageReceiveValidator
import lunalauf.rms.utilities.network.bot.util.ImageViewData
import lunalauf.rms.utilities.network.bot.util.reply.*
import org.apache.commons.io.FileUtils
import org.telegram.telegrambots.meta.api.methods.ActionType
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.*
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.IOException
import java.net.URI
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class RoundCounterBot(
    token: String,
    modelManager: ModelManager.Available,
    silentStart: Boolean,
    loadData: Boolean
) : AbstractBot(token, modelManager, silentStart) {
    companion object {    /* COMMANDS */
        private const val CMD_START = "/start"
        private const val CMD_COUNT = "/count"

        /* BOT ANSWERS */
        private val AWR_START = """
            <b>Herzlich Willkommen beim Luna-Lauf ${Calendar.getInstance().get(Calendar.YEAR)}!</b>🏃‍♀️🏃‍♂️🌙
            Bitte gib den Registrierungscode ein, den du per E-Mail zugeschickt bekommen hast:
            """.trimIndent()
        private val AWR_FAILURE = """
            Ein interner Fehler ist aufgetreten😵
            Bitte versuche es erneut oder wende dich an das Orga-Team.
            """.trimIndent()
        private val AWR_NO_MODEL = """
            Unsere Software ist noch nicht bereit...
            Versuche es später noch einmal oder wende dich an das Orga-Team.
            """.trimIndent()
        private const val AWR_INVALID = "Ungültige Eingabe!"
        private val AWR_NAN = """
            Der Registrierungscode muss eine Nummer sein!
            Bitte gib ihn erneut ein:
            """.trimIndent()
        private val AWR_NO_RUNNER = """
            Diesen Registrierungscode gibt es nicht☹️ ...oder du hast dich vielleicht vertippt😋
            Bitte gib ihn erneut ein:
             """.trimIndent()
        private val VAR_HOWTO_T = """
            So zählst du die Runden für dein Team:
            Drücke nach jeder von einem Teammitglied gelaufenen Runde den Button "Runde zählen" oder gib den Befehl $CMD_COUNT ein.
            """.trimIndent()
        private val VAR_HOWTO_R = """
            So zählst du deine Runden:
            Drücke nach jeder gelaufenen Runde den Button "Runde zählen" oder gib den Befehl $CMD_COUNT ein.
            """.trimIndent()
        private val AWR_REGISTERED_T = "<b>Die Registrierung war erfolgreich!</b>🙌\n\n$VAR_HOWTO_T"
        private val AWR_REGISTERED_R = "<b>Die Registrierung war erfolgreich!</b>🙌\n\n$VAR_HOWTO_R"
        private val AWR_ALREADY_REG_T =
            "<b>Dieses Gerät ist bereits mit dem Registierungscode eingeloggt.</b>\n\n$VAR_HOWTO_T"
        private val AWR_ALREADY_REG_R =
            "<b>Dieses Gerät ist bereits mit dem Registierungscode eingeloggt.</b>\n\n$VAR_HOWTO_R"
        private val AWR_OTHER_REG = """
            <b>Dieses Gerät ist bereits mit einem anderen Registrierungscode eingeloggt.</b>
            Möchstest du fortfahren und den Registrierungscode überschreiben?
            """.trimIndent()
        private val AWR_OTHER_DEV = """
            <b>Dieser Registrierungscode ist bereits auf einem anderen Gerät eingeloggt.</b>
            Möchstest du fortfahren und den Registrierungscode stattdessen auf diesem Gerät einsetzen?
            """.trimIndent()
        private val AWR_REMOVED = """
            <b>Ein anderes Gerät hat sich gerade mit dem aktuellen Registrierungscode eingeloggt!</b>
            Dieses Gerät wurde deshalb getrennt.
            """.trimIndent()
        private val AWR_NO_REG = """
            <b>Dieses Gerät ist noch nicht registriert!</b>
            Gib bitte zuerst den Registrierungscode ein:
            """.trimIndent()
        private const val AWR_ONLY_TEAM_PHOTO = "Nur Laufteams können Fotos senden."
        private const val AWR_NO_PHOTOS = "Im Moment werden von unserem Bot keine Fotos empfangen."
        private val AWR_ALREADY_SENT = """
            <b>Du kannst kein weiteres Foto für den aktuellen Funfactor senden!</b>
            Falls es bei dem vorherigen Senden Probleme gab, wende dich bitte an das Orga-Team.
            """.trimIndent()
        private const val AWR_PHOTO_RECEIVED = "Wir haben euer Foto empfangen😊"
        private val AWR_TOOFAST = """
            <b>War das nicht ein bisschen schnell...?</b>🤔
            Schummeln gilt nicht!🤭
            """.trimIndent()
        private const val AWR_NOT_IN_TIME = "<b>Runden können nur innerhalb der Laufzeit gezählt werden!</b>"
        private const val LAST_ROUND_LOGGED = "<b>Deine/Eure letzte Runde wurde bereits gezählt.</b>"
    }

    /* KEYBOARDS */
    private val confirmationKeyboard: InlineKeyboardMarkup = initConfirmationKeyboard()
    private val countKeyboard: InlineKeyboardMarkup = initCountKeyboard()
    private val countReplyKeyboard: ReplyKeyboardMarkup = initCountReplyKeyboard()

    private val chatId2runner: MutableMap<Long, Runner> = HashMap()
    private val runner2chatId: MutableMap<Runner, Long> = HashMap()

    var imageReceiveValidator = AtomicReference<ImageReceiveValidator?>()
    private val _image = Channel<ImageViewData>()
    val image get() = _image.receiveAsFlow()

    init {
        if (loadData) loadConnectionData()
    }

    private fun initConfirmationKeyboard(): InlineKeyboardMarkup {
        val keyboardMarkup = InlineKeyboardMarkup()
        val yesButton = InlineKeyboardButton("Ja").apply { callbackData = "yes" }
        val cancelButton = InlineKeyboardButton("Abbrechen").apply { callbackData = "no" }
        keyboardMarkup.keyboard = listOf(
            listOf(yesButton, cancelButton)
        )
        return keyboardMarkup
    }

    private fun initCountKeyboard(): InlineKeyboardMarkup {
        val keyboardMarkup = InlineKeyboardMarkup()
        val countRoundButton = InlineKeyboardButton("Runde zählen").apply { callbackData = CMD_COUNT }
        keyboardMarkup.keyboard = listOf(
            listOf(countRoundButton)
        )
        return keyboardMarkup
    }

    private fun initCountReplyKeyboard(): ReplyKeyboardMarkup {
        val keyboardMarkup = ReplyKeyboardMarkup()
        val row = KeyboardRow().apply { add(CMD_COUNT) }
        keyboardMarkup.keyboard = listOf(row)
        return keyboardMarkup
    }

    private fun loadConnectionData() {
        val constModelState = modelManager.model.value
        if (constModelState is ModelState.Loaded) {
            runBlocking(ModelState.modelContext) {
                constModelState.event.connections.value
            }.forEach {
                registerChatId(it.chatId.value, it.runner.value)
            }
        } else {
            logger.error("Cannot load connection data since model is not available")
        }
    }

    override fun saveConnectionData() {
        val constModelState = modelManager.model.value
        if (constModelState is ModelState.Loaded) {
            val connectionId2Runner = getChatId2Runner()
            runBlocking {
                constModelState.event.storeConnections(connectionId2Runner)
            }
        } else {
            logger.error("Cannot save connection data since model is not available")
        }
    }

    private fun getChatId2Runner(): Map<Long, Runner> {
        synchronized(this) {
            return chatId2runner.toMap()
        }
    }

    @Throws(TelegramApiException::class)
    fun sendGlobalMessage(message: String, onlyTeams: Boolean) {
        for ((chatId, runner) in getChatId2Runner()) {
            if (onlyTeams && runner.team.value == null)
                continue
            execute(send(chatId, message))
        }
    }

    override fun getBotUsername() = "lunalauf_counter_bot"

    override fun onUpdateReceived(update: Update) {
        if (beSilent()) return

        try {
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.callbackQuery)
                return
            }

            if (!update.hasMessage()) return

            val msg = update.message
            when {
                msg.isCommand -> handleCommand(msg.chatId, msg.text)
                msg.hasPhoto() -> handlePhoto(msg.chatId, msg.caption, msg.photo)
                else -> handleCommandReply(msg)
            }
        } catch (e: TelegramApiException) {
            logger.error("Exception occurred while processing received update", e)
        }
    }

    private fun registerChatId(chatId: Long, runner: Runner) {
        synchronized(this) {
            chatId2runner[chatId] = runner
            runner2chatId[runner] = chatId
        }
    }

    private fun unregisterChatId(runner: Runner, oldChatId: Long) {
        synchronized(this) {
            runner2chatId.remove(runner)
            chatId2runner.remove(oldChatId)
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleCommand(chatId: Long, text: String) {
        when (text) {
            CMD_START -> handleStartCommand(chatId)
            CMD_COUNT -> handleCountCommand(chatId)
            else -> {}
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleStartCommand(chatId: Long) {
        execute(send(chatId, AWR_START))
        pendingReplies.setCommandReply(chatId, CMD_START)
    }

    @Throws(TelegramApiException::class)
    private fun handleCountCommand(chatId: Long) {
        countRound(chatId)
    }

    @Throws(TelegramApiException::class)
    private fun handleCommandReply(msg: Message) {
        val reply = pendingReplies.getReply(msg.chatId)
        if (reply != null) {
            if (reply is CommandReply) {
                when (reply.command) {
                    CMD_START -> handleStartReply(msg)
                    else -> {}
                }
            } else {
                pendingReplies.removeReply(msg.chatId)
                register(msg)
            }
        } else {
            register(msg)
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleStartReply(msg: Message) {
        register(msg)
    }

    @Throws(TelegramApiException::class)
    private fun register(msg: Message) {
        val typingAction = SendChatAction().apply {
            chatId = msg.chatId.toString()
            setAction(ActionType.TYPING)
        }
        execute(typingAction)

        val sendMsg: SendMessage
        val constModelState = modelManager.model.value
        if (constModelState is ModelState.Loaded) {
            val event = constModelState.event
            sendMsg = try {
                val chipId = msg.text.toLong()
                val runner = runBlocking(ModelState.modelContext) {
                    event.getRunner(chipId)
                }
                if (runner != null) {
                    register(msg.chatId, runner)
                    return
                } else {
                    send(msg.chatId, AWR_NO_RUNNER)
                }
            } catch (e: NumberFormatException) {
                send(msg.chatId, AWR_NAN)
            } catch (e: IllegalStateException) {
                send(msg.chatId, AWR_FAILURE)
            }
        } else {
            sendMsg = send(msg.chatId, AWR_NO_MODEL)
        }

        execute(sendMsg)
    }

    @Throws(IllegalStateException::class, TelegramApiException::class)
    private fun register(chatId: Long, runner: Runner) {
        val registeredRunner = chatId2runner[chatId]
        val registeredChatId = runner2chatId[runner]
        val isChatIdRegistered = registeredRunner != null
        val isRunnerRegistered = registeredChatId != null
        val isTeam = runner.team.value != null

        if (!isChatIdRegistered && !isRunnerRegistered) {
            registerChatId(chatId, runner)
            val sMsg = send(chatId, if (isTeam) AWR_REGISTERED_T else AWR_REGISTERED_R)
            sMsg.replyMarkup = countKeyboard
            pendingReplies.setCountReply(chatId, execute(sMsg))
        } else if (isChatIdRegistered && isRunnerRegistered && registeredRunner == runner) {
            execute(send(chatId, if (isTeam) AWR_ALREADY_REG_T else AWR_ALREADY_REG_R))
        } else if (isRunnerRegistered) {
            val sMsg: SendMessage = send(chatId, AWR_OTHER_DEV)
            sMsg.replyMarkup = confirmationKeyboard
            val message = execute(sMsg)

            val reply = pendingReplies.setConfirmationReply(chatId, message, AWR_OTHER_DEV)
            reply.setConfirmAction {
                val oldChatId = registeredChatId!!
                unregisterChatId(runner, oldChatId)
                registerChatId(chatId, runner)

                execute(send(oldChatId, AWR_REMOVED))
                pendingReplies.removeReply(oldChatId)
                val sMsgReply = send(chatId, if (isTeam) AWR_REGISTERED_T else AWR_REGISTERED_R)
                sMsgReply.replyMarkup = countKeyboard
                pendingReplies.setCountReply(chatId, execute(sMsgReply))
            }
        } else if (isChatIdRegistered) {
            val sMsg: SendMessage = send(chatId, AWR_OTHER_REG)
            sMsg.replyMarkup = confirmationKeyboard
            val message = execute(sMsg)
            val reply = pendingReplies.setConfirmationReply(chatId, message, AWR_OTHER_REG)
            reply.setConfirmAction {
                runner2chatId.remove(registeredRunner)
                registerChatId(chatId, runner)
                val sMsgReply = send(chatId, if (isTeam) AWR_REGISTERED_T else AWR_REGISTERED_R)
                sMsgReply.replyMarkup = countKeyboard
                pendingReplies.setCountReply(chatId, execute(sMsgReply))
            }
        } else {
            execute(send(chatId, AWR_FAILURE))
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleCallbackQuery(query: CallbackQuery) {
        val chatId = query.message.chatId
        val data = query.data
        val reply = pendingReplies.getReply(chatId)
        if (reply != null) {
            handleReply(query, chatId, data, reply)
        } else {
            when (data) {
                CMD_COUNT -> countRound(chatId)
                else -> execute(send(chatId, AWR_INVALID))
            }
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleReply(
        query: CallbackQuery,
        chatId: Long,
        data: String,
        reply: Reply
    ) {
        if (reply is InlineReply) {
            if (reply.message.messageId == query.message.messageId) {
                handleInlineReply(reply, chatId, data)
            } else {
                when (data) {
                    CMD_COUNT -> countRound(chatId)
                    else -> {
                        execute(send(chatId, AWR_INVALID))
                        pendingReplies.removeReply(chatId)
                    }
                }
            }
        } else {
            execute(send(chatId, AWR_FAILURE))
            pendingReplies.removeReply(chatId)
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleInlineReply(
        inlineReply: InlineReply,
        chatId: Long,
        data: String
    ) {
        when (inlineReply) {
            is ConfirmationReply -> handleConfirmationReply(inlineReply, chatId, data)
            is CountReply -> handleCountReply(inlineReply, chatId, data)
            else -> {
                execute(send(chatId, AWR_FAILURE))
                pendingReplies.removeReply(chatId)
            }
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleConfirmationReply(
        confReply: ConfirmationReply,
        chatId: Long,
        data: String
    ) {
        when (data) {
            "yes" -> {
                pendingReplies.processedReply(chatId)
                confReply.runConfirmAction()
            }

            "no" -> {
                pendingReplies.processedReply(chatId)
                confReply.runCancelAction()
            }

            else -> execute(send(chatId, AWR_INVALID))
        }
    }

    @Throws(TelegramApiException::class)
    private fun handleCountReply(
        countReply: CountReply,
        chatId: Long,
        data: String
    ) {
        if (CMD_COUNT == data)
            countRound(chatId)
        else
            execute(send(chatId, AWR_INVALID))
    }

    @Throws(TelegramApiException::class)
    private fun countRound(chatId: Long) {
        pendingReplies.removeReply(chatId)
        val runner = chatId2runner[chatId]
        if (runner == null) {
            execute(send(chatId, AWR_NO_REG))
            return
        }

        val constModelState = modelManager.model.value
        val sMsg = if (constModelState is ModelState.Loaded) {
            val result = runBlocking(ModelState.modelContext) {
                runCatching { runner.logRound() }.getOrNull()
            }
            if (result == null) {
                send(chatId, AWR_FAILURE)
            } else {
                when (result) {
                    LogRoundResult.ValidationFailed -> send(chatId, AWR_TOOFAST)
                    LogRoundResult.RunDisabled -> send(chatId, AWR_NOT_IN_TIME)
                    is LogRoundResult.LastRoundAlreadyLogged -> send(chatId, LAST_ROUND_LOGGED)
                    is LogRoundResult.Logged -> {
                        val rounds = runner.numOfRounds.value
                        var text = "<b><i>Runde $rounds</i></b>"
                        if (rounds % 10 == 0) text += " 🎉 weiter so!"
                        send(chatId, text)
                    }
                }
            }
        } else {
            send(chatId, AWR_NO_MODEL)
        }

        sMsg.replyMarkup = countKeyboard
        val message = execute(sMsg)
        pendingReplies.setCountReply(chatId, message)
    }

    @Throws(TelegramApiException::class)
    private fun handlePhoto(chatId: Long, caption: String?, photoSizes: List<PhotoSize>) {
        val runner = chatId2runner[chatId]
        if (runner == null) {
            execute(send(chatId, AWR_NO_REG))
            return
        }
        val team = runner.team.value
        if (team == null) {
            execute(send(chatId, AWR_ONLY_TEAM_PHOTO))
            return
        }
        val validator = imageReceiveValidator.get()
        if (validator == null) {
            execute(send(chatId, AWR_NO_PHOTOS))
            return
        }
        var backdoor = false
        if (!validator.accept(team)) {
            if ("L" != caption) {
                execute(send(chatId, AWR_ALREADY_SENT))
                return
            }
            backdoor = true
        }

        val backdoorFinal = backdoor
        launchInDefaultScope {
            val bigPhotoSize: PhotoSize = photoSizes[photoSizes.size - 1]
            val getFile = GetFile()
            getFile.fileId = bigPhotoSize.fileId
            val file = execute(getFile)
            val fileURL = file.getFileUrl(token)
            val photoFilePath = calcPhotoFilePath(team, file)
            val systemFile = java.io.File(photoFilePath)
            try {
                FileUtils.copyURLToFile(URI(fileURL).toURL(), systemFile, 5000, 30000)
            } catch (e: IOException) {
                this@RoundCounterBot.execute(send(chatId, AWR_FAILURE))
                logger.error("Failed to load photo from Telegram server onto disk", e)
            }
            if (!backdoorFinal) {
                val image = java.io.File("file:///$photoFilePath")
                _image.send(ImageViewData(team, image))
            }
            this@RoundCounterBot.execute(send(chatId, AWR_PHOTO_RECEIVED))
        }
    }

    private fun calcPhotoFilePath(team: Team, file: File): String {
        val path = Paths.get("").toAbsolutePath().toString()
        val extension = file.filePath.substring(file.filePath.lastIndexOf("."))
        return path + "/team_photos/" + team.name.value + extension
    }
}
