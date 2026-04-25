package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Webhook(
        @JsonProperty("url") String url,
        @JsonProperty("updated_at") Long updatedAt
) {}