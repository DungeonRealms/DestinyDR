package net.dungeonrealms.commands;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mechanics.DungeonManager;
import net.dungeonrealms.party.Affair;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Created by Nick on 10/20/2015.
 */
public class CommandInvoke extends BasicCommand {
    public CommandInvoke(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "[WARNING] " + ChatColor.YELLOW + "You do not have permissions for this!");
            return false;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("bandittrove")) {
                if (Affair.getInstance().isInParty(player)) {
                }
                DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, Collections.singletonList(player));
            } else if (args[0].equalsIgnoreCase("varenglade")) {
            }
        }

        return false;
    }
}
