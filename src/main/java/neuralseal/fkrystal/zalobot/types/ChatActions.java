package neuralseal.fkrystal.zalobot.types;

public enum ChatActions {
    TYPING("typing"),
    UPLOAD_PHOTO("upload_photo"); // Still preview

    private final String action;
    ChatActions(String action) {
        this.action = action;
    }

    public String getChatAction() {
        return action;
    }

}
