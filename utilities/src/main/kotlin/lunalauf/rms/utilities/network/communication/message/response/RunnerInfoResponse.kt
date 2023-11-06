package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

open class RunnerInfoResponse : Response {
    @SerializedName("runner")
    var runnerName: String? = null

    @SerializedName("runner-id")
    var runnerId: Long = 0

    @SerializedName("rounds")
    var numRunnerRounds = 0

    constructor() : super(ResponseType.RUNNER_INFO)
    constructor(responseType: ResponseType) : super(responseType)
}
