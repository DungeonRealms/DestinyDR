package net.dungeonrealms.game.player.support;

import net.dungeonrealms.API;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.inventory.SupportMenus;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Created by Brad on 17/06/2016.
 */
public class Support {

    public static void handleClick(Player player, String menu, ItemStack itemClicked, int slotClicked) {
        // @todo: Do this sometime (move out of ClickHandler since it's bulky.
    }

    /**
     * This will add/set/remove an amount of E-Cash for the specified user (uuid).
     *
     * @param player
     * @param playerName
     * @param uuid
     * @param amount
     * @param type
     */
    public static void modifyEcash(Player player, String playerName, UUID uuid, int amount, String type) {
        DatabaseAPI.getInstance().update(uuid, (type != "set" ? EnumOperators.$INC : EnumOperators.$SET), EnumData.ECASH, (type != "remove" ? amount : (amount*-1)), true);
        API.updatePlayerData(uuid);
        player.sendMessage(ChatColor.GREEN + "Successfully " + type + (type == "add" ? "ed" : (type == "remove" ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " E-Cash to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
        SupportMenus.openMainMenu(player, playerName);
    }

    /**
     * This will add/set/remove an amount of level(s) for the specified user (uuid).
     *
     * @param player
     * @param playerName
     * @param uuid
     * @param amount
     * @param type
     */
    public static void modifyLevel(Player player, String playerName, UUID uuid, int amount, String type) {
        if (amount < 1 || amount > 100) {
            player.sendMessage(ChatColor.RED + "Error: Invalid #.");
            return;
        }

        DatabaseAPI.getInstance().update(uuid, (type != "set" ? EnumOperators.$INC : EnumOperators.$SET), EnumData.LEVEL, (type != "remove" ? amount : (amount*-1)), true);
        API.updatePlayerData(uuid);
        player.sendMessage(ChatColor.GREEN + "Successfully " + type + (type == "add" ? "ed" : (type == "remove" ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " level to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
        SupportMenus.openMainMenu(player, playerName);
    }

    /**
     * This will add/set/remove an amount of experience for the specified user (uuid).
     *
     * @param player
     * @param playerName
     * @param uuid
     * @param amount
     * @param type
     */
    public static void modifyExp(Player player, String playerName, UUID uuid, int amount, String type) {
        DatabaseAPI.getInstance().update(uuid, (type != "set" ? EnumOperators.$INC : EnumOperators.$SET), EnumData.EXPERIENCE, (type != "remove" ? amount : (amount*-1)), true);
        API.updatePlayerData(uuid);
        player.sendMessage(ChatColor.GREEN + "Successfully " + type + (type == "add" ? "ed" : (type == "remove" ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " experience to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
        SupportMenus.openMainMenu(player, playerName);
    }

    /**
     * This will add/set/remove an amount of gem(s) for the specified user (uuid).
     *
     * @param player
     * @param playerName
     * @param uuid
     * @param amount
     * @param type
     */
    public static void modifyGems(Player player, String playerName, UUID uuid, int amount, String type) {
        DatabaseAPI.getInstance().update(uuid, (type != "set" ? EnumOperators.$INC : EnumOperators.$SET), EnumData.GEMS, (type != "remove" ? amount : (amount*-1)), true);
        API.updatePlayerData(uuid);
        player.sendMessage(ChatColor.GREEN + "Successfully " + type + (type == "add" ? "ed" : (type == "remove" ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " gens to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
        SupportMenus.openMainMenu(player, playerName);
    }

}
