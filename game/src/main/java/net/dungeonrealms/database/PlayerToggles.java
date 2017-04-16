package net.dungeonrealms.database;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class PlayerToggles implements LoadableData, SaveableData {

    @Getter
    @Setter
    private boolean debug, trade, tradeChat, globalChat, receiveMessage, pvp, duel, chaoticPrevention, soundTrack, tips, glow, damageIndicators, vanish;

    @SneakyThrows
    public void extractData(ResultSet set) {
        debug = set.getBoolean("toggles.debug_enabled");
        trade = set.getBoolean("toggles.trading_enabled");
        tradeChat = set.getBoolean("toggles.trade_chat_enabled");
        globalChat = set.getBoolean("toggles.default_global_chat");
        receiveMessage = set.getBoolean("toggles.pms_enabled");
        pvp = set.getBoolean("toggles.pvp_enabled");
        duel = set.getBoolean("toggles.dueling_enabled");
        chaoticPrevention = set.getBoolean("toggles.chaotic_prevention_enabled");
        soundTrack = set.getBoolean("toggles.sound_enabled");
        tips = set.getBoolean("toggles.tips_enabled");
        glow = set.getBoolean("toggles.glowEnabled");
        damageIndicators = set.getBoolean("toggles.dmgIndicators");
        vanish = set.getBoolean("toggles.vanish");
    }

    public String getUpdateStatement() {
        return String.format("UPDATE toggles SET debug_enabled = '%s', trading_enabled = '%s', trade_chat_enabled = '%s', default_global_chat = '%s', " +
                        "pms_enabled = '%s', pvp_enabled = '%s', dueling_enabled = '%s', chaotic_prevention_enabled = '%s', sound_enabled = '%s'," +
                        "tips_enabled = '%s', glowEnabled = '%s', dmgIndicators = '%s', vanish = '%s' " +
                        "WHERE character_id = '%s';",
                debug, trade, tradeChat, globalChat, receiveMessage, pvp, duel, chaoticPrevention, soundTrack, tips, glow, damageIndicators, vanish);
    }
}
