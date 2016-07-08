package net.dungeonrealms.game.world.entities.types.mounts;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 27-May-16.
 */
public enum EnumMountSkins {
    SKELETON_HORSE(0, "SKELETONHORSE", new ItemStack(Material.SKULL_ITEM, 1), 0, "Skeleton Horse"),
    ZOMBIE_HORSE(1, "ZOMBIEHORSE", new ItemStack(Material.SKULL_ITEM, 1), 2, "Zombie Horse");

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

    EnumMountSkins(int id, String name, ItemStack selectionItem, int shortID, String displayName) {
        this.id = id;
        this.name = name;
        this.selectionItem = selectionItem;
        this.shortID = shortID;
        this.displayName = displayName;
    }

    public static EnumMountSkins getById(int id) {
        for (EnumMountSkins ems : values()) {
            if (ems.getId() == id) {
                return ems;
            }
        }
        return null;
    }

    public static EnumMountSkins getByName(String rawName) {
        for (EnumMountSkins ems : values()) {
            if (ems.name.equalsIgnoreCase(rawName)) {
                return ems;
            }
        }
        return null;
    }
}
