package neuralseal.fkrystal.zalobot;

import java.util.ArrayList;
import java.util.List;


/**
 * Builder cho {@link ZaloBot}, hẹ hẹ
 */
public class ZaloBotBuilder {
    private String _botManagementName = null;
    private String _botToken;
    private final List<Handler> _handlers =  new ArrayList<>();
    private WebhookServerData _webhookServerData;


    /**
     * Đặt tên bot, để dễ quản lý trong code
     * @param botManagementName Tên bot
     * @return {@link ZaloBotBuilder} hiện tại
     */
    public ZaloBotBuilder withManagementName(String botManagementName) {
        this._botManagementName = botManagementName;
        return this;
    }

    /**
     * Set the bot token
     * @param botToken Valid bot token
     * @return {@link ZaloBotBuilder} hiện tại
     */
    public ZaloBotBuilder withToken(String botToken) {
        this._botToken = botToken;
        return this;
    }

    /**
     * Thêm handler cho bot
     * @param handler Class được implement {@link Handler}
     * @return {@link ZaloBotBuilder} hiện tại
     */
    public ZaloBotBuilder withHandler(Handler handler) {
        this._handlers.add(handler);
        return this;
    }

    /**
     * Đưa data cho webhook server
     * @param data {@link WebhookServerData}
     * @return {@link ZaloBotBuilder} hiện tại
     */
    public ZaloBotBuilder withWebhookServerData(WebhookServerData data) {
        this._webhookServerData = data;
        return this;
    }

    /**
     * Build bot
     * @return {@link ZaloBot} đã tinh chỉnh theo builder
     */
    public ZaloBot build() {
        if (_botToken == null) throw new NullPointerException("Bot token is null. Specify a bot token for this.");
        return new ZaloBot(_botToken, _botManagementName).addHandler(_handlers).setWebHookServerData(_webhookServerData);
    }
}