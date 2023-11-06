package lunalauf.rms.utilities.network.communication.message.response

import com.google.gson.annotations.SerializedName
import lunalauf.rms.utilities.network.communication.ErrorType
import lunalauf.rms.utilities.network.communication.message.type.ResponseType

class ErrorResponse : Response(ResponseType.ERROR) {
    @SerializedName("error")
    var error: ErrorType = ErrorType.UNKNOWN_ERROR
}
