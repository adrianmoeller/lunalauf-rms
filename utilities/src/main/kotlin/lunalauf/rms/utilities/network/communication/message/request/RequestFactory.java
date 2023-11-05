package lunalauf.rms.utilities.network.communication.message.request;

import java.util.Random;

public class RequestFactory {

    private long messageIdCounter;

    public RequestFactory() {
        messageIdCounter = new Random().nextLong();
    }

    private long getNextMessageId() {
        messageIdCounter = (messageIdCounter + 1) % (Long.MAX_VALUE - 1);
        return messageIdCounter;
    }

    public MinigameRecordRequest createMinigameRecordRequest(long runnerId, int minigameId, int points) {
        MinigameRecordRequest request = new MinigameRecordRequest();
        request.messageId = getNextMessageId();
        request.runnerId = runnerId;
        request.minigameId = minigameId;
        request.points = points;
        return request;
    }

    public RoundCountRequest createRoundCoundRequest(long runnerId) {
        RoundCountRequest request = new RoundCountRequest();
        request.messageId = getNextMessageId();
        request.runnerId = runnerId;
        return request;
    }

    public RunnerInfoRequest createRunnerInfoRequest(long runnerId) {
        RunnerInfoRequest request = new RunnerInfoRequest();
        request.messageId = getNextMessageId();
        request.runnerId = runnerId;
        return request;
    }

}
