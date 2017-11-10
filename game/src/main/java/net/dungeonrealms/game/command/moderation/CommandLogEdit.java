package net.dungeonrealms.game.command.moderation;

import com.google.common.collect.Lists;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.command.BaseCommand;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class CommandLogEdit extends BaseCommand{
    public CommandLogEdit() {
        super("logedit", "Log Edit command", "Log edit", Lists.newArrayList("le"));
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Rank.isGM((Player) sender) && !(Rank.isPMOD((Player)sender) && DungeonRealms.isMaster())) {
            return true;
        }

        Player pl = (Player) sender;

        if (pl.hasMetadata("logedit")) {
            pl.removeMetadata("logedit", DungeonRealms.getInstance());
            pl.sendMessage(ChatColor.RED + "Log edit disabled!");
        } else {
            pl.setMetadata("logedit", new FixedMetadataValue(DungeonRealms.getInstance(), ""));
            pl.sendMessage(ChatColor.RED + "Log edit enabled!");
        }
        return false;
    }
}
