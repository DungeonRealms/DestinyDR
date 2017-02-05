package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.menu.ShardSwitcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static net.dungeonrealms.GameAPI.handleLogout;
import static net.dungeonrealms.GameAPI.submitAsyncCallback;

/**
 * Created by Brad on 09/06/2016.
 */

public class CommandShard extends BaseCommand {

    public CommandShard(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;

        if (args.length == 0 || !Rank.isGM(player)) {

            if (Chat.listened(player)) {
                player.sendMessage(ChatColor.RED + "You cannot /shard while interacting with chat.");
                player.sendMessage(ChatColor.GRAY + "Please finish what you were doing and try again.");
                return true;
            }
            submitAsyncCallback(() -> new ShardSwitcher(player), menu -> {
                try {
                    player.closeInventory();
                    menu.get().open(player);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } else {
            ShardInfo shardInfo = ShardInfo.getByPseudoName(args[0]);
            
            if(shardInfo == null){
            	sender.sendMessage(ChatColor.RED + "Shard Not Found!");
            	return true;
            }

            GameAPI.sendToShard(player, shardInfo);

        }

        return true;
    }

}
