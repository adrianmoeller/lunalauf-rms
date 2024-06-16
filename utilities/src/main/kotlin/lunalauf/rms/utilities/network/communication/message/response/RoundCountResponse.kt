package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

abstract class RoundCountResponse(responseType: ResponseType) : Response(responseType) {
    @SerializedName("name")
    var name: String = ""
}
