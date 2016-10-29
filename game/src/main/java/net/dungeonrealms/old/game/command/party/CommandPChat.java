package net.dungeonrealms.old.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.old.game.party.PartyMechanics;
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

        if (PartyMechanics.getInstance().isInParty(player)) {
            if (args.length > 0) {
                StringBuilder message = new StringBuilder();

                for (String rw : args) {
                    message.append(rw).append(" ");
                }

                PartyMechanics.getInstance().sendPartyChat(player, message.toString());
            } else {
                PartyMechanics.getInstance().togglePartyChat(player);
            }
        } else {
            player.sendMessage(ChatColor.RED + "You are not in a party!");
        }

        return false;
    }
}
