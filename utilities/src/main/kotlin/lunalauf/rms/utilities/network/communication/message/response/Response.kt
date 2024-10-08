package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.communication.message.type.MessageType
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

open class Response(responseType: ResponseType) : Message(MessageType.RESPONSE) {
    @SerializedName("res-type")
    val responseType: String = responseType.name

    override fun toString(): String {
        return super.toString()
            .replace(")", ", type: $responseType)")
    }
}
