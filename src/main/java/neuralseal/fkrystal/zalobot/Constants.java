package neuralseal.fkrystal.zalobot;

import neuralseal.fkrystal.zalobot.utils.VersionUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {
	public static Map<String, ZaloBot> botMap = new ConcurrentHashMap<>();
	public static final String VERSION = VersionUtils.bakeVersion(
			21, 6, 0, "final", 0);
}
