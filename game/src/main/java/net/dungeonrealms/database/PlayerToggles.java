package net.dungeonrealms.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;

public class PlayerToggles implements LoadableData, SaveableData {

	private PlayerWrapper wrapper;
	private Map<Toggles, Boolean> toggles = new HashMap<>();

    public PlayerToggles(PlayerWrapper wrapper){
        this.wrapper = wrapper;
    }

    public boolean getState(Toggles t) {
    	return toggles.get(t) && wrapper.getRank().isAtLeast(t.getMinRank());
    }

    public void setState(Toggles t, boolean b) {
    	toggles.put(t, b);
    }

    public void toggle(Toggles t) {
    	boolean newState = !getState(t);
    	wrapper.getPlayer().sendMessage((newState? ChatColor.GREEN : ChatColor.RED) + t.getDisplayName() + " - " + ChatColor.BOLD + (newState ? "ENABLED" : "DISABLED"));
    	setState(t, newState);
    }

    @SneakyThrows
    public void extractData(ResultSet set) {
        for (Toggles t : Toggles.values())
        	if (t.isSaved())
        		setState(t, set.getBoolean("toggles." + t.getDBField()));
    }

    public String getUpdateStatement() {
    	String sql = "UPDATE toggles SET ";
    	for (Toggles t : Toggles.values())
    		if (t.isSaved())
    			sql += (t == Toggles.values()[0] ? "" : ", ") + t.getDBField() + " = '" + (getState(t) ? 1 : 0) + "'";
    	return sql + " WHERE account_id = '" + wrapper.getAccountID() + "';";
    }

    @AllArgsConstructor
    public enum Toggles {
    	DEBUG("debug_enabled", "Toggles displaying combat debug messages", "Debug Messages"),
    	TRADE("trading_enabled", "Toggles trading requests.", "Trade"),
    	TRADE_CHAT("trade_chat_enabled", "tradechat", "Toggles receiving <T>rade chat.", "Trade Chat"),
    	GLOBAL_CHAT("default_global_chat", "globalchat", "Toggles talking only in global chat.", "Global Chat"),
    	ENABLE_PMS("pms_enabled", "tells", "Toggles receiving NON-BUD /tell.", "NON-BUD Private Messages."),
    	PVP("pvp_enabled", "Toggles all outgoing PvP damage (anti-neutral).", "Outgoing PvP Damage"),
    	DUEL("dueling_enabled", "Toggles dueling requests", "Dueling Requests"),
    	CHAOTIC_PREVENTION("chaotic_prevention_enabled", "chaos", "Toggles killing blows on lawful players (anti-chaotic).", "Anti-Chaotic"),
    	//SOUNDTRACK("sound", "Toggles the DungeonRealms Soundtrack.", "Soundtrack"),
    	TIPS("tips_enabled", "Toggles the receiving of informative tips.", "Tip Display."),
    	GLOW("glowEnabled", "Toggles rare items glowing.", "Item Glow"),
    	DAMAGE_INDICATORS("dmgIndicators", "floatdamage", "Toggles floating damage values.", "Damage Indicators"),

    	GUILD_CHAT("guild_chat", "guildchat", "Toggles talking only in guild chat.", "Guild Chat", false),

    	VANISH("vanish", "Toggles your vanish-status.", "Vanish", PlayerRank.TRIALGM);

    	@Getter
    	private String columnName;
    	private String commandName;
    	@Getter private String description;
    	@Getter private String displayName;
    	@Getter private PlayerRank minRank;
    	@Getter private boolean saved;

    	Toggles(String columnName, String description, String display) {
    		this(columnName, description, display, PlayerRank.DEFAULT);
    	}

    	Toggles(String columnName, String description, String display, PlayerRank rank) {
    		this(columnName, null, description, display, rank, true);
    	}

    	Toggles(String columnName, String cmd, String description, String display) {
    		this(columnName, cmd, description, display, true);
    	}

    	Toggles(String columnName, String cmd, String description, String display, boolean save) {
    		this(columnName, cmd, description, display, PlayerRank.DEFAULT, save);
    	}

    	public String getCommand() {
    		return "toggle" + (commandName == null ? getDBField() : commandName);
    	}

    	public String getDBField() {
    		return columnName;
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
