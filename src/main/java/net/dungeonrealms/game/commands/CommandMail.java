package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/15/2015.
 */
public class CommandMail extends BasicCommand {

    public CommandMail(String command, String usage, String description) {
        super(command, usage, description);
    }
    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (args.length == 2) {
            if (args[0].equals("send")) {
                /*if (args[1].equalsIgnoreCase("a")) {
                    Bukkit.getOnlinePlayers().stream().forEach(player1 -> MailHandler.getInstance().sendMail(player, player1.getName(), player.getItemInHand()));
                }*/
                if (MailHandler.getInstance().sendMail(player, args[1], player.getItemInHand())) {
                    player.setItemInHand(null);
                }
            }
            return true;
        }

        PlayerMenus.openMailInventory(player);

        return true;

    }
}
