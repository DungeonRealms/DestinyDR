package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class CommandOreEdit extends BaseCommand {
    public CommandOreEdit() {
        super("oreedit", "Ore Edit command", "Ore edit", Lists.newArrayList("oe"));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Rank.isGM((Player) sender)) {
            return true;
        }

        Player pl = (Player) sender;

        if (pl.hasMetadata("oreedit")) {
            pl.removeMetadata("oreedit", DungeonRealms.getInstance());
            pl.sendMessage(ChatColor.RED + "Ore edit disabled!");
        } else {
            pl.setMetadata("oreedit", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
            pl.sendMessage(ChatColor.RED + "Ore edit enabled!");
        }
        return false;
    }
}
