package net.dungeonrealms.duel;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.mechanics.ItemManager;

/**
 * Created by Chase on Sep 20, 2015
 */
public class DuelWager {
    public Player p1;
    public Player p2;
    private ItemTier armorTier;
    private ItemTier weaponTier;
    private ArrayList<ItemStack> winningItems;
    public boolean completed = false;
    private int timerID;

    public DuelWager(Player p1, Player p2) {
        this.p1 = p1;
        this.p2 = p2;
        armorTier = ItemTier.TIER_5;
        weaponTier = ItemTier.TIER_5;
        winningItems = new ArrayList<>();
    }
    /**
     * updates the Duel Inventory by setting the slot for both players screens
     * @param slot
     * @param stack
     */
    public void setItemSlot(int slot, ItemStack stack) {
        p1.getOpenInventory().setItem(slot, stack);
        p2.getOpenInventory().setItem(slot, stack);
    }

    /**
     * Checks if the player is on the left side of the inventory.
     * @param p
     * @return
     */
    public boolean isLeft(Player p) {
        return (p.getUniqueId() == p1.getUniqueId());
    }

    /**
     * Go to next Tier Weapon for inventory
     */
    public void cycleWeapon() {
        ItemTier[] list = ItemTier.values();
        int j = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] == weaponTier) {
                j = i + 1;
                if (j >= list.length) {
                    j = 0;
                    break;
                }
            }
        }
        weaponTier = list[j];
        setItemSlot(32, getWeaponItem());
    }

    /**
     * @return
     */
    private ItemStack getWeaponItem() {
        switch (weaponTier) {
            case TIER_1:
                return ItemManager.createItem(Material.WOOD_SWORD, "Weapon Tier Limit", null);
            case TIER_2:
                return ItemManager.createItem(Material.STONE_SWORD, "Weapon Tier Limit", null);
            case TIER_3:
                return ItemManager.createItem(Material.IRON_SWORD, "Weapon Tier Limit", null);
            case TIER_4:
                return ItemManager.createItem(Material.DIAMOND_SWORD, "Weapon Tier Limit", null);
            case TIER_5:
                return ItemManager.createItem(Material.GOLD_SWORD, "Weapon Tier Limit", null);
        }
        return null;
    }

    /**
     * Go to next Tier
     */
    public void cycleArmor() {
        ItemTier[] list = ItemTier.values();
        int j = 0;
        for (int i = 0; i < list.length; i++) {
            if (list[i] == armorTier) {
                j = i + 1;
                if (j >= list.length) {
                    j = 0;
                    break;
                }
            }
        }
        armorTier = list[j];
        setItemSlot(30, getArmorItem());
    }

    /**
     * @return
     */
    private ItemStack getArmorItem() {
        switch (armorTier) {
            case TIER_1:
                return ItemManager.createItem(Material.LEATHER_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_2:
                return ItemManager.createItem(Material.CHAINMAIL_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_3:
                return ItemManager.createItem(Material.IRON_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_4:
                return ItemManager.createItem(Material.DIAMOND_CHESTPLATE, "Armor Tier Limit", null);
            case TIER_5:
                return ItemManager.createItem(Material.GOLD_CHESTPLATE, "Armor Tier Limit", null);
        }
        return null;
    }

    /**
     * Player2 is the loser.
     * Ends the duel for the specified DUel
     * @param winner
     * @param loser
     */
    public void endDuel(Player winner, Player loser) {
        Bukkit.broadcastMessage(winner.getDisplayName() + " has defeated " + loser.getDisplayName() + " in a duel.");
        for (ItemStack winningItem : winningItems) {
            winner.getInventory().addItem(winningItem);
        }
        DuelMechanics.DUELS.remove(p1.getUniqueId());
        DuelMechanics.DUELS.remove(p2.getUniqueId());
        DuelMechanics.WAGERS.remove(this);

    }

    /**
     *Initiates a duel with players from wager.
     */
    public void startDuel() {
        completed = true;
        saveWagerItems();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int time = 10;

            @Override
            public void run() {
                time--;
                if (time == 0) {
                    p1.sendMessage(ChatColor.GREEN + "Duel started with " + p2.getDisplayName());
                    p2.sendMessage(ChatColor.GREEN + "Duel started with " + p1.getDisplayName());
                    DuelMechanics.DUELS.put(p1.getUniqueId(), p2.getUniqueId());
                    DuelMechanics.DUELS.put(p2.getUniqueId(), p1.getUniqueId());
                    this.cancel();
                } else {
                    p1.sendMessage(ChatColor.GREEN.toString() + time + ChatColor.YELLOW.toString()
                            + " seconds until the battle begins!");
                    p2.sendMessage(ChatColor.GREEN.toString() + time + ChatColor.YELLOW.toString()
                            + " seconds until the battle begins!");
                }
            }

        }, 0, 1000);
        
        
        timerID = Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            ItemStack[] p1armor = p1.getInventory().getArmorContents();
            ItemStack[] p2armor = p2.getInventory().getArmorContents();
            if(!DuelMechanics.isDueling(p1.getUniqueId()))
            	Bukkit.getScheduler().cancelTask(timerID);//this is okay.
            for (int i = 0; i < p1armor.length; i++) {
                if (!isTier(p1armor[i])) {
                    ItemStack stack = p1armor[i];
                    p1armor[i] = null;
                    p1.getInventory().setArmorContents(p1armor);
                    p1.getInventory().addItem(stack);
                }
            }
            for (int i = 0; i < p2armor.length; i++) {
                if (!isTier(p2armor[i])) {
                    ItemStack stack = p2armor[i];
                    p1armor[i] = null;
                    p2.getInventory().setArmorContents(p2armor);
                    p2.getInventory().addItem(stack);
                }
            }
        },10 * 1000);
        p1.closeInventory();
        p2.closeInventory();

    }

    /**
     * @param itemStack
     * @return
     */
    private boolean isTier(ItemStack itemStack) {
        return false;
    }

    /**
     * Save Items that are being waged
     */
    private void saveWagerItems() {
        int[] slots = new int[]{1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 23, 24, 25, 26, 5, 6, 7, 14, 15, 16, 17};
        InventoryView inv = p1.getOpenInventory();
        for (int slot : slots) {
            ItemStack current = inv.getItem(slot);
            if (current != null && current.getType() != Material.AIR)
                winningItems.add(current);
        }
    }

    /**
     * LEFT ITEMS 1,2,3 9, 10, 11, 12, 18, 19, 20, 21 RIGHT ITEMS 23,24,25,26 ,
     * 5,6,7, 14,15,16,17
     */
    public void giveItemsBack() {
        InventoryView inv = p1.getOpenInventory();
        int[] left = new int[]{1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21};
        int[] right = new int[]{23, 24, 25, 26, 5, 6, 7, 14, 15, 16, 17};
        for (int aLeft : left) {
            ItemStack current = inv.getItem(aLeft);
            if (current != null && current.getType() != Material.AIR) {
                p1.getInventory().addItem(current);
            }
        }
        for (int aRight : right) {
            ItemStack current = inv.getItem(aRight);
            if (current != null && current.getType() != Material.AIR) {
                p2.getInventory().addItem(current);
            }
        }
    }

    /**
     * @param uuid
     */
    public void handleLogOut(UUID uuid) {
        if (p1.getUniqueId() == uuid) {
            endDuel(p2, p1);
        } else {
            endDuel(p1, p1);
        }
    }

    /**
     * @param slot 
     * Check if slot is specified slot
     */
    public boolean isLeftSlot(int slot) {
        int[] left = new int[]{1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21};
        for (int aLeft : left)
            if (aLeft == slot)
                return true;
        return false;
    }
}
