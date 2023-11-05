package lunalauf.rms.utilities.network.communication;

import com.google.gson.*;
import lunalauf.rms.utilities.network.communication.message.Message;
import lunalauf.rms.utilities.network.communication.message.request.MinigameRecordRequest;
import lunalauf.rms.utilities.network.communication.message.request.RoundCountRequest;
import lunalauf.rms.utilities.network.communication.message.request.RunnerInfoRequest;
import lunalauf.rms.utilities.network.communication.message.response.*;
import lunalauf.rms.utilities.network.communication.message.type.MessageType;
import lunalauf.rms.utilities.network.communication.message.type.RequestType;
import lunalauf.rms.utilities.network.communication.message.type.ResponseType;

import java.lang.reflect.Type;

public class MessageProcessor {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Message.class, new MessageDeserializer())
            .create();

    public static String toJsonString(Message message) {
        return gson.toJson(message);
    }

    public static Message fromJsonString(String jsonString) throws JsonParseException {
        return gson.fromJson(jsonString, Message.class);
    }

    private static class MessageDeserializer implements JsonDeserializer<Message> {

        @Override
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            return switch (MessageType.valueOf(type)) {
                case REQUEST -> deserializeRequest(jsonObject, json, context);
                case RESPONSE -> deserializeResponse(jsonObject, json, context);
                default -> throw new JsonParseException("Unknown type: " + type);
            };
        }

        private Message deserializeRequest(JsonObject jsonObject, JsonElement json, JsonDeserializationContext context) {
            String requestType = jsonObject.get("req-type").getAsString();

            return switch (RequestType.valueOf(requestType)) {
                case ROUND_COUNT -> context.deserialize(json, RoundCountRequest.class);
                case RUNNER_INFO -> context.deserialize(json, RunnerInfoRequest.class);
                case MINIGAME_RECORD -> context.deserialize(json, MinigameRecordRequest.class);
                default -> throw new JsonParseException("Unknown request type: " + requestType);
            };
        }

        private Message deserializeResponse(JsonObject jsonObject, JsonElement json, JsonDeserializationContext context) {
            String responseType = jsonObject.get("res-type").getAsString();

            return switch (ResponseType.valueOf(responseType)) {
                case ROUND_COUNT_ACCEPTED -> context.deserialize(json, RoundCountAcceptedResponse.class);
                case ROUND_COUNT_REJECTED -> context.deserialize(json, RoundCountRejectedResponse.class);
                case RUNNER_INFO -> context.deserialize(json, RunnerInfoResponse.class);
                case TEAMRUNNER_INFO -> context.deserialize(json, TeamRunnerInfoResponse.class);
                case MINIGAME_RECORD_DONE -> context.deserialize(json, MinigameRecordDoneResponse.class);
                case MINIGAME_RECORD_FAILED -> context.deserialize(json, MinigameRecordFailedResponse.class);
                case ERROR -> context.deserialize(json, ErrorResponse.class);
                default -> throw new JsonParseException("Unknown response type: " + responseType);
            };
        }

    }

}
