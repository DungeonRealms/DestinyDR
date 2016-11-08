package net.dungeonrealms.old.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.old.game.mechanic.ItemManager;
import net.dungeonrealms.old.game.player.chat.Chat;
import net.dungeonrealms.old.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 12/2/2015.
 */
public class CommandSkip extends BaseCommand {

    public CommandSkip(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        if (GameAPI.getRegionName(player.getLocation()).equalsIgnoreCase("tutorial_island")) {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED
                    + "If you skip this tutorial you will not recieve " + ChatColor.UNDERLINE + "ANY"
                    + ChatColor.RED + " of the item rewards for completing it.");
            player.sendMessage(ChatColor.GRAY + "If you're sure you still want to skip it, type '" + ChatColor.GREEN
                    + ChatColor.BOLD + "Y" + ChatColor.GRAY + "' to finish the tutorial. Otherwise, just type '"
                    + ChatColor.RED + "cancel" + ChatColor.GRAY + "' to continue with the tutorial.");

            Chat.listenForMessage(player, chat -> {
                if (chat.getMessage().equalsIgnoreCase("y")) {

                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        player.teleport(new Location(Bukkit.getWorlds().get(0), -600 + .5, 60 + 1.5, 473 + .5, -1F, 2.5F));
                        ItemManager.giveStarter(player);

                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                            final JSONMessage normal = new JSONMessage(ChatColor.GOLD + " ❢ " + ChatColor.YELLOW + "Need more information? Visit our wiki " + ChatColor.WHITE);
                            normal.addURL(ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "http://dungeonrealms.wikia.com/wiki/Main_Page");
                            normal.addSuggestCommand(ChatColor.YELLOW.toString() + " or for any questions. Click " + ChatColor.GOLD.toString() + ChatColor.BOLD + ChatColor.UNDERLINE + "HERE!", ChatColor.GREEN, "/ask ");
                            normal.addText(ChatColor.GOLD + " ❢ ");

                            player.sendMessage("");
                            normal.sendToPlayer(player);
                            player.sendMessage("");

                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 0.5F);
                        }, 40);
                    });
                }
            }, p -> p.sendMessage(ChatColor.RED + "TutorialQuest Skip - " + ChatColor.BOLD + "CANCELLED"));
        } else {
            player.sendMessage(ChatColor.RED + "You are not on the tutorial island.");
        }

        return true;
    }
}
