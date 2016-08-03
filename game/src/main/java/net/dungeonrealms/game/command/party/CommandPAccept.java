package net.dungeonrealms.game.command.party;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
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
            if (Affair._invitations.containsKey(player) && Affair._invitations.get(player) != null) {
                Affair._invitations.get(player).getMembers().add(player);
                Affair._invitations.remove(player);
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
