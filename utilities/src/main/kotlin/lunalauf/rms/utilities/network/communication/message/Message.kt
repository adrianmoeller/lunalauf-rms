package lunalauf.rms.utilities.network.communication.message

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.message.type.MessageType

abstract class Message(
    type: MessageType
) {
    @SerializedName("type")
    val type: String

    @JvmField
    @SerializedName("id")
    var messageId: Long = 0

    init {
        this.type = type.name
    }
}
