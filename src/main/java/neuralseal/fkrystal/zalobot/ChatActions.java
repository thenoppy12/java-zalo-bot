package neuralseal.fkrystal.zalobot;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ChatActions {
    TYPING("typing"),
    UPLOAD_PHOTO("upload_photo"); // Still preview

    private final String action;
    ChatActions(String action) {
        this.action = action;
    }

    @JsonCreator
    public String getChatAction() {
        return action;
    }

}