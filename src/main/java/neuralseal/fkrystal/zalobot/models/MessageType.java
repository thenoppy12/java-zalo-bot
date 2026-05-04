package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    STICKER("CHAT_STICKER"),
    IMAGE("CHAT_PHOTO"),
    NONE("none"),
    UNKNOWN("unknown");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MessageType fromString(String value) {
        if (value == null) return NONE;
        for (MessageType type : MessageType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        System.err.println("Got unknown Zalo message type: " + value);
        return UNKNOWN;
    }
}
