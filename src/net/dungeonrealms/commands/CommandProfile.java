package net.dungeonrealms.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.inventory.GUI;
import net.dungeonrealms.mastery.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 9/26/2015.
 */
public class CommandProfile implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {

        if (s instanceof ConsoleCommandSender) return false;

        Player player = (Player) s;

        GUI profileMain = new GUI("Profile", 27, event -> {
                event.setWillClose(false);
        }, DungeonRealms.getInstance())
                .setOption(4, Utils.getPlayerHead(player), ChatColor.GREEN + player.getName() + "'s Profile", new String[]{
                        ChatColor.DARK_GRAY + "Player Profile"
                })
                .setOption(0, new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Attributes", new String[]{
                        ChatColor.DARK_GRAY + "Player Attributes",
                        "",
                        ChatColor.GRAY + "As you play throughout Dungeon Realms,",
                        ChatColor.GRAY + "your player will acquire attribute points.",
                        ChatColor.GRAY + "With Attribute points you can improve",
                        ChatColor.GRAY + "several of many individual character",
                        ChatColor.GRAY + "skills!",
                        "",
                        ChatColor.YELLOW + "Click to view Player Attributes!"
                })
                .setOption(8, new ItemStack(Material.NAME_TAG), ChatColor.GREEN + "Pets", new String[]{
                        ChatColor.DARK_GRAY + "Player Pet",
                        "",
                        ChatColor.GRAY + "Want a friendly companion",
                        ChatColor.GRAY + "to bring along on your",
                        ChatColor.GRAY + "adventures? Pets are the",
                        ChatColor.GRAY + "solution!",
                        "",
                        ChatColor.YELLOW + "Click to view Player Pets!"
                })
                .setOption(18, new ItemStack(Material.EMERALD), ChatColor.GREEN + "Donations", new String[]{
                        ChatColor.DARK_GRAY + "Micro Transactions",
                        "",
                        ChatColor.GRAY + "Want to access to more awesomeness?",
                        ChatColor.GRAY + "Consider donating to support Dungeon Realms",
                        ChatColor.GRAY + "in return you'll receive several in-game",
                        ChatColor.GRAY + "perks!",
                        "",
                        ChatColor.YELLOW + "Click to view Micro Transactions!"
                })
                .setOption(26, new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GREEN + "Settings & Preferences", new String[]{
                        ChatColor.DARK_GRAY + "Settings & Preferences",
                        "",
                        ChatColor.GRAY + "Help us help you! By adjusting",
                        ChatColor.GRAY + "your player settings & preferences",
                        ChatColor.GRAY + "we can optimize your gaming experience",
                        ChatColor.GRAY + "on this RPG!",
                        "",
                        ChatColor.YELLOW + "Click to view Settings & Preferences!"
                });

        profileMain.open(player);


        return false;
    }
}
