package net.dungeonrealms.game.command.moderation;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;

import net.dungeonrealms.game.player.inventory.menus.guis.support.CharacterSelectionGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Brad on 25/12/2016.
 */
public class CommandArmorsee extends BaseCommand {
    public CommandArmorsee() {
        super("armorsee", "/<command> <player>", "View a player's armor inventory.", Collections.singletonList("mas"));
    }

    public static Map<UUID, UUID> offline_armor_watchers = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player sender = (Player) s;

        if (!Rank.isGM(sender)) return false;

        if (args.length == 0) {
            s.sendMessage(usage);
            return true;
        }

        String playerName = args[0];
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            Inventory inv = Bukkit.createInventory(null, 9, player.getName() + " Armor");
            for (int i = 0; i < 4; i++) {
                ItemStack stack = player.getInventory().getArmorContents()[i];
                if (stack == null) continue;
                inv.addItem(stack);
            }
            inv.setItem(8, player.getEquipment().getItemInMainHand());
            sender.openInventory(inv);
        }
        else {
            SQLDatabaseAPI.getInstance().getUUIDFromName(playerName, false, (uuid) -> {
                if(uuid == null) {
                    sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                    return;
                }
                Integer accountID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(uuid);

                if(accountID == null) {
                    sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + playerName + ChatColor.RED + " does not exist in our database.");
                    return;
                }
                new CharacterSelectionGUI(player, accountID, (charID) -> {
                    PlayerWrapper.getPlayerWrapper(uuid,charID, false, true, (wrapper) -> {
                        if(wrapper == null) {
                            return;
                        }

                        if(wrapper.getPendingArmor() == null) {
                            sender.sendMessage(ChatColor.GREEN + "This player is not wearing any armor!");
                            return;
                        }
                        sender.openInventory(wrapper.getPendingArmor());
                        offline_armor_watchers.put(sender.getUniqueId(), uuid);

                    });
                }).open(player,null);
            });

        }

        return false;
    }
}
