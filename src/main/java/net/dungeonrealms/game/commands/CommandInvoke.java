package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.party.Affair;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

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
        if (!Rank.isGM(player)) {
            return false;
        }
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("bandittrove")) {
                if (Affair.getInstance().isInParty(player)) {
                	List<Player> list = Affair.getInstance().getParty(player).get().getMembers();
                	list.add( Affair.getInstance().getParty(player).get().getOwner());
                    DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, list, "T1Dungeon");
                }else{
                	DungeonManager.getInstance().createNewInstance(DungeonManager.DungeonType.BANDIT_TROVE, Collections.singletonList(player), "T1Dungeon");
                }
            } else if (args[0].equalsIgnoreCase("varenglade")) {
            }
        }

        return false;
    }
}
