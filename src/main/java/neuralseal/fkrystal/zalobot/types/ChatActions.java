package neuralseal.fkrystal.zalobot.types;

public enum ChatActions {
    TYPING("typing"),
    @SuppressWarnings("preview") UPLOAD_PHOTO("upload_photo");

    private final String action;
    ChatActions(String action) {
        this.action = action;
    }

    public String getChatAction() {
        return action;
    }

}
