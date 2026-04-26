package neuralseal.fkrystal.zalobot;

import neuralseal.fkrystal.zalobot.utils.VersionUtils;
import okhttp3.OkHttpClient;
import java.util.concurrent.TimeUnit;

public class Constants {
	public static final OkHttpClient sharedOkHttp = new OkHttpClient.Builder().readTimeout(65, TimeUnit.SECONDS).build();
	public static final String VERSION = VersionUtils.bakeVersion(
			21, 6, 0, "final", 0);
}
