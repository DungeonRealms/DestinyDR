package net.dungeonrealms.game.command.friend;

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
 * Created by chase on 7/7/2016.
 */
public class DenyCommand extends BaseCommand implements CooldownCommand {

    public DenyCommand(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;


        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Invalid usage! You must type: /deny <name>");
            return false;
        }
        if(checkCooldown(player))return true;
        PlayerWrapper hisWrapper = PlayerWrapper.getPlayerWrapper(player);
        if (hisWrapper == null) return false;

        String name = args[0];

        SQLDatabaseAPI.getInstance().getUUIDFromName(name, false, uuid -> {
            if (uuid == null) {
                player.sendMessage(ChatColor.RED + "This player has never logged into Dungeon Realms");
                return;
            }

            if (!hisWrapper.getPendingFriends().containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "You're not pending a request from that user!");
                return;
            }


            hisWrapper.getPendingFriends().remove(uuid);
            player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + "'s friend request!");
        });
        return false;
    }

    @Override
    public String getName() {
        return "deny";
    }
}
