package neuralseal.fkrystal.zalobot.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
    TEXT_RECEIVED("message.text.received"),
    IMAGE_RECEIVED("message.image.received"),
    STICKER_RECEIVED("message.sticker.received"),
    UNSUPPORTED_RECEIVED("message.unsupported.received"),
    UNKNOWN("unknown");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EventType fromString(String value) {
        if (value == null) return UNKNOWN;
        for (EventType type : EventType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        System.err.println("Got unknown Zalo event: " + value);
        return UNKNOWN;
    }
}