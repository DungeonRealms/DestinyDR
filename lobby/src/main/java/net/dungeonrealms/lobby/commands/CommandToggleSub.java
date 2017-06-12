package net.dungeonrealms.lobby.commands;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.lobby.Lobby;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 5/31/2017.
 */
public class CommandToggleSub extends BaseCommand {

    public CommandToggleSub() {
        super("togglesub", "/<command>", "Toggle the beta shards sub only mode.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !Lobby.getInstance().isLoggedIn((Player)sender))
            return false;

        if (Rank.isDev((Player) sender) || (sender).isOp()) {
            CommandTest.subOnly = !CommandTest.subOnly;
            sender.sendMessage(CommandTest.subOnly ? "The beta shard is now in sub only mode!" : "The beta shard is now in community mode!");
            return true;
        }
        return false;
    }
}
