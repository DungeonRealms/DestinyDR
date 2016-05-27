package net.dungeonrealms.game.world.entities.types.mounts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran on 10/16/2015.
 */
public enum EnumMounts {
    TIER1_HORSE(0, "T1HORSE", new ItemStack(Material.SADDLE, 1), 0),
    TIER2_HORSE(1, "T2HORSE", new ItemStack(Material.DIAMOND_BARDING, 1), 0),
    TIER3_HORSE(2, "T3HORSE", new ItemStack(Material.GOLD_BARDING, 1), 0),
    SKELETON_HORSE(3, "SKELETONHORSE", new ItemStack(Material.SKULL_ITEM, 1), 0),
    ZOMBIE_HORSE(4, "ZOMBIEHORSE", new ItemStack(Material.SKULL_ITEM, 1), 2),
    MULE(5, "MULE", new ItemStack(Material.CHEST, 1), 0);

    private int id;
    private String name;
    private ItemStack selectionItem;
    private int shortID;

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

    EnumMounts(int id, String name, ItemStack selectionItem, int shortID) {
        this.id = id;
        this.name = name;
        this.selectionItem = selectionItem;
        this.shortID = shortID;
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
