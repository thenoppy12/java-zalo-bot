package neuralseal.fkrystal.zalobot;

import neuralseal.fkrystal.zalobot.models.Received;
import neuralseal.fkrystal.zalobot.handlers.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handler gốc để tạo các handler cho riêng mình
 * @see MessageHandler
 * @see CommandHandler
 */
public interface Handler {
    record CallbackContext(ZaloBot bot, @Nullable List<String> args) {}
    boolean checkUpdate(Received received);
    CompletableFuture<Void> handleUpdate(Received received, ZaloBot bot);
}