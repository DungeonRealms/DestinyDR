package net.dungeonrealms.game.command.friend;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.FriendHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by chase on 7/7/2016.
 */
public class AddCommand extends BaseCommand implements CooldownCommand {

    public AddCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        // No friends on the event shard.
        if (DungeonRealms.getInstance().isEventShard) {
            player.sendMessage(ChatColor.RED + "You cannot add friends on this shard.");
            return false;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Invalid usage! You must type: /add <name>");
            return false;
        }

        if(checkCooldown(player))return true;
        //wait 10 seconds between trying to lookup db..
        player.setMetadata("addcmd_cooldown", new FixedMetadataValue(DungeonRealms.getInstance(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10)));

        String playerName = args[0];

        Player friend = Bukkit.getPlayer(playerName);
        if (friend != null) {

            if (GameAPI._hiddenPlayers.contains(friend)) {
                player.sendMessage(ChatColor.RED + "That player is not on any shard!");
                return false;
            }

            if (FriendHandler.getInstance().areFriends(player, friend.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You're already friends.");
                return false;
            }

            FriendHandler.getInstance().sendRequest(player, friend);
            return false;
        }

        SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                return;
            }

            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                if (!wrapper.isPlaying()) {
                    player.sendMessage(ChatColor.RED + "That player is not on any shard!");
                    return;
                }
                if (FriendHandler.getInstance().areFriends(player, uuid)) {
                    player.sendMessage(ChatColor.RED + "You're already friends.");
                    return;
                }
                HashMap<UUID, Integer> pending = wrapper.getPendingFriends();

                if (pending.containsKey(uuid)) {
                    player.sendMessage(ChatColor.RED + "You've already sent this user a friend request.");
                    return;
                }

                FriendHandler.getInstance().sendRequestOverNetwork(player, uuid.toString(), PlayerWrapper.getPlayerWrapper(player).getAccountID());

            });
        });
        return false;
    }

    @Override
    public String getName() {
        return "add";
    }
}
