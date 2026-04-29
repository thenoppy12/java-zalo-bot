package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
        @JsonProperty("id") String id,
        @JsonProperty("account_name") String accountName,
        @JsonProperty("account_type") String accountType,
        @JsonProperty("can_join_groups") boolean canJoinGroups,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("is_bot") Boolean isBot
) {}