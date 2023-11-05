package lunalauf.rms.utilities.network.communication.message.response;

import com.google.gson.annotations.SerializedName;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

public class TeamRunnerInfoResponse extends RunnerInfoResponse {

    @SerializedName("team")
    public String teamName;
    @SerializedName("team-rounds")
    public int numTeamRounds;
    @SerializedName("minigame")
    public int teamMinigamePoints;

    public TeamRunnerInfoResponse() {
        super(ResponseType.TEAMRUNNER_INFO);
    }

}
