package neuralseal.fkrystal.zalobot.handlers;

import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.handler.Handler;
import neuralseal.fkrystal.zalobot.models.Update;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handle commands, like /start, /echo [args].
 */
public class CommandHandler implements Handler {
    private final String command;
    private final CommandCallback callback;

    public CommandHandler(String command, CommandCallback callback) {
        this.command = command.toLowerCase();
        this.callback = callback;
    }

    @Override
    public boolean checkUpdate(Update update) {
        if (update.message() == null || update.message().text() == null) return false;
        String text = update.message().text().toLowerCase().trim();
        return text.startsWith("/" + command);
    }

    @Override
    public CompletableFuture<Void> handleUpdate(Update update, ZaloBot bot) {
        return CompletableFuture.runAsync(() -> {
            String text = update.message().text().trim();
            String[] parts = text.split("\\s+");
            List<String> args = (parts.length > 1) ? Arrays.asList(parts).subList(1, parts.length) : Collections.emptyList();
            CallbackContext context = new CallbackContext(bot, args);
            callback.accept(update, context);
        });
    }

    @FunctionalInterface
    public interface CommandCallback {
        void accept(Update update, CallbackContext context);
    }
}