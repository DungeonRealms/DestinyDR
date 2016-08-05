package net.dungeonrealms.game.tab.filler;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */

public class GuildFiller extends Variable {

    public GuildFiller() {
        super("fillguild");
    }

    @Override
    public String getReplacement(Player player) {
        return ChatColor.GRAY + "Please visit the Guild{newline}" + ChatColor.GRAY + "Registrar to start a guild!";
    }
}
