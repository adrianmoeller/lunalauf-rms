package lunalauf.rms.utilities.network.bot.util.reply;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CountReply extends InlineReply {

	public CountReply(long chatId, ReplyContainer wrapper, Message message) {
		super(chatId, wrapper, message);
	}

	@Override
	public void runReplacedAction() throws TelegramApiException {
		EditMessageReplyMarkup newMarkup = new EditMessageReplyMarkup();
		newMarkup.setChatId(Long.toString(this.chatId));
		newMarkup.setMessageId(message.getMessageId());
		wrapper.getBot().execute(newMarkup);
	}

}
