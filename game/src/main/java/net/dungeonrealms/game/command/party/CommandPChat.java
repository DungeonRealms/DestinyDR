package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by Nick on 11/11/2015.
 */
public class CommandPChat extends BaseCommand {

    public CommandPChat(String command, String usage, String description, List<String> aliases) {
        super(command, usage, description, aliases);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        if (Affair.getInstance().isInParty(player)) {

            if (args.length == 0) {

                return true;
            }

            if (args.length > 0) {
                StringBuilder message = new StringBuilder();

                for (String rw : args) {
                    message.append(rw).append(" ");
                }

                Affair.getInstance().sendPartyChat(player, message.toString());
            } else {
                Affair.getInstance().togglePartyChat(player);
            }

        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
        }

        return false;
    }
}
