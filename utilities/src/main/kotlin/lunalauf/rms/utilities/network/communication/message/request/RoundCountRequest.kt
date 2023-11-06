package lunalauf.rms.utilities.network.communication.message.request

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.RequestType

class RoundCountRequest : Request(RequestType.ROUND_COUNT) {
    @SerializedName("runner-id")
    var runnerId: Long = 0
}
