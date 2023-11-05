package lunalauf.rms.utilities.network.util;

public class RepetitionHandler {

    private final long repetitionTimeThreshold;
    private long lastId = -1;
    private long lastTimeMillis = 0;

    public RepetitionHandler(long repetitionTimeThreshold) {
        this.repetitionTimeThreshold = repetitionTimeThreshold;
    }

    public boolean isUnwantedRepetition(long id) {
        if (id == lastId)
            if (System.currentTimeMillis() - lastTimeMillis < repetitionTimeThreshold)
                return true;
        lastId = id;
        lastTimeMillis = System.currentTimeMillis();
        return false;
    }

}
