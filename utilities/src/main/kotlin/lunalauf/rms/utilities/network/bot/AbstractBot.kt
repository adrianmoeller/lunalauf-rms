package lunalauf.rms.utilities.network.bot

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lunalauf.rms.modelapi.ModelAPI
import lunalauf.rms.modelapi.ModelState
import lunalauf.rms.utilities.network.bot.util.reply.ReplyContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

abstract class AbstractBot(
    protected val token: String,
    protected val modelState: StateFlow<ModelState>,
    silentStart: Boolean
) : TelegramLongPollingBot(token) {
    companion object {
        protected const val SILENT_DURATION: Long = 3000 // ms
    }

    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val defaultScope = CoroutineScope(Dispatchers.IO)
    private val modelScope = CoroutineScope(ModelAPI.modelContext)

    private val startTime = System.currentTimeMillis()
    private var silentStart: Boolean

    protected var pendingReplies: ReplyContainer = ReplyContainer(this)

    init {
        this.silentStart = silentStart
    }

    private suspend fun catchAll(block: suspend CoroutineScope.() -> Unit, scope: CoroutineScope) {
        try {
            block(scope)
        } catch (e: Throwable) {
            logger.error("Exception occurred inside coroutine.", e)
        }
    }

    protected fun launchInModelScope(block: suspend CoroutineScope.() -> Unit) {
        modelScope.launch {
            catchAll(block, this)
        }
    }

    protected fun launchInDefaultScope(block: suspend CoroutineScope.() -> Unit) {
        defaultScope.launch {
            catchAll(block, this)
        }
    }

    // TODO instantly save connection data in model object?
    abstract suspend fun saveConnectionData()

    protected fun beSilent(): Boolean {
        if (!silentStart) return false
        if (System.currentTimeMillis() < startTime + SILENT_DURATION) return true
        silentStart = false
        return false
    }

    protected fun send(chatId: Long, text: String): SendMessage {
        val sendMessage = SendMessage()
        sendMessage.chatId = chatId.toString()
        sendMessage.text = text
        sendMessage.enableHtml(true)
        return sendMessage
    }
}
