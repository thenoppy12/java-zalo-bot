package neuralseal.fkrystal.zalobot.handlers;

import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.Handler;
import neuralseal.fkrystal.zalobot.models.Received;

import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;

/**
 * Handle messages, like checking message have specific text or something.
 */
public class MessageHandler implements Handler {
    private final Predicate<Received> filters;
    private final MessageCallback callback;

    public MessageHandler(Predicate<Received> filters, MessageCallback callback) {
        this.filters = filters;
        this.callback = callback;
    }

    @Override
    public boolean checkUpdate(Received received) {
        return received.message() != null && filters.test(received);
    }

    @Override
    public CompletableFuture<Void> handleUpdate(Received received, ZaloBot bot) {
        return CompletableFuture.runAsync(() -> {
            CallbackContext context = new CallbackContext(bot, null);
            callback.accept(received, context);
        });
    }

    @FunctionalInterface
    public interface MessageCallback {
        void accept(Received received, CallbackContext context);
    }
}