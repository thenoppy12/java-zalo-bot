package neuralseal.fkrystal.zalobot.handlers;

import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.handler.Handler;
import neuralseal.fkrystal.zalobot.models.Update;
import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;

/**
 * Handle messages, like checking message have specific text or something.
 */
public class MessageHandler implements Handler {
    private final Predicate<Update> filters;
    private final MessageCallback callback;

    public MessageHandler(Predicate<Update> filters, MessageCallback callback) {
        this.filters = filters;
        this.callback = callback;
    }

    @Override
    public boolean checkUpdate(Update update) {
        return update.message() != null && filters.test(update);
    }

    @Override
    public CompletableFuture<Void> handleUpdate(Update update, ZaloBot bot) {
        return CompletableFuture.runAsync(() -> {
            CallbackContext context = new CallbackContext(bot, null);
            callback.accept(update, context);
        });
    }

    @FunctionalInterface
    public interface MessageCallback {
        void accept(Update update, CallbackContext context);
    }
}
