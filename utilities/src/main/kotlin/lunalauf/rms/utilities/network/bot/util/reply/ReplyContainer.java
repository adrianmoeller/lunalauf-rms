package lunalauf.rms.utilities.network.bot.util.reply;

import java.util.HashMap;
import java.util.Map;

import lunalauf.rms.utilities.network.bot.AbstractBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ReplyContainer {
	
	private final AbstractBot bot;
	
	private Map<Long, Reply> replies; 

	public ReplyContainer(AbstractBot bot) {
		this.bot = bot;
		this.replies = new HashMap<>();
	}

	public AbstractBot getBot() {
		return bot;
	}
	
	public boolean hasReply(long chatId) {
		return replies.containsKey(chatId);
	}

	public Reply getReply(long chatId) {
		return replies.get(chatId);
	}

	public void processedReply(long chatId) {
		replies.remove(chatId);
	}
	
	public void removeReply(long chatId) throws TelegramApiException {
		Reply oldReply = replies.remove(chatId);
		if(oldReply != null)
			oldReply.runReplacedAction();
	}

	public void setReply(long chatId, Reply reply) throws TelegramApiException {
		Reply oldReply = getReply(chatId);
		if(oldReply != null)
			oldReply.runReplacedAction();
		replies.put(chatId, reply);
	}
	
	public CommandReply setCommandReply(long chatId, String command) throws TelegramApiException {
		CommandReply reply = new CommandReply(chatId, this, command);
		setReply(chatId, reply);
		return reply;
	}
	
	public ConfirmationReply setConfirmationReply(long chatId, Message message, String text) throws TelegramApiException {
		ConfirmationReply reply = new ConfirmationReply(chatId, this, message, text);
		setReply(chatId, reply);
		return reply;
	}
	
	public CountReply setCountReply(long chatId, Message message) throws TelegramApiException {
		CountReply reply = new CountReply(chatId, this, message);
		setReply(chatId, reply);
		return reply;
	}

}
