package neuralseal.fkrystal.zalobot.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VersionUtils {
    public static String bakeVersion(int major, int minor, int micro, String releaseLevel, int serial) {
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
