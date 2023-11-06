package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

class TeamRunnerInfoResponse : RunnerInfoResponse(ResponseType.TEAMRUNNER_INFO) {
    @SerializedName("team")
    var teamName: String? = null

    @SerializedName("team-rounds")
    var numTeamRounds = 0

    @SerializedName("minigame")
    var teamMinigamePoints = 0
}
