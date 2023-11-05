package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class RoundCountRejectedResponse extends RoundCountResponse {

    @SerializedName("msg")
    public String causeMessage;

    public RoundCountRejectedResponse() {
        super(ResponseType.ROUND_COUNT_REJECTED);
    }

}
