package neuralseal.fkrystal.zalobot;

import neuralseal.fkrystal.zalobot.handler.Handler;
import neuralseal.fkrystal.zalobot.impl.WebhookServerData;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link ZaloBot} builder, hehe
 */
public class ZaloBotBuilder {
    private String _botManagementName = null;
    private String _botToken;
    private final List<Handler> _handlers =  new ArrayList<>();
    private WebhookServerData _webhookServerData;


    /**
     * Set bot's management name
     * @param botManagementName Name of management bot alias
     * @return Current {@link ZaloBotBuilder} instance
     */
    public ZaloBotBuilder withManagementName(String botManagementName) {
        this._botManagementName = botManagementName;
        return this;
    }

    /**
     * Set the bot token
     * @param botToken Valid bot token
     * @return Current {@link ZaloBotBuilder} instance
     */
    public ZaloBotBuilder withToken(String botToken) {
        this._botToken = botToken;
        return this;
    }

    /**
     * Add bot's handler for events.
     * @param handler A class implement {@link Handler}
     * @return Current {@link ZaloBotBuilder} instance
     */
    public ZaloBotBuilder withHandler(Handler handler) {
        this._handlers.add(handler);
        return this;
    }

    /**
     * Set webhook server data (port, path, secret)
     * @param data {@link WebhookServerData}, read that record please
     * @return Current {@link ZaloBotBuilder} instance
     */
    public ZaloBotBuilder withWebhookServerData(WebhookServerData data) {
        this._webhookServerData = data;
        return this;
    }

    /**
     * Build the bot.
     * @return Configured {@link ZaloBot}
     */
    public ZaloBot build() {
        if (_botToken == null) throw new NullPointerException("Bot token is null. Specify a bot token for this.");
        return new ZaloBot(_botToken, _botManagementName).addHandler(_handlers).setWebHookServerData(_webhookServerData);
    }
}
