package lunalauf.rms.utilities.network.bot;

import lunalauf.rms.modelapi.ModelState;
import lunalauf.rms.utilities.network.bot.util.reply.ReplyContainer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public abstract class AbstractBot extends TelegramLongPollingBot {

    protected static final long SILENT_DURATION = 3000; // ms
    protected final String botToken;
    protected boolean silentStart;
    protected final long startTime;

    protected final ModelState modelState;
    protected ReplyContainer pendingReplies;

    public AbstractBot(String token, ModelState modelState, boolean silentStart) {
        super(token);
        this.botToken = token;
        this.modelState = modelState;
        this.pendingReplies = new ReplyContainer(this);

        this.silentStart = silentStart;
        this.startTime = System.currentTimeMillis();
    }

    // TODO instantly save connection data in LunaLauf object?
    public abstract void saveConnectionData();

    protected boolean beSilent() {
        if (!silentStart)
            return false;

        if (System.currentTimeMillis() < startTime + SILENT_DURATION)
            return true;

        silentStart = false;
        return false;
    }

    protected SendMessage send(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Long.toString(chatId));
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        return sendMessage;
    }

}
