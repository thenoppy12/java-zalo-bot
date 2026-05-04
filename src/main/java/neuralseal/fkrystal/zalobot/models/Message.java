package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
        @JsonProperty("message_id") String messageId,
        @JsonProperty("message_type") MessageType messageType,
        @JsonProperty("text") String text,
        @JsonProperty("chat") Chat chat,
        @JsonProperty("from") User from,
        @JsonProperty("date") Long date,
        @JsonProperty("photo_url") String photoUrl,
        @JsonProperty("caption") String caption,
        @JsonProperty("sticker") String sticker,
        @JsonProperty("url") String stickerUrl
) {}