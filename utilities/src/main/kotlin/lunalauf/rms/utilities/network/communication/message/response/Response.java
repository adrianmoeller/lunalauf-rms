package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.Message;
import lunalauf.rms.utilities.network.communication.message.type.MessageType;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class Response extends Message {

    @SerializedName("res-type")
    public final String responseType;

    public Response(ResponseType responseType) {
        super(MessageType.RESPONSE);
        this.responseType = responseType.name();
    }

}
