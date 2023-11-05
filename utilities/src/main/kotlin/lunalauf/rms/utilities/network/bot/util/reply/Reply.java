package lunalauf.rms.utilities.network.bot.util.reply;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public abstract class Reply {
	
	protected final long chatId;
	protected final ReplyContainer wrapper;

	public Reply(long chatId, ReplyContainer wrapper) {
		this.chatId = chatId;
		this.wrapper = wrapper;
	}
	
	abstract public void runReplacedAction() throws TelegramApiException;

}
