package net.dungeonrealms.game.world.entity.type.mounts;

import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 27-May-16.
 */
public enum EnumMountSkins {
    SKELETON_HORSE("SKELETONHORSE", new ItemStack(Material.SKULL_ITEM, 1), 0, "Skeleton Horse"),
    ZOMBIE_HORSE("ZOMBIEHORSE", new ItemStack(Material.SKULL_ITEM, 1), 2, "Zombie Horse");

    @Getter private String name;
    @Getter private ItemStack selectionItem;
    @Getter private int shortID;
    @Getter private String displayName;

    EnumMountSkins(String name, ItemStack selectionItem, int shortID, String displayName) {
        this.name = name;
        this.selectionItem = selectionItem;
        this.shortID = shortID;
        this.displayName = displayName;
    }
    
    public int getId() {
    	return ordinal();
    }

    public static EnumMountSkins getById(int id) {
        for (EnumMountSkins ems : values())
            if (ems.getId() == id)
                return ems;
        return null;
    }

    public static EnumMountSkins getByName(String rawName) {
        for (EnumMountSkins ems : values())
            if (ems.name.equalsIgnoreCase(rawName))
                return ems;
        return null;
    }
}
