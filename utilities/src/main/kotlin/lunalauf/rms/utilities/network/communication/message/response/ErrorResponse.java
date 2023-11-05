package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.ErrorType;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class ErrorResponse extends Response {

    @SerializedName("error")
    public ErrorType error;

    public ErrorResponse() {
        super(ResponseType.ERROR);
    }
}
