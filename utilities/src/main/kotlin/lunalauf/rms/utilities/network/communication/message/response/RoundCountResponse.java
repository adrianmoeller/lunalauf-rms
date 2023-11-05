package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public abstract class RoundCountResponse extends Response {

    @SerializedName("name")
    public String name;

    public RoundCountResponse(ResponseType responseType) {
        super(responseType);
    }
}
