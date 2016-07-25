package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.game.handlers.FriendHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Alan on 7/25/2016.
 */
public class CommandReply extends BasicCommand {

    public CommandReply(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (PunishAPI.isMuted(player.getUniqueId())) {
            player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return true;
        }

        GamePlayer gp = GameAPI.getGamePlayer(player);
        if (gp == null) return true;

        if (gp.getLastMessager() == null) {
            player.sendMessage(ChatColor.RED + "No player has messaged you recently.");
            return true;
        }

        FriendHandler.sendMessageToFriend(player, gp.getLastMessager(), args[0]);

        return true;
    }
}
