package neuralseal.fkrystal.zalobot.impl;

import neuralseal.fkrystal.zalobot.ZaloBot;
import java.util.Collections;
import java.util.List;

public record CallbackContext(ZaloBot bot, List<String> args) {
    public CallbackContext(ZaloBot bot) {
        this(bot, Collections.emptyList());
    }
}