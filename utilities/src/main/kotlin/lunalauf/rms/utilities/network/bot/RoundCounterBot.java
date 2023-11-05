package lunalauf.rms.utilities.network.bot;

import LunaLaufLanguage.Runner;
import javafx.application.Platform;
import javafx.concurrent.Task;
import lunalauf.rms.modelapi.ModelState;
import lunalauf.rms.modelapi.states.RunnersState;
import lunalauf.rms.utilities.network.bot.util.reply.*;
import lunalauf.rms.utilities.network.util.ImageViewer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoundCounterBot extends AbstractBot {
    private final Logger logger = LoggerFactory.getLogger(RoundCounterBot.class);

    /* COMMANDS */
    private final String CMD_START = "/start";
    private final String CMD_COUNT = "/count";

    /* BOT ANSWERS */
    private final String AWR_START = "<b>Herzlich Willkommen beim Luna-Lauf " + Calendar.getInstance().get(Calendar.YEAR) + "!</b>🏃‍♀️🏃‍♂️🌙\n" //
            + "Bitte gib den Registrierungscode ein, den du per E-Mail zugeschickt bekommen hast:";
    private final String AWR_FAILURE = "Ein interner Fehler ist aufgetreten😵\n" //
            + "Bitte versuche es erneut oder wende dich an das Orga-Team.";

    private final String AWR_NO_MODEL = "Unsere Software ist noch nicht bereit...\n" +
            "Versuche es später noch einmal oder wende dich an das Orga-Team.";
    private final String AWR_INVALID = "Ungültige Eingabe!";
    private final String AWR_NAN = "Der Registrierungscode muss eine Nummer sein!\n" //
            + "Bitte gib ihn erneut ein:";
    private final String AWR_NO_RUNNER = "Diesen Registrierungscode gibt es nicht☹️ ...oder du hast dich vielleicht vertippt😋\n" //
            + "Bitte gib ihn erneut ein:";
    private final String VAR_HOWTO_T = "So zählst du die Runden für dein Team:\n" //
            + "Drücke nach jeder von einem Teammitglied gelaufenen Runde den Button \"Runde zählen\" oder gib den Befehl " + CMD_COUNT
            + " ein.";
    private final String VAR_HOWTO_R = "So zählst du deine Runden:\n" //
            + "Drücke nach jeder gelaufenen Runde den Button \"Runde zählen\" oder gib den Befehl " + CMD_COUNT + " ein.";
    private final String AWR_REGISTERED_T = "<b>Die Registrierung war erfolgreich!</b>🙌\n\n" + VAR_HOWTO_T;
    private final String AWR_REGISTERED_R = "<b>Die Registrierung war erfolgreich!</b>🙌\n\n" + VAR_HOWTO_R;
    private final String AWR_ALREADY_REG_T = "<b>Dieses Gerät ist bereits mit dem Registierungscode eingeloggt.</b>\n\n" + VAR_HOWTO_T;
    private final String AWR_ALREADY_REG_R = "<b>Dieses Gerät ist bereits mit dem Registierungscode eingeloggt.</b>\n\n" + VAR_HOWTO_R;
    private final String AWR_OTHER_REG = "<b>Dieses Gerät ist bereits mit einem anderen Registrierungscode eingeloggt.</b>\n"
            + "Möchstest du fortfahren und den Registrierungscode überschreiben?";
    private final String AWR_OTHER_DEV = "<b>Dieser Registrierungscode ist bereits auf einem anderen Gerät eingeloggt.</b>\n"
            + "Möchstest du fortfahren und den Registrierungscode stattdessen auf diesem Gerät einsetzen?";
    private final String AWR_REMOVED = "<b>Ein anderes Gerät hat sich gerade mit dem aktuellen Registrierungscode eingeloggt!</b>\n"
            + "Dieses Gerät wurde deshalb getrennt.";
    private final String AWR_NO_REG = "<b>Dieses Gerät ist noch nicht registriert!</b>\n" + "Gib bitte zuerst den Registrierungscode ein:";
    private final String AWR_ONLY_TEAM_PHOTO = "Nur Laufteams können Fotos senden.";
    private final String AWR_NO_PHOTOS = "Im Moment werden von unserem Bot keine Fotos empfangen.";
    private final String AWR_ALREADY_SENT = "<b>Du kannst kein weiteres Foto für den aktuellen Funfactor senden!</b>\n"
            + "Falls es bei dem vorherigen Senden Probleme gab, wende dich bitte an das Orga-Team.";
    private final String AWR_PHOTO_RECEIVED = "Wir haben euer Foto empfangen😊";
    private final String AWR_TOOFAST = "<b>War das nicht ein bisschen schnell...?</b>🤔\n" + "Schummeln gilt nicht!🤭";
    private final String AWR_NOT_IN_TIME = "<b>Runden können nur innerhalb der Laufzeit gezählt werden!</b>";
    private final String LAST_ROUND_LOGGED = "<b>Deine/Eure letzte Runde wurde bereits gezählt.</b>";

    /* KEYBOARDS */
    private InlineKeyboardMarkup confirmationKeyboard;
    private InlineKeyboardMarkup countKeyboard;
    private ReplyKeyboardMarkup countReplyKeyboard;

    private Map<Long, Runner> chatId2runner;
    private Map<Runner, Long> runner2chatId;

    private ImageViewer imageViewer;

    private ExecutorService executor;

    public RoundCounterBot(String token, ModelState modelState, boolean silentStart, boolean loadData) {
        super(token, modelState, silentStart);
        chatId2runner = new HashMap<>();
        runner2chatId = new HashMap<>();
        imageViewer = null;
        executor = Executors.newCachedThreadPool();

        initConfirmationKeyboard();
        initCountKeyboard();
        initCountReplyKeyboard();

        if (loadData)
            loadConnectionData();
    }

    private void initConfirmationKeyboard() {
        confirmationKeyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton yesButton = new InlineKeyboardButton("Ja");
        yesButton.setCallbackData("yes");
        InlineKeyboardButton cancelButton = new InlineKeyboardButton("Abbrechen");
        cancelButton.setCallbackData("no");
        confirmationKeyboard.setKeyboard(Arrays.asList(Arrays.asList(yesButton, cancelButton)));
    }

    private void initCountKeyboard() {
        countKeyboard = new InlineKeyboardMarkup();
        InlineKeyboardButton countRoundButton = new InlineKeyboardButton("Runde zählen");
        countRoundButton.setCallbackData(CMD_COUNT);
        countKeyboard.setKeyboard(Arrays.asList(Arrays.asList(countRoundButton)));
    }

    private void initCountReplyKeyboard() {
        countReplyKeyboard = new ReplyKeyboardMarkup();
        KeyboardRow row = new KeyboardRow();
        row.add(CMD_COUNT);
        countReplyKeyboard.setKeyboard(Arrays.asList(row));
    }

    private void loadConnectionData() {
        Result<Void> re = new Result<Void>("Load Connection Data");

        synchronized (modelState) {
            Result<LunaLauf> llRes = re.makeSub(modelState.getLunaLauf());
            if (!llRes.hasResult()) {
                Platform.runLater(() -> re.failed("Luna-Lauf file is not loaded", null).log());
                return;
            }

            for (ConnectionEntry entry : llRes.getResult().getConnections())
                regChatId(entry.getChatId(), entry.getRunner());
        }

        Platform.runLater(() -> re.passed(null, 0, "Done", Lvl.INFO).log());
    }

    @Override
    public void saveConnectionData() {
        Result<Void> re = new Result<Void>("Save Connection Data");

        Result<LunaLauf> llRes = re.makeSub(modelState.getLunaLauf());
        if (!llRes.hasResult()) {
            Platform.runLater(() -> re.failed("Luna-Lauf file is not loaded", null).log());
            return;
        }

        Set<ConnectionEntry> connectionEntries = new HashSet<>();
        for (Entry<Long, Runner> entry : chatId2runner.entrySet()) {
            // TODO do this in LunaLaufApi
            ConnectionEntry connectionEntry = LunaLaufLanguageFactory.eINSTANCE.createConnectionEntry();
            connectionEntry.setChatId(entry.getKey());
            connectionEntry.setRunner(entry.getValue());
            connectionEntries.add(connectionEntry);
        }

        synchronized (modelState) {
            llRes.getResult().getConnections().clear();
            llRes.getResult().getConnections().addAll(connectionEntries);
        }

        Platform.runLater(() -> re.passed(null, 0, "Done", Lvl.INFO).log());
    }

    public void setImageViewer(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    public void removeImageViewer(ImageViewer imageViewer) {
        if (this.imageViewer != null && this.imageViewer.equals(imageViewer))
            this.imageViewer = null;
    }

    public void sendGlobalMessage(String message, boolean onlyTeams) throws TelegramApiException {
        for (Entry<Long, Runner> entry : chatId2runner.entrySet()) {
            if (onlyTeams && entry.getValue().getTeam() == null)
                continue;
            execute(this.send(entry.getKey(), message));
        }
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    @Override
    public String getBotUsername() {
        return "lunalauf_counter_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (beSilent())
            return;

        try {
            if (update.hasCallbackQuery()) {
                handleCallbackQuery(update.getCallbackQuery());
                return;
            }

            if (!update.hasMessage())
                return;

            Message msg = update.getMessage();
            if (msg.isCommand())
                handleCommand(msg.getChatId(), msg.getText());
            else if (msg.hasPhoto())
                handlePhoto(msg.getChatId(), msg.getCaption(), msg.getPhoto());
            else
                handleCommandReply(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    protected void regChatId(long chatId, Runner runner) {
        chatId2runner.put(chatId, runner);
        runner2chatId.put(runner, chatId);
    }

    private void handleCommand(long chatId, String text) throws TelegramApiException {
        switch (text) {
            case CMD_START -> handleStartCommand(chatId);
            case CMD_COUNT -> handleCountCommand(chatId);
            default -> {
            }
        }
    }

    private void handleStartCommand(long chatId) throws TelegramApiException {
        execute(this.send(chatId, AWR_START));
        pendingReplies.setCommandReply(chatId, CMD_START);
    }

    private void handleCountCommand(long chatId) throws TelegramApiException {
        countRound(chatId);
    }

    private void handleCommandReply(Message msg) throws TelegramApiException {
        if (pendingReplies.hasReply(msg.getChatId())) {
            Reply reply = pendingReplies.getReply(msg.getChatId());
            if (reply instanceof CommandReply) {
                switch (((CommandReply) reply).getCommand()) {
                    case CMD_START:
                        handleStartReply(msg);
                        break;
                    default:
                        break;
                }
            } else {
                pendingReplies.removeReply(msg.getChatId());
                register(msg);
            }
        } else {
            register(msg);
        }
    }

    private void handleStartReply(Message msg) throws TelegramApiException {
        register(msg);
    }

    private void register(Message msg) throws TelegramApiException {
        SendChatAction typingAction = new SendChatAction();
        typingAction.setChatId(Long.toString(msg.getChatId()));
        typingAction.setAction(ActionType.TYPING);
        execute(typingAction);

        SendMessage sendMsg = this.send(msg.getChatId(), AWR_FAILURE);
        if (modelState instanceof ModelState.Loaded modelStateLoaded) {
            RunnersState runnersState = modelStateLoaded.getRunners().getValue();
            try {
                long chipId = Long.parseLong(msg.getText());

                Runner runner = runnersState.getRunner(chipId);
                if (runner != null) {
                    register(msg.getChatId(), runner);
                    return;
                } else {
                    sendMsg = this.send(msg.getChatId(), AWR_NO_RUNNER);
                }
            } catch (NumberFormatException e) {
                sendMsg = this.send(msg.getChatId(), AWR_NAN);
            } catch (IllegalStateException e) {
                sendMsg = this.send(msg.getChatId(), AWR_FAILURE);
            }
        } else {
            this.send(msg.getChatId(), AWR_NO_MODEL);
        }

        execute(sendMsg);
    }

    private void register(long chatId, Runner runner) throws IllegalStateException, TelegramApiException {
        boolean registeredChatId = chatId2runner.containsKey(chatId);
        boolean registeredRunner = runner2chatId.containsKey(runner);
        boolean isTeam;
        synchronized (modelState) {
            isTeam = runner.getTeam() != null;
        }

        if (!registeredChatId && !registeredRunner) {
            regChatId(chatId, runner);
            SendMessage sMsg = this.send(chatId, isTeam ? AWR_REGISTERED_T : AWR_REGISTERED_R);
            sMsg.setReplyMarkup(countKeyboard);
            pendingReplies.setCountReply(chatId, execute(sMsg));
        } else if (registeredChatId && registeredRunner && chatId2runner.get(chatId).equals(runner)) {
            execute(this.send(chatId, isTeam ? AWR_ALREADY_REG_T : AWR_ALREADY_REG_R));
        } else if (registeredRunner) {
            SendMessage sMsg = this.send(chatId, AWR_OTHER_DEV);
            sMsg.setReplyMarkup(confirmationKeyboard);
            Message message = execute(sMsg);

            ConfirmationReply reply = pendingReplies.setConfirmationReply(chatId, message, AWR_OTHER_DEV);
            reply.setConfirmAction(() -> {
                long oldChatId = runner2chatId.get(runner);
                runner2chatId.remove(runner);
                chatId2runner.remove(oldChatId);

                regChatId(chatId, runner);

                execute(this.send(oldChatId, AWR_REMOVED));
                pendingReplies.removeReply(oldChatId);
                SendMessage sMsgReply = this.send(chatId, isTeam ? AWR_REGISTERED_T : AWR_REGISTERED_R);
                sMsgReply.setReplyMarkup(countKeyboard);
                pendingReplies.setCountReply(chatId, execute(sMsgReply));
            });
        } else if (registeredChatId) {
            SendMessage sMsg = this.send(chatId, AWR_OTHER_REG);
            sMsg.setReplyMarkup(confirmationKeyboard);
            Message message = execute(sMsg);

            ConfirmationReply reply = pendingReplies.setConfirmationReply(chatId, message, AWR_OTHER_REG);
            reply.setConfirmAction(() -> {
                runner2chatId.remove(chatId2runner.get(chatId));
                regChatId(chatId, runner);

                SendMessage sMsgReply = this.send(chatId, isTeam ? AWR_REGISTERED_T : AWR_REGISTERED_R);
                sMsgReply.setReplyMarkup(countKeyboard);
                pendingReplies.setCountReply(chatId, execute(sMsgReply));
            });
        } else {
            execute(this.send(chatId, AWR_FAILURE));
        }
    }

    private void handleCallbackQuery(CallbackQuery query) throws TelegramApiException {
        long chatId = query.getMessage().getChatId();
        String data = query.getData();
        if (pendingReplies.hasReply(chatId)) {
            Reply reply = pendingReplies.getReply(chatId);
            handleReply(query, chatId, data, reply);
        } else {
            switch (data) {
                case CMD_COUNT:
                    countRound(chatId);
                    break;
                default:
                    execute(this.send(chatId, AWR_INVALID));
                    break;
            }
        }
    }

    private void handleReply(CallbackQuery query, long chatId, String data, Reply reply) throws TelegramApiException {
        if (reply instanceof InlineReply inlineReply) {
            if (inlineReply.getMessage().getMessageId().equals(query.getMessage().getMessageId())) {
                handleInlineReply(inlineReply, chatId, data);
            } else {
                switch (data) {
                    case CMD_COUNT:
                        countRound(chatId);
                        break;
                    default:
                        execute(this.send(chatId, AWR_INVALID));
                        pendingReplies.removeReply(chatId);
                        break;
                }
            }
        } else {
            execute(this.send(chatId, AWR_FAILURE));
            pendingReplies.removeReply(chatId);
        }
    }

    private void handleInlineReply(InlineReply inlineReply, long chatId, String data) throws TelegramApiException {
        if (inlineReply instanceof ConfirmationReply) {
            handleConfirmationReply((ConfirmationReply) inlineReply, chatId, data);
        } else if (inlineReply instanceof CountReply) {
            handleCountReply((CountReply) inlineReply, chatId, data);
        } else {
            execute(this.send(chatId, AWR_FAILURE));
            pendingReplies.removeReply(chatId);
        }
    }

    private void handleConfirmationReply(ConfirmationReply confReply, long chatId, String data) throws TelegramApiException {
        switch (data) {
            case "yes" -> {
                pendingReplies.processedReply(chatId);
                confReply.runConfirmAction();
            }
            case "no" -> {
                pendingReplies.processedReply(chatId);
                confReply.runCancelAction();
            }
            default -> execute(this.send(chatId, AWR_INVALID));
        }
    }

    private void handleCountReply(CountReply countReply, long chatId, String data) throws TelegramApiException {
        if (CMD_COUNT.equals(data)) {
            countRound(chatId);
        } else {
            execute(this.send(chatId, AWR_INVALID));
        }
    }

    private void countRound(long chatId) throws TelegramApiException {
        pendingReplies.removeReply(chatId);

        Runner runner = chatId2runner.get(chatId);
        if (runner == null) {
            execute(this.send(chatId, AWR_NO_REG));
            return;
        }

        Message message;

        Result<Round> res = modelState.logRound(runner, 1, false);
        if (res.isFailed()) {
            SendMessage sMsg = this.send(chatId, AWR_FAILURE);
            sMsg.setReplyMarkup(countKeyboard);
            message = execute(sMsg);
        } else if (res.getCode() == 0) {
            SendMessage sMsg = this.send(chatId, AWR_TOOFAST);
            sMsg.setReplyMarkup(countKeyboard);
            message = execute(sMsg);
        } else if (res.getCode() == 4) {
            SendMessage sMsg = this.send(chatId, AWR_NOT_IN_TIME);
            sMsg.setReplyMarkup(countKeyboard);
            message = execute(sMsg);
        } else if (res.getCode() == 5) {
            SendMessage sMsg = this.send(chatId, LAST_ROUND_LOGGED);
            sMsg.setReplyMarkup(countKeyboard);
            message = execute(sMsg);
        } else {
            int rounds;
            synchronized (modelState) {
                rounds = runner.numOfRounds();
            }
            String text = "<b><i>Runde " + rounds + "</i></b>";
            if (rounds % 10 == 0)
                text += " 🎉 weiter so!";
            SendMessage sMsg = this.send(chatId, text);
            sMsg.setReplyMarkup(countKeyboard);
            message = execute(sMsg);
        }

        pendingReplies.setCountReply(chatId, message);
        Platform.runLater(res::log);
    }

    private void handlePhoto(Long chatId, String caption, List<PhotoSize> photoSizes) throws TelegramApiException {
        Runner runner = chatId2runner.get(chatId);
        if (runner == null) {
            execute(this.send(chatId, AWR_NO_REG));
            return;
        }

        if (runner.getTeam() == null) {
            execute(this.send(chatId, AWR_ONLY_TEAM_PHOTO));
            return;
        }

        if (imageViewer == null) {
            execute(this.send(chatId, AWR_NO_PHOTOS));
            return;
        }

        boolean backdoor = false;
        if (!imageViewer.acceptFrom(runner)) {
            if (!"L".equals(caption)) {
                execute(this.send(chatId, AWR_ALREADY_SENT));
                return;
            }
            backdoor = true;
        }
        boolean backdoorFinal = backdoor;

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                PhotoSize bigPhotoSize = photoSizes.get(photoSizes.size() - 1);
                GetFile getFile = new GetFile();
                getFile.setFileId(bigPhotoSize.getFileId());
                org.telegram.telegrambots.meta.api.objects.File file = execute(getFile);
                String fileURL = file.getFileUrl(botToken);

                String photoFilePath = calcPhotoFilePath(runner, file);
                File systemFile = new File(photoFilePath);

                try {
                    FileUtils.copyURLToFile(new URI(fileURL).toURL(), systemFile, 5000, 30000);
                } catch (IOException e) {
                    RoundCounterBot.this.execute(RoundCounterBot.this.send(chatId, AWR_FAILURE));
                    logger.error("Failed to load photo from Telegram server onto disk", e);
                }

                if (!backdoorFinal) {
                    File image = new File("file:///" + photoFilePath);
                    Platform.runLater(() -> imageViewer.view(runner, image));
                }

                RoundCounterBot.this.execute(RoundCounterBot.this.send(chatId, AWR_PHOTO_RECEIVED));

                return null;
            }
        };

        executor.execute(task);
    }

    private String calcPhotoFilePath(Runner runner, org.telegram.telegrambots.meta.api.objects.File file) {
        String modelPath = modelState.getResource().getURI().devicePath();
        String path = modelPath.substring(0, modelPath.lastIndexOf("/") + 1);
        String extension = file.getFilePath().substring(file.getFilePath().lastIndexOf("."));
        return path + "team_photos/" + runner.getTeam().getName() + extension;
    }

}
