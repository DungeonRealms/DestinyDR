package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.player.Rank;
import net.dungeonrealms.game.player.menu.ShardSwitcher;
import net.dungeonrealms.network.bungeecord.BungeeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.dungeonrealms.GameAPI.handleLogout;

/**
 * Created by Brad on 09/06/2016.
 */

public class CommandShard extends BasicCommand {

    public CommandShard(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 0 || !Rank.isGM(player)) {
            new ShardSwitcher(player).open(player);
            return true;
        }


        if (args.length > 0) {
            GameAPI.IGNORE_QUIT_EVENT.add(player.getUniqueId());
            GameAPI.submitAsyncCallback(() -> handleLogout(player.getUniqueId(), false),
                    consumer -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.BOLD + ChatColor.UNDERLINE + args[0] + ChatColor.YELLOW + "...");

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                                () -> BungeeUtils.sendToServer(player.getName(), args[0]), 10);
                    }));

        }

        return true;
    }

}
