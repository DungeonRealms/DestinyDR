package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.party.Affair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11/11/2015.
 */
public class CommandPChat extends BasicCommand {

    public CommandPChat(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (Affair.getInstance().isInParty(player)) {

            if (args.length > 0) {
                StringBuilder message = new StringBuilder();

                for (String rw : args) {
                    message.append(rw).append(" ");
                }

                Affair.AffairO party = Affair.getInstance().getParty(player).get();

                List<Player> everyone = new ArrayList<>();
                {
                    everyone.add(party.getOwner());
                    everyone.addAll(party.getMembers());
                }

                everyone.stream().forEach(player1 -> {
                    player1.sendMessage(ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY + player.getName() + ": " + ChatColor.WHITE + message.toString());
                });
            } else {
                player.sendMessage(ChatColor.RED + "/pchat <message>");
            }

        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
        }

        return false;
    }
}
