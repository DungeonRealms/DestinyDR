package net.dungeonrealms.game.command.friend;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by chase on 7/8/2016.
 */
public class RemoveCommand extends BaseCommand implements CooldownCommand {

    public RemoveCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Invalid usage! You must type: /unfriend <name>");
            return false;
        }

        if(checkCooldown(player))return true;
        String name = args[0];

        PlayerWrapper hisWrapper = PlayerWrapper.getPlayerWrapper(player);
        if (hisWrapper == null) return false;

        SQLDatabaseAPI.getInstance().getUUIDFromName(name, false, (uuid) -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms!");
                return;
            }
            if (!hisWrapper.getFriendsList().containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "You are not friends with this person!");
                return;
            }

            PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
                //DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIENDS, uuid.toString(), true);
                hisWrapper.getFriendsList().remove(uuid);
                player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + " from your friends list!");

                if (wrapper.isPlaying() && wrapper.getShardPlayingOn() != null) {
                    //send bungee message instead..
                    GameAPI.sendNetworkMessage("Friends", "removeFriend:" + player.getUniqueId().toString() +
                            ":" + player.getName() + ":" + uuid.toString());
                } else {
                    wrapper.getFriendsList().remove(player.getUniqueId());
                    wrapper.saveFriends(false, null);
                }
            });
        });


        return false;
    }

    @Override
    public String getName() {
        return "remove";
    }
}
