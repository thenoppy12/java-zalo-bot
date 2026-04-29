import neuralseal.fkrystal.zalobot.ZaloBot;
import neuralseal.fkrystal.zalobot.ZaloBotBuilder;
import neuralseal.fkrystal.zalobot.Handler;
import neuralseal.fkrystal.zalobot.handlers.MessageHandler;
import neuralseal.fkrystal.zalobot.WebhookServerData;
import neuralseal.fkrystal.zalobot.ChatActions;
import neuralseal.fkrystal.zalobot.utils.NgrokUtils;

public class Test {
    public static void main(String[] args) {
        String BOT_TOKEN = "get yourself one.";
        String NGROK_TOKEN = "ngrok token for uhhh .-. webhook ?";
        String WEBHOOK_SECRET = "secure webhook token lol";

        int WEBHOOK_SERVER_PORT = 8080;
        String WEBHOOK_SERVER_PATH = "/catchMe";

        Handler messageHandler = new MessageHandler(
                u -> u.message().text() != null,
                (update, context) -> {
                    String chatId = update.message().chat().id();
                    context.bot().api.sendChatAction(chatId, ChatActions.TYPING).join();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    context.bot().api.sendMessage(chatId, "You said: " + update.message().text()).join();
                });

        ZaloBot bot = new ZaloBotBuilder()
                .withToken(BOT_TOKEN)
                .withHandler(messageHandler)
                .withWebhookServerData(new WebhookServerData(WEBHOOK_SERVER_PORT, WEBHOOK_SERVER_PATH, WEBHOOK_SECRET))
                // Bot management name, can be null if not specify.
                .withManagementName("this can be ignored, bot management name will be null and its allowed.")
                .build();

        String NGROK_PUBLISH_URL = NgrokUtils.tunnelHttpToNgrok(NGROK_TOKEN, WEBHOOK_SERVER_PORT);
        System.out.println("Ngrok Tunnel established at: " + NGROK_PUBLISH_URL);

        boolean success = bot.api.setWebhook(NGROK_PUBLISH_URL+WEBHOOK_SERVER_PATH, WEBHOOK_SECRET);
        if (success) {
            System.out.println("\nSETUP COMPLETE! Traffic path: Zalo -> Ngrok -> Java");
        } else {
            System.err.println("\nFailed to set webhook. Check your Bot Token!");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(bot::stop));
        bot.keepMeAlivePlease();
    }
}