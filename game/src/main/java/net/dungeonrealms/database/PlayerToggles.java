package net.dungeonrealms.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerToggles implements LoadableData, SaveableData {

    private PlayerWrapper wrapper;
    private Map<Toggles, Boolean> toggles = new HashMap<>();

    public PlayerToggles(PlayerWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public boolean getState(Toggles t) {
        Boolean bool = toggles.get(t);
        return bool != null && bool && wrapper.getRank().isAtLeast(t.getMinRank());
    }

    public void setState(Toggles t, boolean newState) {
        if (!wrapper.getRank().isAtLeast(t.getMinRank()))
            return; // Somehow a player is toggling a togled they don't have access to?

        toggles.put(t, newState);

        // Some special cases :/
        Player player = wrapper.getPlayer();
        if (player == null)
            return; // They're logging in. This'll be called again once they exist in the world.

        if (t == Toggles.VANISH) {
            GameAPI._hiddenPlayers.remove(player);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            GameAPI.sendNetworkMessage("vanish", player.getUniqueId().toString(), String.valueOf(newState));
            player.setCustomNameVisible(!newState);

            if (newState) {
                GameAPI._hiddenPlayers.add(player);
                Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(player));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1));
            } else {
                Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(player));
            }

            player.setGameMode(newState ? GameMode.SPECTATOR : GameMode.CREATIVE);
        } else if (t == Toggles.INVULNERABLE) {
            if (newState) {
                EntityAPI.untargetEntity(player, 30); // Untarget player.
                CombatLog.removeFromPVP(player);
                CombatLog.removeFromCombat(player);
            }
        } else if (t == Toggles.STREAM) {
            player.performCommand("ncp notify " + (newState ? "off" : "on")); // Toggle NCP notifications.
            for (int i = 0; i < 50; i++)
                player.sendMessage(""); // Clear chat
        }
    }

    public void toggle(Toggles t) {
        boolean newState = !getState(t);
        wrapper.getPlayer().sendMessage((newState ? ChatColor.GREEN : ChatColor.RED) + t.getDisplayName() + " - " + ChatColor.BOLD + (newState ? "ENABLED" : "DISABLED"));
        setState(t, newState);
    }

    @SneakyThrows
    public void extractData(ResultSet set) {
        for (Toggles t : Toggles.values()) {
            if (t.isSaved() && wrapper.getRank().isAtLeast(t.getMinRank())) {
                String db = "toggles." + t.getDBField();
                setState(t, SQLDatabaseAPI.hasColumn(set, db) ? set.getBoolean(db) : t.isToggledByDefault());
            }
        }
    }

    public String getUpdateStatement() {
        String sql = "UPDATE toggles SET ";
        for (Toggles t : Toggles.values())
            if (t.isSaved())
                sql += (t == Toggles.values()[0] ? "" : ", ") + t.getDBField() + " = '" + (getState(t) ? 1 : 0) + "'";
        return sql + " WHERE account_id = '" + wrapper.getAccountID() + "';";
    }

    @AllArgsConstructor
    @Getter
    public enum Toggles {
        DEBUG("debug_enabled", "debug", "Toggles displaying combat debug messages", "Debug Messages", true),
        TRADE("trading_enabled", "trade", "Toggles trading requests.", "Trade", true),
        TRADE_CHAT("trade_chat_enabled", "tradechat", "Toggles receiving <T>rade chat.", "Trade Chat"),
        GLOBAL_CHAT("default_global_chat", "globalchat", "Toggles talking only in global chat.", "Global Chat"),
        ENABLE_PMS("pms_enabled", "tells", "Toggles receiving NON-BUD /tell.", "NON-BUD Private Messages.", true),
        PVP("pvp_enabled", "pvp", "Toggles all outgoing PvP damage (anti-neutral).", "Outgoing PvP Damage"),
        DUEL("dueling_enabled", "duel", "Toggles dueling requests", "Dueling Requests", true),
        CHAOTIC_PREVENTION("chaotic_prevention_enabled", "chaos", "Toggles killing blows on lawful players (anti-chaotic).", "Anti-Chaotic"),
        //SOUNDTRACK("sound", "Toggles the DungeonRealms Soundtrack.", "Soundtrack"),
        TIPS("tips_enabled", "tips", "Toggles the receiving of informative tips.", "Tip Display", true),
        GLOW("glowEnabled", "glow", "Toggles rare items glowing.", "Item Glow", true),
        HOLOGRAMS("dmgIndicators", "indicators", "Toggles floating damage values.", "Damage Holograms", true),

        GUILD_CHAT("guild_chat", "guildchat", "Toggles talking only in guild chat.", "Guild Chat", false, false),

        // Staff Toggles
        STREAM("stream", "Prevent sensitive messages from displaying.", "Stream Mode", PlayerRank.PMOD),
        VANISH("vanish", "Vanish to players and mobs.", "Vanish", PlayerRank.TRIALGM),
        INVULNERABLE("god", "Allow incoming damage.", "Invincibility", PlayerRank.TRIALGM);

        private String columnName;
        private String commandName;
        private String description;
        private String displayName;
        private PlayerRank minRank;
        private boolean toggledByDefault;
        private boolean saved;

        Toggles(String columnName, String description, String display) {
            this(columnName, description, display, PlayerRank.DEFAULT);
        }

        Toggles(String columnName, String description, String display, PlayerRank rank) {
            this(columnName, null, description, display, rank, false, true);
        }

        Toggles(String columnName, String cmd, String description, String display) {
            this(columnName, cmd, description, display, true);
        }

        Toggles(String columnName, String cmd, String description, String display, boolean save) {
            this(columnName, cmd, description, display, PlayerRank.DEFAULT, false, save);
        }

        Toggles(String columnName, String cmd, String description, String display, boolean defaultState, boolean save) {
            this(columnName, cmd, description, display, PlayerRank.DEFAULT, defaultState, save);
        }

        public String getCommand() {
            return "toggle" + (commandName == null ? getDBField() : commandName);
        }

        public String getDBField() {
            return columnName;
        }

        public DyeColor getDye(boolean toggled) {
            return toggled ? DyeColor.LIME : DyeColor.valueOf(getMinRank().getDyeColor());
        }

        public static List<Toggles> getToggles(Player player) {
            List<Toggles> toggles = new ArrayList<>();
            PlayerRank rank = Rank.getRank(player);
            for (Toggles t : values())
                if (rank.isAtLeast(t.getMinRank()))
                    toggles.add(t);
            return toggles;
        }
    }
}
