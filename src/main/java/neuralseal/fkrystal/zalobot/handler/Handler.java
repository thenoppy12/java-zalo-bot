package neuralseal.fkrystal.zalobot.handler;

import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.models.Update;
import java.util.concurrent.CompletableFuture;

public interface Handler {
    boolean checkUpdate(Update update);
    CompletableFuture<Void> handleUpdate(Update update, ZaloBot bot);
}
