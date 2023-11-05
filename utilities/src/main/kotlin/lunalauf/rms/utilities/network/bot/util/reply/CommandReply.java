package lunalauf.rms.utilities.network.bot.util.reply;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CommandReply extends Reply {
	
	protected String command;

	public CommandReply(long chatId, ReplyContainer wrapper, String command) {
		super(chatId, wrapper);
		this.command = command;
	}

	@Override
	public void runReplacedAction() throws TelegramApiException {
		// NO-OP
	}

	public String getCommand() {
		return command;
	}

}
