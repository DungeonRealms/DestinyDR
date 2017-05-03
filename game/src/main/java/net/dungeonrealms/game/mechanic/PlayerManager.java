package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.PersistentItem;
import net.dungeonrealms.game.item.items.functional.ItemPlayerJournal;
import net.dungeonrealms.game.item.items.functional.ItemPortalRune;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Created by Nick on 9/18/2015.
 */
public class PlayerManager {

    /**
     * Ensures that every time the player logs in
     * the last slot (8) has the correct item.
     *
     * @param uuid
     * @since 1.0
     */
    public static void checkInventory(Player player) {
    	
        if (!hasItem(player.getInventory(), ItemType.PORTAL_RUNE) && isSlotFree(player.getInventory(), 7))
            player.getInventory().setItem(7, new ItemPortalRune(player).generateItem());

        if (!hasItem(player.getInventory(), ItemType.PLAYER_JOURNAL) && isSlotFree(player.getInventory(), 8))
        	player.getInventory().setItem(8, new ItemPlayerJournal().generateItem());
    }

    public static boolean isSlotFree(PlayerInventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        return (item == null || item.getType() == null || item.getType() == Material.AIR);
    }

    public static boolean hasItem(Player p, ItemType type) {
    	return hasItem(p.getInventory(), type);
    }
    
    public static boolean hasItem(PlayerInventory inv, ItemType type) {
        for (ItemStack item : inv.getContents())
            if (PersistentItem.isType(item, type))
            	return true;
        return false;
    }

    public enum PlayerToggles {
        DEBUG(0, EnumData.TOGGLE_DEBUG, "toggledebug", "Toggles displaying combat debug messages.", "Debug Messages"),
        TRADE(1, EnumData.TOGGLE_TRADE, "toggletrade", "Toggles trading requests.", "Trade"),
        TRADE_CHAT(2, EnumData.TOGGLE_TRADE_CHAT, "toggletradechat", "Toggles receiving <T>rade chat.", "Trade Chat"),
        GLOBAL_CHAT(3, EnumData.TOGGLE_GLOBAL_CHAT, "toggleglobalchat", "Toggles talking only in global chat.", "Global Only Chat"),
        RECEIVE_MESSAGES(4, EnumData.TOGGLE_RECEIVE_MESSAGE, "toggletells", "Toggles receiving NON-BUD /tell.", "Non-BUD Private Messages"),
        PVP(5, EnumData.TOGGLE_PVP, "togglepvp", "Toggles all outgoing PvP damage (anti-neutral).", "Outgoing PvP Damage"),
        DUEL(6, EnumData.TOGGLE_DUEL, "toggleduel", "Toggles dueling requests.", "Dueling Requests"),
        CHAOTIC_PREVENTION(7, EnumData.TOGGLE_CHAOTIC_PREVENTION, "togglechaos", "Toggles killing blows on lawful players (anti-chaotic).", "Anti-Chaotic"),
        DAMAGE_INDICATORS(8, EnumData.TOGGLE_DAMAGE_INDICATORS, "togglefloatdamage", "Toggles floating damage values.", "Damage Indicators"),
        ITEM_GLOW(9, EnumData.TOGGLE_GLOW, "toggleglow", "Toggles rare items glowing.", "Item Glow"),
        TIPS(10, EnumData.TOGGLE_TIPS, "toggletips", "Toggles the receiving of informative tips", "Tip display");

        private int id;
        private EnumData dbField;
        private String commandName;
        private String description;
        private String friendlyName;

        PlayerToggles(int id, EnumData dbField, String commandName, String description, String friendlyName) {
            this.id = id;
            this.dbField = dbField;
            this.commandName = commandName;
            this.description = description;
            this.friendlyName = friendlyName;
        }

        public static PlayerToggles getById(int id) {
            for (PlayerToggles playerToggles : values()) {
                if (playerToggles.id == id) {
                    return playerToggles;
                }
            }
            return null;
        }

        public static PlayerToggles getByCommandName(String commandName) {
            for (PlayerToggles playerToggles : values()) {
                if (playerToggles.commandName.equalsIgnoreCase(commandName)) {
                    return playerToggles;
                }
            }
            return null;
        }

        public EnumData getDbField() {
            return dbField;
        }

        public String getFriendlyName() {
            return friendlyName;
        }

        public String getDescription() {
            return description;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setToggleState(Player player, boolean state) {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, dbField, state, false);
            player.sendMessage((state ? ChatColor.GREEN : ChatColor.RED) + friendlyName + " - " + ChatColor.BOLD + (state ? "ENABLED" : "DISABLED"));
        }
    }
}
