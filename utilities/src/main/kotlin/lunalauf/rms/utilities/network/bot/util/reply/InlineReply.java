package lunalauf.rms.utilities.network.bot.util.reply;

import org.telegram.telegrambots.meta.api.objects.Message;

public abstract class InlineReply extends Reply {
	
	protected final Message message;

	public InlineReply(long chatId, ReplyContainer wrapper, Message message) {
		super(chatId, wrapper);
		this.message = message;
	}
	
	public Message getMessage() {
		return message;
	}

}
