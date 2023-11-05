package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class RoundCountAcceptedResponse extends RoundCountResponse {

    @SerializedName("rounds")
    public int newNumRounds;

    public RoundCountAcceptedResponse() {
        super(ResponseType.ROUND_COUNT_ACCEPTED);
    }

}
