package neuralseal.fkrystal.zalobot.impl;

import org.jetbrains.annotations.Nullable;

public record WebhookServerData(int port, String path, @Nullable String expectedSecretToken) {}