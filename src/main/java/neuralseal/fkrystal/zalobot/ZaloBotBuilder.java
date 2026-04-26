package neuralseal.fkrystal.zalobot;

import neuralseal.fkrystal.zalobot.handler.Handler;

import java.util.ArrayList;
import java.util.List;

public class ZaloBotBuilder {
    private String botManagementName = null;
    private String botToken;
    private final List<Handler> handlers =  new ArrayList<>();
    private int webhookServerPort;
    private String webhookServerPath;
    private String webhookSecret;

    public ZaloBotBuilder() {
        super();
    }

    public ZaloBotBuilder withManagementName(String botManagementName) {
        this.botManagementName = botManagementName;
        return this;
    }

    public ZaloBotBuilder withToken(String botToken) {
        this.botToken = botToken;
        return this;
    }

    public ZaloBotBuilder withHandler(Handler handler) {
        this.handlers.add(handler);
        return this;
    }

    @SuppressWarnings("unchecked")
    public ZaloBotBuilder withWebhookServer(int webhookServerPort,  String webhookServerPath,  String webhookSecret) {
        this.webhookServerPort = webhookServerPort;
        this.webhookServerPath = webhookServerPath;
        this.webhookSecret = webhookSecret;
        return this;
    }

    public ZaloBot build() {
        if (botToken == null) throw new NullPointerException("Bot token is null. Specify a bot token for this.");
        ZaloBot bot = new ZaloBot(botToken, botManagementName);
        bot.addHandler(handlers);
        bot.startWebhookServer(webhookServerPort, webhookServerPath, webhookSecret);
        return bot;
    }
}
