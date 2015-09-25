package net.dungeonrealms.mechanics;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.listeners.GUIListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 9/18/2015.
 */
public class PlayerManager {
    public static void checkInventory(Player player) {
        player.getInventory().setItem(7, ItemManager.createHearthStone("HearthStone", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Back to your hearthstone location!"
        }));
        player.getInventory().setItem(8, ItemManager.getPlayerProfile(player, "Player Profile", new String[]{
                ChatColor.GRAY + "(Right-Click) " + ChatColor.AQUA + "Open your profile!"
        }));
    }

    public static void createProfileGUI(Player player) {
        GUIListener menu = new GUIListener(player.getName() + "'s Profile", 27, event -> {
            int i = event.getPosition();
            event.setWillClose(false);
            switch (i) {
                case 19:
                    break;
                default:
            }
        }, DungeonRealms.getInstance())
                .setOption(4, new ItemStack(Material.EXP_BOTTLE), ChatColor.GREEN + "Stats", new String[]{
                        ChatColor.DARK_GRAY + "Player Stats",
                        "",
                        ChatColor.GRAY + "As you play throughout DungeonRealms,",
                        ChatColor.GRAY + "your player will acquire stat points.",
                        ChatColor.GRAY + "With Attribute points you can improve",
                        ChatColor.GRAY + "several of many individual character",
                        ChatColor.GRAY + "skills!",
                        "",
                        ChatColor.YELLOW + "Click to view Player Stats!"
                })
                .setOption(12, new ItemStack(Material.NAME_TAG), ChatColor.GREEN + "Pets", new String[]{
                        ChatColor.DARK_GRAY + "Player Pet",
                        "",
                        ChatColor.GRAY + "Want a friendly companion",
                        ChatColor.GRAY + "to bring along on your",
                        ChatColor.GRAY + "adventures? Pets are the",
                        ChatColor.GRAY + "solution!",
                        "",
                        ChatColor.YELLOW + "Click to view Player Pets!"
                })
                .setOption(14, new ItemStack(Material.SADDLE), ChatColor.GREEN + "Mounts", new String[]{
                        ChatColor.DARK_GRAY + "Player Mount",
                        "",
                        ChatColor.GRAY + "Want to travel in style?",
                        ChatColor.GRAY + "Ride everywhere on your",
                        ChatColor.GRAY + "own private mount!",
                        "",
                        ChatColor.YELLOW + "Click to view Player Pets!"
                })
                .setOption(20, new ItemStack(Material.EMERALD), ChatColor.GREEN + "Donations", new String[]{
                        ChatColor.DARK_GRAY + "Micro Transactions",
                        "",
                        ChatColor.GRAY + "Want to access to more awesomeness?",
                        ChatColor.GRAY + "Consider donating to support DungeonRealms",
                        ChatColor.GRAY + "in return you'll receive several in-game",
                        ChatColor.GRAY + "perks!",
                        "",
                        ChatColor.YELLOW + "Click to view Micro Transactions!"
                })
                .setOption(24, new ItemStack(Material.REDSTONE_COMPARATOR), ChatColor.GREEN + "Settings & Preferences", new String[]{
                        ChatColor.DARK_GRAY + "Settings & Preferences",
                        "",
                        ChatColor.GRAY + "Help us help you! By adjusting",
                        ChatColor.GRAY + "your player settings & preferences",
                        ChatColor.GRAY + "we can optimize your gaming experience",
                        ChatColor.GRAY + "on this RPG!",
                        "",
                        ChatColor.YELLOW + "Click to view Settings & Preferences!"
                });

        menu.open(player);
    }
}
