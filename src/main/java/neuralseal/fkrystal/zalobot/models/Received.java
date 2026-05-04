package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Received(
        @JsonProperty("update_id") long updateId,
        @JsonProperty("event_name") EventType eventName,
        @JsonProperty("message") Message message
) {}