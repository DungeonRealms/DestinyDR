package net.dungeonrealms.common.game.database.player;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;

@AllArgsConstructor
public enum PlayerRank {
    DEFAULT("", ChatColor.GRAY, "Default"),
    SUB("S", ChatColor.GREEN, "Subscriber"),
    SUB_PLUS("S+", ChatColor.GOLD, "Subscriber+"),
    SUB_PLUS_PLUS("S++",ChatColor.YELLOW, "Subscriber++"),
    BUILDER("BLD", ChatColor.DARK_GREEN, "Builder"),
    PARTNER("PTNR", ChatColor.DARK_GREEN, "Partner"),
    YOUTUBER("YT", ChatColor.RED, "YouTuber"),
    HIDDEN_MOD("S+", ChatColor.GREEN, "Subscriber"),
    PMOD("PMOD", ChatColor.WHITE, "WHITE", "Player Moderator",
            "nocheatplus.notify", "nocheatplus.command.notify", "nocheatplus.command.info", "nocheatplus.command.inspect"),
    TRIALGM("GM", ChatColor.AQUA, "LIGHT_BLUE", "Trial Game Master",
            "nocheatplus.checks", "nocheatplus.bypass.denylogin", "essentials.*", "nocheatplus.bypass.denylogin", "bukkit.command.teleport", "minecraft.command.tp"),
    GM("GM", ChatColor.AQUA, "LIGHT_BLUE", "Game Master", "bukkit.command.kick"),
    CM("CM", ChatColor.AQUA, "Community Manager"),
    HEADGM("GM", ChatColor.AQUA, "Head Game Master"),
    SUPPORT("SUPPORT", ChatColor.BLUE, "Support Agent"),
    DEV("DEV", ChatColor.DARK_AQUA, "PURPLE", "Developer", "bukkit.command.*");

    private String prefix;
    @Getter private ChatColor chatColor;
    @Getter private String dyeColor; // This is a string because we can't include Bukkit code on bungee-shared resources.
    private String displayPrefix;
    @Getter private String[] perms;
    
    PlayerRank(String prefix, ChatColor color, String fullPrefix, String... perms) {
    	this(prefix, color, "GRAY", fullPrefix, perms);
    }
    
    public boolean isSUB() {
    	return isAtLeast(PlayerRank.SUB);
    }
    
    public boolean isSubPlus() {
    	return isAtLeast(PlayerRank.SUB_PLUS);
    }
    
    public boolean isLifetimeSUB() {
    	return isAtLeast(PlayerRank.SUB_PLUS_PLUS);
    }
    
    public int getRank() {
    	return ordinal();
    }
    
    public String getInternalName() {
    	return name().toLowerCase();
    }
    
    /**
     * Get the prefix that displays in chat.
     * @return
     */
    public String getChatPrefix() {
    	return this.prefix.length() > 0 ? getPrefix() + " " : "";
    }
    
    /**
     * Gets this rank's prefix without an added .
     * @return
     */
    public String getPrefix() {
    	return getChatColor() + "" + ChatColor.BOLD + this.prefix + ChatColor.RESET;
    }
    
    /**
     * Get the full display prefix.
     */
    public String getFullPrefix() {
    	return getChatColor() + this.displayPrefix;
    }

    public boolean isAtLeast(PlayerRank rank){
        return getRank() >= rank.getRank();
    }
    
    public static PlayerRank getFromInternalName(String name) {
        if(name == null)return null;
        return Arrays.stream(values()).filter(rank -> rank.getInternalName().equals(name.toLowerCase())).findFirst().orElse(null);
    }
    
    public static PlayerRank getFromPrefix(String prefix) {
    	for (PlayerRank p : values())
    		if (p.getPrefix().equalsIgnoreCase(prefix))
    			return p;
    	return getFromInternalName(prefix);
    }
}
