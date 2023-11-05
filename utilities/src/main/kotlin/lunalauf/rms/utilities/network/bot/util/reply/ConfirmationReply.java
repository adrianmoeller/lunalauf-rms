package lunalauf.rms.utilities.network.bot.util.reply;

import lunalauf.rms.utilities.network.bot.util.TelegramRunnable;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ConfirmationReply extends InlineReply {

	private final String CANCELLED = "\n\n❌ <b>Abgebrochen</b>";
	private final String CONFIRMED = "\n\n✔️ <b>Bestätigt</b>";

	private final String text;	
	private TelegramRunnable confirmAction;
	private TelegramRunnable cancelAction;

	public ConfirmationReply(long chatId, ReplyContainer wrapper, Message message, String text) {
		super(chatId, wrapper, message);
		this.text = text;
		this.confirmAction = () -> {};
		this.cancelAction = () -> {};
	}

	@Override
	public void runReplacedAction() throws TelegramApiException {
		changeMessageToCancelled();
	}

	public void setConfirmAction(TelegramRunnable confirmAction) {
		this.confirmAction = confirmAction;
	}

	public void runConfirmAction() throws TelegramApiException {
		confirmAction.run();
		changeMessageToConfirmed();
	}

	public void setCancelAction(TelegramRunnable cancelAction) {
		this.cancelAction = cancelAction;
	}

	public void runCancelAction() throws TelegramApiException {
		cancelAction.run();
		changeMessageToCancelled();
	}

	private void changeMessageToConfirmed() throws TelegramApiException {
		EditMessageText newMessage = new EditMessageText();
		newMessage.setChatId(Long.toString(this.chatId));
		newMessage.setMessageId(message.getMessageId());
		newMessage.setText(text + CONFIRMED);
		newMessage.enableHtml(true);
		wrapper.getBot().execute(newMessage);
	}

	private void changeMessageToCancelled() throws TelegramApiException {
		EditMessageText newMessage = new EditMessageText();
		newMessage.setChatId(Long.toString(this.chatId));
		newMessage.setMessageId(message.getMessageId());
		newMessage.setText(text + CANCELLED);
		newMessage.enableHtml(true);
		wrapper.getBot().execute(newMessage);
	}

}
