package net.dungeonrealms.commands;

import net.dungeonrealms.inventory.Menu;
import net.dungeonrealms.mail.Mail;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/15/2015.
 */
public class CommandMail implements CommandExecutor {

    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (args.length > 0) {
            if (args[0].equals("send")) {
                assert args[1] != null : "args[1] is null!?";
                assert player.getItemInHand() != null && player.getItemInHand().getType() != null && player.getItemInHand().getType() != Material.AIR : "Somethings wrong!";
                Mail.getInstance().sendMail(player, args[1], player.getItemInHand());

            }
            return true;
        }

        Menu.openMailInventory(player);

        return true;

    }
}
