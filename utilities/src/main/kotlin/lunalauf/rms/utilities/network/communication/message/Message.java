package lunalauf.rms.utilities.network.communication.message;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.MessageType;

public abstract class Message {

    @SerializedName("type")
    public final String type;
    @SerializedName("id")
    public long messageId;

    public Message(MessageType type) {
        this.type = type.name();
    }

}
