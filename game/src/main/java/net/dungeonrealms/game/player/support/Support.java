package net.dungeonrealms.game.player.support;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank.PlayerRank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.UpdateType;
import net.dungeonrealms.game.player.inventory.SupportMenus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
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

        PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
            wrapper.setEcash(type.equals("set") ? amount : type.equals("remove") ? wrapper.getEcash() + (amount * -1) : wrapper.getEcash() + amount);
            wrapper.saveData(true, null, (wrapp) -> {
                GameAPI.updatePlayerData(uuid, UpdateType.ECASH);
                player.sendMessage(ChatColor.GREEN + "Successfully " + type + (Objects.equals(type, "add") ? "ed" : (Objects.equals(type, "remove") ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " E-Cash to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);
            });
        });
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

        PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
            wrapper.setLevel(type.equals("set") ? amount : type.equals("remove") ? wrapper.getLevel() + (amount * -1) : wrapper.getLevel() + amount);
            wrapper.saveData(true, null, (wrap) -> {
                GameAPI.updatePlayerData(uuid, UpdateType.LEVEL);
                player.sendMessage(ChatColor.GREEN + "Successfully " + type + (Objects.equals(type, "add") ? "ed" : (Objects.equals(type, "remove") ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " level to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);
            });
        });
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

        PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
            wrapper.setExperience(type.equals("set") ? amount : type.equals("remove") ? wrapper.getExperience() + (amount * -1) : wrapper.getExperience() + amount);
            wrapper.saveData(true, null, (wrap) -> {
                GameAPI.updatePlayerData(uuid, UpdateType.EXP);
                player.sendMessage(ChatColor.GREEN + "Successfully " + type + (Objects.equals(type, "add") ? "ed" : (Objects.equals(type, "remove") ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " experience to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);
            });
        });
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
        PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
            wrapper.setGems(type.equals("set") ? amount : type.equals("remove") ? (amount * -1) : amount);
            wrapper.saveData(true, null, (wrap) -> {
                GameAPI.updatePlayerData(uuid, UpdateType.GEMS);
                player.sendMessage(ChatColor.GREEN + "Successfully " + type + (Objects.equals(type, "add") ? "ed" : (Objects.equals(type, "remove") ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + ChatColor.GREEN + " experience to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);
            });
        });
    }

    public static void modifySubscription(Player player, String playerName, UUID uuid, int amount, String type, PlayerRank rank) {
        // @todo: There's an error with this, still a WIP.

        player.sendMessage(playerName + ", " + uuid.toString() + ", " + amount + ", " + type + ", " + rank.name());

        PlayerWrapper.getPlayerWrapper(uuid, false, true, (wrapper) -> {
            wrapper.setRank(rank);
            wrapper.setRankExpiration(type.equalsIgnoreCase("set") ? amount : wrapper.getRankExpiration() + amount);
            wrapper.saveData(true, null, wrap -> {
                GameAPI.updatePlayerData(uuid, UpdateType.RANK);
                player.sendMessage(ChatColor.GREEN + "Successfully " + type + (Objects.equals(type, "add") ? "ed" : (Objects.equals(type, "remove") ? "d" : "")) + " " + ChatColor.BOLD + ChatColor.UNDERLINE + amount + " " + rank.getPrefix() + " DAYS" + ChatColor.GREEN + " to " + ChatColor.BOLD + ChatColor.UNDERLINE + playerName + ChatColor.GREEN + ".");
                SupportMenus.openMainMenu(player, playerName);
            });
        });
    }

}
