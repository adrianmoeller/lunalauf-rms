package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

class TeamRunnerInfoResponse : RunnerInfoResponse(ResponseType.TEAMRUNNER_INFO) {
    @SerializedName("team")
    var teamName: String = "-"

    @SerializedName("team-rounds")
    var numTeamRounds = 0

    @SerializedName("funfactors")
    var teamFunfactorPoints = 0
}
