package lunalauf.rms.utilities.network.communication.message.request;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.RequestType;

public class RunnerInfoRequest extends Request {

    @SerializedName("runner-id")
    public long runnerId;

    public RunnerInfoRequest() {
        super(RequestType.RUNNER_INFO);
    }

}
