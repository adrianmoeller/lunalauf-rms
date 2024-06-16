package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

class RoundCountRejectedResponse : RoundCountResponse(ResponseType.ROUND_COUNT_REJECTED) {
    @SerializedName("msg")
    var causeMessage: String? = null
}
