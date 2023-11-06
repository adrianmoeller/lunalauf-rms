package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

class RoundCountAcceptedResponse : RoundCountResponse(ResponseType.ROUND_COUNT_ACCEPTED) {
    @SerializedName("rounds")
    var newNumRounds = 0
}
