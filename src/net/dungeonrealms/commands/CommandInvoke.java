package net.dungeonrealms.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.mechanics.DungeonManager;
import net.dungeonrealms.party.Party;

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

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("bandittrove")) {
                if (Party.getInstance().isInParty(player)) {
                    List<Player> MEMBERS = Party.getInstance().getPlayerParty(player).getMembers();
                    MEMBERS.add(Party.getInstance().getPlayerParty(player).getOwner());
                    DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, MEMBERS);
                    return true;
                }
                DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, Collections.singletonList(player));
            }
        }

        return false;
    }
}
