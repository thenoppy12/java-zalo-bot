package neuralseal.fkrystal.zalobot.utils;

import com.ngrok.Session;

import java.io.IOException;
import java.net.URL;

public class NgrokUtils {
    private static Session session;
    public static String tunnelHttpToNgrok(String ngrokAuthToken, int port) {
        try {
            session = Session.withAuthtoken(ngrokAuthToken).connect();
            return session.httpEndpoint().forward(new URL("http://localhost:" + port)).getUrl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
