package lunalauf.rms.utilities.network.bot.util;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramRunnable {
	void run() throws TelegramApiException;
}
