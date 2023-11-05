package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class RunnerInfoResponse extends Response {

    @SerializedName("runner")
    public String runnerName;
    @SerializedName("runner-id")
    public long runnerId;
    @SerializedName("rounds")
    public int numRunnerRounds;

    public RunnerInfoResponse() {
        super(ResponseType.RUNNER_INFO);
    }

    public RunnerInfoResponse(ResponseType responseType) {
        super(responseType);
    }

}
