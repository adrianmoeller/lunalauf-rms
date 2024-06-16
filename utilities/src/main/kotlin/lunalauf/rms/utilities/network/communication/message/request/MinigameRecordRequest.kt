package lunalauf.rms.utilities.network.communication.message.request

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.RequestType

class MinigameRecordRequest : Request(RequestType.MINIGAME_RECORD) {
    @SerializedName("runner-id")
    var runnerId: Long = 0

    @SerializedName("minigame-id")
    var minigameId = 0

    @SerializedName("points")
    var points = 0
}
