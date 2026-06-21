package neuralseal.fkrystal.zalobot.handlers;

import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.Handler;
import neuralseal.fkrystal.zalobot.models.Received;

import java.util.function.Predicate;
import java.util.concurrent.CompletableFuture;

public class MessageHandler implements Handler {
    private final Predicate<Received> filters;
    private final MessageCallback callback;

    /**
     * Handler cho tin nhắn được nhận (gửi bởi user)<br>
     * Chỉ cần register nóy và {@code filters} sẽ check trước khi chạy đống {@code callback}
     * @param filters Đoán trước khi nào sẽ chạy đống {@code callback}<br>
     * Ví dụ: Có tin nhắn nhận:<br>{@code update -> Objects.nonNull(update)}<br>
     * hoặc tin nhắn chứa chữ "hello":<br>{@code update -> "hello".equals(update.message().text())}
     * @param callback Đống code sẽ chạy khi {@code filters} trả về {@code true}<br>
     * Có thể dùng các web API ở đây
     */
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