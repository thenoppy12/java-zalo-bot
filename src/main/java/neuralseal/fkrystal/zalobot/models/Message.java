package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Message(
        @JsonProperty("message_id") String messageId,
        @JsonProperty("text") String text,
        @JsonProperty("chat") Chat chat,
        @JsonProperty("from") User from,
        @JsonProperty("date") Long date,
        @JsonProperty("photo") String photo,
        @JsonProperty("caption") String caption,
        @JsonProperty("sticker") String sticker,
        @JsonProperty("url") String url
) {}