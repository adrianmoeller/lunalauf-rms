package lunalauf.rms.utilities.network.communication.message.request;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.RequestType;

public class RoundCountRequest extends Request {

    @SerializedName("runner-id")
    public long runnerId;

    public RoundCountRequest() {
        super(RequestType.ROUND_COUNT);
    }

}
