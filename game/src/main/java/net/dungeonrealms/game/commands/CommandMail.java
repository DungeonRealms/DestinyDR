package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.handlers.MailHandler;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.player.rank.Rank;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        if (!Rank.isDev(player)) {
            return true;
        }
        if (args.length == 2) {
            if (args[0].equals("send")) {
                if (player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType() != Material.AIR) {
                    if (!player.getName().equals(args[1])) {
                        if (BankMechanics.getInstance().getTotalGemsInInventory(player) >= 5) {
                            if (API.isItemTradeable(player.getEquipment().getItemInMainHand())) {
                                if (MailHandler.getInstance().sendMail(player, args[1], player.getEquipment().getItemInMainHand())) {
                                    player.getEquipment().setItemInMainHand(null);
                                    BankMechanics.getInstance().takeGemsFromInventory(5, player);
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "This item cannot be sent via mail.");
                            }
                        } else {
                            player.sendMessage(ChatColor.RED + "There is a " + ChatColor.UNDERLINE + "5 GEM" + ChatColor.RESET + ChatColor.RED + " fee to send mail.");
                            return true;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You cannot send mail to yourself.");
                    }
                } else {
                    return true;
                }
            }
            return true;
        }

        PlayerMenus.openMailInventory(player);

        return true;

    }
}
