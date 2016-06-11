package net.dungeonrealms.game.mechanics;

import java.util.UUID;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    public static void checkInventory(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        player.getInventory().setItem(7, ItemManager.createCharacterJournal(Bukkit.getPlayer(uuid)));
        for(ItemStack is : player.getInventory().getContents())
        {
        	if(is == ItemManager.getPlayerProfile(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Character Profile", new String[]{
                    ChatColor.GREEN + "Right Click: " + ChatColor.GRAY + "Open Profile"}))
        	{
        		is.setType(Material.AIR);
        	}
        }

    }

    public enum PlayerToggles {
        DEBUG(0, EnumData.TOGGLE_DEBUG, "toggledebug", "Toggles displaying combat debug messages.", "Debug Messages"),
        TRADE(1, EnumData.TOGGLE_TRADE, "toggletrade", "Toggles trading requests.", "Trade"),
        TRADE_CHAT(2, EnumData.TOGGLE_TRADE_CHAT, "toggletradechat", "Toggles receiving <T>rade chat.", "Trade Chat"),
        GLOBAL_CHAT(3, EnumData.TOGGLE_GLOBAL_CHAT, "toggleglobalchat", "Toggles talking only in global chat.", "Global Only Chat"),
        RECEIVE_MESSAGES(4, EnumData.TOGGLE_RECEIVE_MESSAGE, "toggletells", "Toggles receiving NON-BUD /tell.", "Non-BUD Private Messages"),
        PVP(5, EnumData.TOGGLE_PVP, "togglepvp", "Toggles all outgoing PvP damage (anti-neutral).", "Outgoing PvP Damage"),
        DUEL(6, EnumData.TOGGLE_DUEL, "toggleduel", "Toggles dueling requests.", "Dueling Requests"),
        CHAOTIC_PREVENTION(7, EnumData.TOGGLE_CHAOTIC_PREVENTION, "togglechaos", "Toggles killing blows on lawful players (anti-chaotic).", "Anti-Chaotic");

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

        public void setToggleState(Player player, boolean state) {
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, dbField, state, true);
            player.sendMessage((state ? ChatColor.GREEN : ChatColor.RED) + friendlyName + " - " + ChatColor.BOLD + (state ? "ENABLED" : "DISABLED"));
        }
    }
}
