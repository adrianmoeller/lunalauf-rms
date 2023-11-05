package lunalauf.rms.utilities.network.bot;

import lunalauf.rms.modelapi.ModelState;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class BotManager {

	private String[] tokens;
	private ModelState modelState;
	private TelegramBotsApi api;

	private List<BotSession> botSessions;

	// Runner Info
	private RunnerInfoBot runnerInfoBot = null;
	private BotSession runnerInfoBotSession = null;

	// Round Counter
	private RoundCounterBot roundCounterBot = null;
	private BotSession roundCounterBotSession = null;

	public BotManager(String[] tokens, ModelState modelState) throws TelegramApiException {
		this.tokens = tokens;
		this.modelState = modelState;

		api = new TelegramBotsApi(DefaultBotSession.class);
		botSessions = new ArrayList<>();
	}

	public void terminate() {
		if (roundCounterBot != null)
			roundCounterBot.shutdownExecutor();

		botSessions.forEach(bs -> {
			if (bs.isRunning())
				bs.stop();
		});
	}

	public void switchStateRunnerInfoBot(boolean silentStart, boolean loadData) throws TelegramApiException {
		if (runnerInfoBotSession == null) {
			runnerInfoBot = new RunnerInfoBot(tokens[0], modelState, silentStart, loadData);
			runnerInfoBotSession = api.registerBot(runnerInfoBot);
			botSessions.add(runnerInfoBotSession);
		} else if (runnerInfoBotSession.isRunning()) {
			runnerInfoBotSession.stop();
		} else {
			runnerInfoBotSession.start();
		}
	}

	public boolean isRunnerInfoBotRunning() {
		if (runnerInfoBotSession == null)
			return false;
		else
			return runnerInfoBotSession.isRunning();
	}

	public void switchStateRoundCounterBot(boolean silentStart, boolean loadData) throws TelegramApiException {
		if (roundCounterBotSession == null) {
			roundCounterBot = new RoundCounterBot(tokens[1], modelState, silentStart, loadData);
			roundCounterBotSession = api.registerBot(roundCounterBot);
			botSessions.add(roundCounterBotSession);
		} else if (roundCounterBotSession.isRunning()) {
			roundCounterBotSession.stop();
		} else {
			roundCounterBotSession.start();
		}
	}

	public boolean isRoundCounterBotRunning() {
		if (roundCounterBotSession == null)
			return false;
		else
			return roundCounterBotSession.isRunning();
	}

	public RunnerInfoBot getRunnerInfoBot() {
		return runnerInfoBot;
	}

	public RoundCounterBot getRoundCounterBot() {
		return roundCounterBot;
	}

}
