package lunalauf.rms.utilities.network.bot;

import LunaLaufLanguage.Runner;
import lunalauf.rms.modelapi.ModelState;
import lunalauf.rms.modelapi.states.RunnersState;
import lunalauf.rms.utilities.network.bot.util.reply.CommandReply;
import lunalauf.rms.utilities.network.bot.util.reply.Reply;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class RunnerInfoBot extends AbstractBot {

    /* COMMANDS */
    private final String CMD_START = "/start";
    private final String CMD_SETCHIPID = "/setchipid";
    private final String CMD_STATS = "/stats";
    private final String CMD_ROUNDS = "/rounds";

    /* BOT ANSWERS */
    private final String AWR_START = "*Herzlich Willkommen beim Luna-Lauf!*🏃‍♀️🏃‍♂️🌙\n"
            + "Bitte gib deine Chip-ID ein (hinten auf den Chip gepresst):";
    private final String AWR_SETCHIPID = "Bitte gib deine Chip-ID ein:";
    private final String AWR_NAN = "Die Chip-Id muss eine Nummer sein!\n" //
            + "Bitte gib sie erneut ein:";
    private final String AWR_FAILURE = "Ein interner Fehler ist aufgetreten😵\n"
            + "Bitte versuche es erneut oder wende dich an das Orga-Team.";
    private final String AWR_NO_MODEL = "Unsere Software ist noch nicht bereit...\n" +
            "Versuche es später noch einmal oder wende dich an das Orga-Team.";
    private final String AWR_ID_SET = "deine Chip-ID wurde gespeichert!*🙌\n"
            + "Du kannst nun folgende Befehle nutzen:\n\n" //
            + "/stats - Runden- & Teamstatistik anzeigen\n" //
            + "/rounds - gelaufene Runden anzeigen\n" //
            + "/setchipid - die Chip-ID ändern";
    private final String AWR_NO_RUNNER = "Dieser Chip ist noch nicht bei uns registriert☹️ ...oder du hast dich vertippt😋\n"
            + "Bitte gib die Chip-ID erneut ein:";
    private final String AWR_ID_NOT_SET = "Wir benötigen zuerst deine Chip-ID.\n" //
            + "Bitte gib sie hier ein:";
    private final String AWR_ROUNDS = "Deine gelaufenen Runden: ";

    private Map<Long, Runner> chatId2runner;

    public RunnerInfoBot(String token, ModelState modelState, boolean silentStart, boolean loadData) {
        super(token, modelState, silentStart);
        chatId2runner = new HashMap<>();
    }

    @Override
    public void saveConnectionData() {
        // NO-OP
    }

    @Override
    public String getBotUsername() {
        return "lunalauf_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (beSilent())
            return;

        try {
            if (!update.hasMessage())
                return;

            Message msg = update.getMessage();
            if (msg.isCommand()) {
                handleCommand(msg);
            } else {
                handleCommandReply(msg);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCommand(Message msg) throws TelegramApiException {
        switch (msg.getText()) {
            case CMD_START:
                handleStartCommand(msg);
                break;
            case CMD_SETCHIPID:
                handleSetChipIdCommand(msg);
                break;
            case CMD_STATS:
                handleStatsCommand(msg);
                break;
            case CMD_ROUNDS:
                handleRoundsCommand(msg);
                break;
            default:
                break;
        }
    }

    private void handleStartCommand(Message msg) throws TelegramApiException {
        SendMessage sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_START);
        sMsg.enableMarkdown(true);
        execute(sMsg);
        pendingReplies.setCommandReply(msg.getChatId(), CMD_START);
    }

    private void handleSetChipIdCommand(Message msg) throws TelegramApiException {
        SendMessage sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_SETCHIPID);
        sMsg.enableMarkdown(true);
        execute(sMsg);
        pendingReplies.setCommandReply(msg.getChatId(), CMD_SETCHIPID);
    }

    private void handleStatsCommand(Message msg) {
        // TODO handle /stats command
    }

    private void handleRoundsCommand(Message msg) throws TelegramApiException {
        SendMessage sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_FAILURE);

        if (!chatId2runner.containsKey(msg.getChatId())) {
            sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_ID_NOT_SET);
        } else {
            int rounds;
            synchronized (modelState) {
                rounds = chatId2runner.get(msg.getChatId()).numOfRounds();
            }
            sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_ROUNDS + rounds);
        }

        sMsg.enableMarkdown(true);
        execute(sMsg);
    }

    private void handleCommandReply(Message msg) throws TelegramApiException {
        if (pendingReplies.hasReply(msg.getChatId())) {
            Reply reply = pendingReplies.getReply(msg.getChatId());
            if (reply instanceof CommandReply) {
                switch (((CommandReply) reply).getCommand()) {
                    case CMD_START:
                        handleStartReply(msg);
                        break;
                    case CMD_SETCHIPID:
                        handleSetChipIdReply(msg);
                        break;
                }
            } else {
                pendingReplies.removeReply(msg.getChatId());
                registerChipId(msg);
            }
        } else {
            registerChipId(msg);
        }
    }

    private void handleStartReply(Message msg) throws TelegramApiException {
        registerChipId(msg);
    }

    private void handleSetChipIdReply(Message msg) throws TelegramApiException {
        registerChipId(msg);
    }

    private void registerChipId(Message msg) throws TelegramApiException {
        SendChatAction typingAction = new SendChatAction();
        typingAction.setChatId(Long.toString(msg.getChatId()));
        typingAction.setAction(ActionType.TYPING);
        execute(typingAction);

        SendMessage sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_FAILURE);
        if (modelState instanceof ModelState.Loaded modelStateLoaded) {
            RunnersState runnersState = modelStateLoaded.getRunners().getValue();
            try {
                long chipId = Long.parseLong(msg.getText());

                Runner runner = runnersState.getRunner(chipId);
                if (runner != null) {
                    chatId2runner.put(msg.getChatId(), runner);
                    synchronized (modelState) {
                        String preAddress = "*Hallo " + runner.getName() + ", ";
                        sMsg = new SendMessage(Long.toString(msg.getChatId()), preAddress + AWR_ID_SET);
                    }
                } else {
                    sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_NO_RUNNER);
                }
            } catch (NumberFormatException e) {
                sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_NAN);
            } finally {
                pendingReplies.processedReply(msg.getChatId());
            }
        } else {
            sMsg = new SendMessage(Long.toString(msg.getChatId()), AWR_NO_MODEL);
        }

        sMsg.enableMarkdown(true);
        execute(sMsg);
    }

}
