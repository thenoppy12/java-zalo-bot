package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Generic<T>(
        @JsonProperty("ok") boolean ok,
        @JsonProperty("result") T result,
        @JsonProperty("error_code") int errorCode,
        @JsonProperty("description") String description
) {}