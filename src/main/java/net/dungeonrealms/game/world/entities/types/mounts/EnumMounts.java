package net.dungeonrealms.game.world.entities.types.mounts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran on 10/16/2015.
 */
public enum EnumMounts {
    TIER1_HORSE(0, "T1HORSE", new ItemStack(Material.SADDLE, 1), 0, "Old Horse"),
    TIER2_HORSE(0, "T2HORSE", new ItemStack(Material.IRON_BARDING, 1), 0 , "Squire's Horse"),
    TIER3_HORSE(2, "T3HORSE", new ItemStack(Material.DIAMOND_BARDING, 1), 0, "Traveler's Horse"),
    TIER4_HORSE(3, "T4HORSE", new ItemStack(Material.GOLD_BARDING, 1), 0, "Knight's Horse"),
    MULE(4, "MULE", new ItemStack(Material.CHEST, 1), 0, "Mule");

    private int id;
    private String name;
    private ItemStack selectionItem;
    private int shortID;
    private String displayName;

    public int getId() {
        return id;
    }

    public String getRawName() {
        return name;
    }

    public ItemStack getSelectionItem() {
        return selectionItem;
    }

    public short getShortID() {
        return (short) shortID;
    }

    public String getDisplayName() {
        return displayName;
    }

    EnumMounts(int id, String name, ItemStack selectionItem, int shortID, String displayName) {
        this.id = id;
        this.name = name;
        this.selectionItem = selectionItem;
        this.shortID = shortID;
        this.displayName = displayName;
    }

    public static EnumMounts getById(int id) {
        for (EnumMounts em : values()) {
            if (em.getId() == id) {
                return em;
            }
        }
        return null;
    }

    public static EnumMounts getByName(String rawName) {
        for (EnumMounts em : values()) {
            if (em.name.equalsIgnoreCase(rawName)) {
                return em;
            }
        }
        return null;
    }
}
