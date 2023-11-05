package lunalauf.rms.utilities.network.communication.message.request;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.RequestType;

public class MinigameRecordRequest extends Request {

    @SerializedName("runner-id")
    public long runnerId;
    @SerializedName("minigame-id")
    public int minigameId;
    @SerializedName("points")
    public int points;

    public MinigameRecordRequest() {
        super(RequestType.MINIGAME_RECORD);
    }
}
