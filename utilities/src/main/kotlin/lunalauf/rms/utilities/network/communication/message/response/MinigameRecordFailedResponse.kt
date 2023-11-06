package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

class MinigameRecordFailedResponse : MinigameRecordResponse(ResponseType.MINIGAME_RECORD_FAILED) {
    @SerializedName("msg")
    var causeMessage: String? = null
}
