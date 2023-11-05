package lunalauf.rms.utilities.network.bot.util.reply

import lunalauf.rms.utilities.network.bot.AbstractBot
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class ReplyContainer(val bot: AbstractBot) {
    private val replies: MutableMap<Long, Reply> = HashMap()

    fun hasReply(chatId: Long): Boolean {
        return replies.containsKey(chatId)
    }

    fun getReply(chatId: Long): Reply? {
        return replies[chatId]
    }

    fun processedReply(chatId: Long) {
        replies.remove(chatId)
    }

    @Throws(TelegramApiException::class)
    fun removeReply(chatId: Long) {
        val oldReply = replies.remove(chatId)
        oldReply?.runReplacedAction()
    }

    @Throws(TelegramApiException::class)
    fun setReply(chatId: Long, reply: Reply) {
        val oldReply = getReply(chatId)
        oldReply?.runReplacedAction()
        replies[chatId] = reply
    }

    @Throws(TelegramApiException::class)
    fun setCommandReply(chatId: Long, command: String): CommandReply {
        val reply = CommandReply(chatId, this, command)
        setReply(chatId, reply)
        return reply
    }

    @Throws(TelegramApiException::class)
    fun setConfirmationReply(chatId: Long, message: Message, text: String): ConfirmationReply {
        val reply = ConfirmationReply(chatId, this, message, text)
        setReply(chatId, reply)
        return reply
    }

    @Throws(TelegramApiException::class)
    fun setCountReply(chatId: Long, message: Message): CountReply {
        val reply = CountReply(chatId, this, message)
        setReply(chatId, reply)
        return reply
    }
}
