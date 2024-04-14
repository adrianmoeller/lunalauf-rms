package lunalauf.rms.utilities.network.communication

import com.google.gson.*
import lunalauf.rms.utilities.network.communication.message.Message
import lunalauf.rms.utilities.network.communication.message.request.MinigameRecordRequest
import lunalauf.rms.utilities.network.communication.message.request.RoundCountRequest
import lunalauf.rms.utilities.network.communication.message.request.RunnerInfoRequest
import lunalauf.rms.utilities.network.communication.message.response.*
import lunalauf.rms.utilities.network.communication.message.type.MessageType
import lunalauf.rms.utilities.network.communication.message.type.RequestType
import lunalauf.rms.utilities.network.communication.message.type.ResponseType
import java.lang.reflect.Type

object MessageProcessor {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Message::class.java, MessageDeserializer())
        .create()

    fun toJsonString(message: Message): String {
        return gson.toJson(message)
    }

    @Throws(JsonParseException::class)
    fun fromJsonString(jsonString: String): Message {
        return gson.fromJson(jsonString, Message::class.java)
    }

    private class MessageDeserializer : JsonDeserializer<Message> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Message {
            val jsonObject = json.getAsJsonObject()
            val type = jsonObject["type"].asString

            val messageType = try {
                MessageType.valueOf(type)
            } catch (e: IllegalArgumentException) {
                throw JsonParseException("Unknown type: $type")
            }
            return when (messageType) {
                MessageType.REQUEST -> deserializeRequest(jsonObject, json, context)
                MessageType.RESPONSE -> deserializeResponse(jsonObject, json, context)
            }
        }

        private fun deserializeRequest(
            jsonObject: JsonObject,
            json: JsonElement,
            context: JsonDeserializationContext
        ): Message {
            val requestType = jsonObject["req-type"].asString

            val rType = try {
                RequestType.valueOf(requestType)
            } catch (e: IllegalArgumentException) {
                throw JsonParseException("Unknown request type: $requestType")
            }
            return when (rType) {
                RequestType.ROUND_COUNT -> context.deserialize(json, RoundCountRequest::class.java)
                RequestType.RUNNER_INFO -> context.deserialize(json, RunnerInfoRequest::class.java)
                RequestType.MINIGAME_RECORD -> context.deserialize(json, MinigameRecordRequest::class.java)
            }
        }

        private fun deserializeResponse(
            jsonObject: JsonObject,
            json: JsonElement,
            context: JsonDeserializationContext
        ): Message {
            val responseType = jsonObject["res-type"].asString

            val rType = try {
                ResponseType.valueOf(responseType)
            } catch (e: IllegalArgumentException) {
                throw JsonParseException("Unknown response type: $responseType")
            }
            return when (rType) {
                ResponseType.ROUND_COUNT_ACCEPTED -> context.deserialize(json, RoundCountAcceptedResponse::class.java)
                ResponseType.ROUND_COUNT_REJECTED -> context.deserialize(json, RoundCountRejectedResponse::class.java)
                ResponseType.RUNNER_INFO -> context.deserialize(json, RunnerInfoResponse::class.java)
                ResponseType.TEAMRUNNER_INFO -> context.deserialize(json, TeamRunnerInfoResponse::class.java)
                ResponseType.MINIGAME_RECORD_DONE -> context.deserialize(json, MinigameRecordDoneResponse::class.java)
                ResponseType.MINIGAME_RECORD_FAILED -> context.deserialize(json, MinigameRecordFailedResponse::class.java)
                ResponseType.ERROR -> context.deserialize(json, ErrorResponse::class.java)
            }
        }
    }
}
