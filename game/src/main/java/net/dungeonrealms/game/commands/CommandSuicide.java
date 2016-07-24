package net.dungeonrealms.game.commands;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.player.chat.Chat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandSuicide extends BasicCommand {

    public CommandSuicide(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player p;
        if (sender instanceof Player) {
            p = (Player) sender;
        } else {
            return true;
        }

        if (GameAPI.getGamePlayer(p) == null) {
            p.sendMessage(ChatColor.RED + "You cannot commit suicide at the moment.");
            return true;
        }


        if (!GameAPI.getGamePlayer(p).isAbleToDrop()) {
            p.sendMessage(ChatColor.RED + "You cannot commit suicide at the moment.");
            return true;
        }

        if (!GameAPI.getGamePlayer(p).isAbleToSuicide()) {
            p.sendMessage(ChatColor.RED + "You cannot commit suicide at the moment.");
            return true;
        }


        p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.GRAY + "This " +
                "command will KILL you, you will LOSE everything you are carrying. If you are sure, type '" +
                ChatColor.GREEN.toString() + ChatColor.BOLD + "Y" + ChatColor.GRAY + "', if not, type '" + ChatColor
                .RED.toString() + "cancel" + ChatColor.RED + "'.");

        Chat.listenForMessage(p, event -> {
            if (GameAPI.getGamePlayer(p) == null || !GameAPI.getGamePlayer(p).isAbleToDrop()) {
                p.sendMessage(ChatColor.RED + "You cannot commit suicide at the moment.");
                return;
            }
            if (event.getMessage().equalsIgnoreCase("y")) {
                HealthHandler.getInstance().handlePlayerDeath(p, null);
            } else {
                p.sendMessage(ChatColor.YELLOW + "Suicide - " + ChatColor.BOLD + "CANCELLED");
            }
        }, null);
        return true;
    }

}
