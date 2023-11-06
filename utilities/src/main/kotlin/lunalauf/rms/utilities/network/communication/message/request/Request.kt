package lunalauf.rms.utilities.network.communication.message.request

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.communication.message.type.MessageType
import lunalauf.rms.utilities.network.communication.message.type.RequestType

abstract class Request(requestType: RequestType) : Message(MessageType.REQUEST) {
    @SerializedName("req-type")
    val requestType: String

    init {
        this.requestType = requestType.name
    }
}
