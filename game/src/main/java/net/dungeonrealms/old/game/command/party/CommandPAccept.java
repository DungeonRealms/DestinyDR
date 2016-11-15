package net.dungeonrealms.old.game.command.party;

import net.dungeonrealms.common.frontend.command.BaseCommand;
import net.dungeonrealms.old.game.achievements.Achievements;
import net.dungeonrealms.old.game.party.PartyMechanics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 11/9/2015.
 */
public class CommandPAccept extends BaseCommand {
    public CommandPAccept(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {

        if (s instanceof ConsoleCommandSender) return true;

        Player player = (Player) s;

        if (args.length == 0) {
            if (PartyMechanics._invitations.containsKey(player) && PartyMechanics._invitations.get(player) != null) {
                PartyMechanics._invitations.get(player).getMembers().add(player);
                PartyMechanics._invitations.remove(player);
                player.sendMessage(ChatColor.GREEN + "You have joined the party!");
                Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PARTY_UP);
            } else {
                player.sendMessage(ChatColor.RED + "You do not have any pending invitations!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You must specify the party owners name, like so; /paccept <partyOwnerName>");
        }

        return false;
    }
}
