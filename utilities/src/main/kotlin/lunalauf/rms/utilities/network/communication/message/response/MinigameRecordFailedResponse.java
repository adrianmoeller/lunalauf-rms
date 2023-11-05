package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class MinigameRecordFailedResponse extends MinigameRecordResponse {

    @SerializedName("msg")
    public String causeMessage;

    public MinigameRecordFailedResponse() {
        super(ResponseType.MINIGAME_RECORD_FAILED);
    }

}
