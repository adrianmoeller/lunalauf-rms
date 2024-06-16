package lunalauf.rms.utilities.network.bot.util.reply

import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class CommandReply(
    chatId: Long,
    wrapper: ReplyContainer,
    val command: String
) : Reply(chatId, wrapper) {
    @Throws(TelegramApiException::class)
    override fun runReplacedAction() {
        // NO-OP
    }
}
