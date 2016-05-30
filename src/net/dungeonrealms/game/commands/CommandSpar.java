package net.dungeonrealms.game.commands;

import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.world.spar.Battle;
import net.dungeonrealms.game.world.spar.Spar;
import net.dungeonrealms.game.world.spar.SparArmor;
import net.dungeonrealms.game.world.spar.SparWeapon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by Nick on 12/15/2015.
 */
public class CommandSpar extends BasicCommand {

    public CommandSpar(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        //if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (args.length > 0) {

            // -> /spar Proxying

            String personToFight = args[0];

            if (Bukkit.getPlayer(personToFight) != null) {
                new Battle(player, new ArrayList<>(), Bukkit.getPlayer(personToFight), new ArrayList<>(), player.getLocation(), Spar.SparWorlds.CYREN_BATTLE, "DUEL_" + (System.currentTimeMillis() / 1000L),
                        SparArmor.TIER_1, SparWeapon.TIER_1, 2323, 0, new ArrayList<>()).start();
            } else {
                player.sendMessage(ChatColor.RED + "That person is offline!");
            }


        }

        return false;
    }
}
