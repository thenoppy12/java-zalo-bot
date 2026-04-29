package neuralseal.fkrystal.zalobot;

import okhttp3.OkHttpClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Constants {
	protected static final OkHttpClient sharedOkHttp = new OkHttpClient.Builder().readTimeout(65, TimeUnit.SECONDS).build();
	public static final String VERSION = bakeVersion(
			21, 6, 0, "final", 0);

	private static String bakeVersion(int major, int minor, int micro, String releaseLevel, int serial) {
		String version = major + "." + minor;
		if (micro != 0) {
			version += "." + micro;
		}
		if (!releaseLevel.equals("final")) {
			version += getReleaseLevel(releaseLevel) + serial;
		}
		return version;
	}
	private static String getReleaseLevel(String releaseLevel) {
		Map<String,String> map = new ConcurrentHashMap<>();
		map.put("alpha", "a");
		map.put("beta", "b");
		map.put("candidate", "rc");
		return map.get(releaseLevel);
	}
}