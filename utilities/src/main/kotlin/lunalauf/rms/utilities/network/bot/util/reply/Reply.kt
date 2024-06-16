package lunalauf.rms.utilities.network.bot.util.reply

import org.telegram.telegrambots.meta.exceptions.TelegramApiException

abstract class Reply(
    protected val chatId: Long,
    protected val wrapper: ReplyContainer
) {
    @Throws(TelegramApiException::class)
    abstract fun runReplacedAction()
}
