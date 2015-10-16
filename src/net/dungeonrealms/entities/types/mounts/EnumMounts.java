package net.dungeonrealms.entities.types.mounts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran on 10/16/2015.
 */
public enum EnumMounts {
    TIER1_HORSE(0, "T1HORSE", new ItemStack(Material.SADDLE)),
    GOLD_HORSE(1, "GOLDHORSE", new ItemStack(Material.GOLD_BARDING)),
    DIAMOND_HORSE(2, "DIAMONDHORSE", new ItemStack(Material.DIAMOND_BARDING)),
    SKELETON_HORSE(3, "SKELETONHORSE", new ItemStack(Material.SKULL_ITEM, (short) 0)),
    ZOMBIE_HORSE(4, "ZOMBIEHORSE", new ItemStack(Material.SKULL_ITEM, (short) 2));

    private int id;
    private String name;
    private ItemStack selectionItem;

    public int getId() {
        return id;
    }

    public String getRawName() {
        return name;
    }

    public ItemStack getSelectionItem() {
        return selectionItem;
    }

    EnumMounts(int id, String name, ItemStack selectionItem) {
        this.id = id;
        this.name = name;
        this.selectionItem = selectionItem;
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
