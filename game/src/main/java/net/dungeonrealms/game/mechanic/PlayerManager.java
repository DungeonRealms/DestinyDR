package net.dungeonrealms.game.mechanic;

import lombok.Getter;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.database.PlayerToggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.lang.reflect.Field;
import java.util.UUID;

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
        if (player == null || !player.isOnline()) return;

        if (!hasItem(player.getInventory(), "realmPortalRune") && isSlotFree(player.getInventory(), 7)) {
            player.getInventory().setItem(7, ItemManager.createRealmPortalRune(uuid));
        }

        if (!hasItem(player.getInventory(), "journal") && isSlotFree(player.getInventory(), 8)) {
            player.getInventory().setItem(8, ItemManager.createCharacterJournal(Bukkit.getPlayer(uuid)));
        }
    }

    public static boolean isSlotFree(PlayerInventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        return (item == null || item.getType() == null || item.getType() == Material.AIR);
    }

    public static boolean hasItem(PlayerInventory inv, String type) {
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == null || item.getType() == Material.AIR) continue;
            net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsStack.getTag();
            if (tag == null) continue;
            if (!tag.hasKey(type)) continue;
            if (tag.getString(type).equalsIgnoreCase("true")) {
                return true;
            }
        }
        return false;
    }

    public enum PlayerToggles {
        DEBUG(0, "debug", "toggledebug", "Toggles displaying combat debug messages.", "Debug Messages"),
        TRADE(1, "trade", "toggletrade", "Toggles trading requests.", "Trade"),
        TRADE_CHAT(2, "tradeChat", "toggletradechat", "Toggles receiving <T>rade chat.", "Trade Chat"),
        GLOBAL_CHAT(3, "globalChat", "toggleglobalchat", "Toggles talking only in global chat.", "Global Only Chat"),
        RECEIVE_MESSAGES(4, "receiveMessage", "toggletells", "Toggles receiving NON-BUD /tell.", "Non-BUD Private Messages"),
        PVP(5, "pvp", "togglepvp", "Toggles all outgoing PvP damage (anti-neutral).", "Outgoing PvP Damage"),
        DUEL(6, "duel", "toggleduel", "Toggles dueling requests.", "Dueling Requests"),
        CHAOTIC_PREVENTION(7, "chaoticPrevention", "togglechaos", "Toggles killing blows on lawful players (anti-chaotic).", "Anti-Chaotic"),
        DAMAGE_INDICATORS(8, "damageIndicators", "togglefloatdamage", "Toggles floating damage values.", "Damage Indicators"),
        ITEM_GLOW(9, "glow", "toggleglow", "Toggles rare items glowing.", "Item Glow"),
        TIPS(10, "tips", "toggletips", "Toggles the receiving of informative tips", "Tip display");

        private int id;
        @Getter
        private String variableName;
        private String commandName;
        private String description;
        private String friendlyName;

        PlayerToggles(int id, String variableName, String commandName, String description, String friendlyName) {
            this.id = id;
            this.variableName = variableName;
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
            try {
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                net.dungeonrealms.database.PlayerToggles toggle = wrapper.getToggles();
                Class<?> clas = toggle.getClass();
                Field variable = clas.getDeclaredField(variableName);
                variable.setAccessible(true);
                variable.set(toggle, !((boolean) variable.get(toggle)));
                player.sendMessage((state ? ChatColor.GREEN : ChatColor.RED) + getFriendlyName() + " - " + ChatColor.BOLD + (state ? "ENABLED" : "DISABLED"));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public boolean getToggleState(Player player) {
            try {
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                net.dungeonrealms.database.PlayerToggles toggle = wrapper.getToggles();
                Class<?> clas = toggle.getClass();
                Field variable = clas.getDeclaredField(variableName);
                variable.setAccessible(true);
                return (boolean) variable.get(toggle);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
