package neuralseal.fkrystal.zalobot;

import org.jetbrains.annotations.Nullable;

/**
 * Data cho webhook server
 * @param port Cổng để chạy server
 * @param path Endpoint để nhận, nên đặt để tránh crawler
 * @param expectedSecretToken Webhook token, cho thêm bảo mật
 */
public record WebhookServerData(int port, String path, @Nullable String expectedSecretToken) {}