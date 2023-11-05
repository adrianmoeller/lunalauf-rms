package lunalauf.rms.utilities.network.bot.util.reply

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class CountReply(
    chatId: Long,
    wrapper: ReplyContainer,
    message: Message
) : InlineReply(chatId, wrapper, message) {
    @Throws(TelegramApiException::class)
    override fun runReplacedAction() {
        val newMarkup = EditMessageReplyMarkup()
        newMarkup.chatId = chatId.toString()
        newMarkup.messageId = message.messageId
        wrapper.bot.execute(newMarkup)
    }
}
