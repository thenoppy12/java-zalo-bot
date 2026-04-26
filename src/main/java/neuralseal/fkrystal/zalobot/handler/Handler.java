package neuralseal.fkrystal.zalobot.handler;

import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.models.Update;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Handler {
    record CallbackContext(ZaloBot bot, @Nullable List<String> args) {}
    boolean checkUpdate(Update update);
    CompletableFuture<Void> handleUpdate(Update update, ZaloBot bot);
}
