package lunalauf.rms.utilities.network.bot.util

import org.telegram.telegrambots.meta.exceptions.TelegramApiException

interface TelegramRunnable {
    @Throws(TelegramApiException::class)
    fun run()
}
