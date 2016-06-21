package net.dungeonrealms.game.world.anticheat;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mastery.Utils;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by Nick on 10/1/2015.
 */
public class AntiCheat {

    static AntiCheat instance = null;

    public static AntiCheat getInstance() {
        if (instance == null) {
            instance = new AntiCheat();
        }
        return instance;
    }

    public void startInitialization() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> Bukkit.getOnlinePlayers().stream().forEach(this::checkPlayer), 0, 20);
    }

    /**
     * Will be placed inside an eventlistener to make sure
     * the player isn't duplicating.
     *
     * @param event
     * @return
     * @since 1.0
     */
    public boolean watchForDupes(InventoryClickEvent event) {
        ItemStack checkItem = event.getCurrentItem();
        if (checkItem == null) return false;
        if (!isRegistered(checkItem)) return false;
        String check = getUniqueEpochIdentifier(checkItem);
        for (ItemStack item : event.getInventory().getContents()) {
            if (item == null || item.getType() == null || item.getType().equals(Material.AIR)) continue;
            if (check.equals(getUniqueEpochIdentifier(item))) {
                event.getWhoClicked().getInventory().remove(checkItem);
                return true;
            }
        }
        checkPlayer(((Player) event.getWhoClicked()));
        return false;
    }

    public void checkPlayer(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().equals(Material.AIR)) {
                if (item.getAmount() > 1) {
                    if (isRegistered(item)) {
                        player.getInventory().remove(item);
                        Utils.log.warning("[ANTI-CHEAT] [DUPE] Player: " + player.getName());
                        //player.sendMessage(ChatColor.RED + "Duplication detected in your inventory! Action has been logged and most certainly prevented you from any future opportunities.");
                        //Bukkit.broadcastMessage(ChatColor.RED + "Detected Duplicated Items in: " + ChatColor.AQUA + player.getName() + "'s" + ChatColor.RED + " inventory. Duplicated Items Removed.");
                    }
                }
            }
        }


        if (player.getItemOnCursor() != null && !player.getItemOnCursor().getType().equals(Material.AIR)) {
            if (player.getItemOnCursor().getAmount() > 1) {
                if (isRegistered(player.getItemOnCursor())) {
                    player.setItemOnCursor(new ItemStack(Material.AIR));
                }
            }
        }
    }

    /**
     * Returns the actual Epoch Unix String Identifier
     *
     * @param item
     * @return
     * @since 1.0
     */
    public String getUniqueEpochIdentifier(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || !tag.hasKey("u")) return null;
        return tag.getString("u");
    }

    /**
     * Check to see if item contains 'u' field.
     *
     * @param item
     * @return
     * @since 1.0
     */
    public boolean isRegistered(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return !(nmsStack == null || nmsStack.getTag() == null) && nmsStack.getTag().hasKey("u");
    }

    /**
     * Adds a (u) to the item. (u) -> UNIQUE IDENTIFIER
     *
     * @param item
     * @return
     * @since 1.0
     */
    public ItemStack applyAntiDupe(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || tag.hasKey("u")) return item;
        tag.set("u", new NBTTagString(System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability() + new Random().nextInt(999) + "R"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

}
