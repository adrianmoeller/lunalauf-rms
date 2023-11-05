package lunalauf.rms.utilities.network.communication.message.request;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.Message;
import lunalauf.rms.utilities.network.communication.message.type.MessageType;
import lunalauf.rms.utilities.network.communication.message.type.RequestType;

public abstract class Request extends Message {

    @SerializedName("req-type")
    public final String requestType;

    public Request(RequestType requestType) {
        super(MessageType.REQUEST);
        this.requestType = requestType.name();
    }

}
