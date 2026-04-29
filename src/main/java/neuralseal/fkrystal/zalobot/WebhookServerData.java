package neuralseal.fkrystal.zalobot;

import org.jetbrains.annotations.Nullable;

public record WebhookServerData(int port, String path, @Nullable String expectedSecretToken) {}