package net.dungeonrealms.game.command.party;

import com.google.common.collect.Lists;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.affair.party.PLootMenu;
import net.dungeonrealms.game.affair.party.Party;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class CommandPLoot extends BaseCommand {

    public CommandPLoot() {
        super("ploot", "/<command>", "Change party loot mode.", Lists.newArrayList("partyloot"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        Optional<Party> part = Affair.getInstance().getParty(player);

        if (part != null && part.isPresent()) {
            Party party = part.get();
            if (player.equals(party.getOwner())) {
                //Setting the ploot?
                try {
                    new PLootMenu(player, party).open(player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    new PLootMenu(player, party).open(player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                player.sendMessage(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Party Loot Mode: " + ChatColor.WHITE + "");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be in a party to use this command!");
        }
        return false;
    }
}
