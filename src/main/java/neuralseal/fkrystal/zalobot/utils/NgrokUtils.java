package neuralseal.fkrystal.zalobot.utils;

import com.ngrok.Session;
import neuralseal.fkrystal.zalobot.WebhookServerData;

import java.io.IOException;
import java.net.URL;

public class NgrokUtils {
    /**
     * Dùng ngrok làm tunnel để zalo đưa tin nhắn về để bot xử lý<br>
     * (Dùng khi nhà bạn không mở được port, hoặc mạng nhà bạn bị nhà mạng NAT khu vực)
     * @param ngrokAuthToken Ngrok token, lấy ở <a href="https://dashboard.ngrok.com/get-started/your-authtoken">đây</a>
     * @param port Port của webhook server sẽ chạy, được xác định bằng record {@link WebhookServerData}{@code .port()}
     * @return Link tunnel được ngrok cho
     */
    public static String tunnelHttpToNgrok(String ngrokAuthToken, int port) {
        try {
            Session session = Session.withAuthtoken(ngrokAuthToken).connect();
            return session.httpEndpoint().forward(new URL("http://localhost:" + port)).getUrl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}