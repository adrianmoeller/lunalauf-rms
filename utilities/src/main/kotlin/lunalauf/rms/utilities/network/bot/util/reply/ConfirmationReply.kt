package lunalauf.rms.utilities.network.bot.util.reply

import lunalauf.rms.utilities.network.bot.util.TelegramRunnable
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class ConfirmationReply(
    chatId: Long,
    wrapper: ReplyContainer,
    message: Message,
    private val text: String
) : InlineReply(chatId, wrapper, message) {
    companion object {
        private const val CANCELLED = "\n\n❌ <b>Abgebrochen</b>"
        private const val CONFIRMED = "\n\n✔️ <b>Bestätigt</b>"
    }

    private var confirmAction: () -> Unit = {}
    private var cancelAction: () -> Unit = {}

    @Throws(TelegramApiException::class)
    override fun runReplacedAction() {
        changeMessageToCancelled()
    }

    fun setConfirmAction(confirmAction: () -> Unit) {
        this.confirmAction = confirmAction
    }

    @Throws(TelegramApiException::class)
    fun runConfirmAction() {
        confirmAction()
        changeMessageToConfirmed()
    }

    fun setCancelAction(cancelAction: () -> Unit) {
        this.cancelAction = cancelAction
    }

    @Throws(TelegramApiException::class)
    fun runCancelAction() {
        cancelAction()
        changeMessageToCancelled()
    }

    @Throws(TelegramApiException::class)
    private fun changeMessageToConfirmed() {
        val newMessage = EditMessageText().apply {
            chatId = chatId.toString()
            messageId = message.messageId
            text += CONFIRMED
            enableHtml(true)
        }
        wrapper.bot.execute(newMessage)
    }

    @Throws(TelegramApiException::class)
    private fun changeMessageToCancelled() {
        val newMessage = EditMessageText().apply {
            chatId = chatId.toString()
            messageId = message.messageId
            text += CANCELLED
            enableHtml(true)
        }
        wrapper.bot.execute(newMessage)
    }
}
