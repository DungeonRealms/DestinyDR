package net.dungeonrealms.game.world.entity.type.mounts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 27-May-16.
 */
@AllArgsConstructor @Getter
public enum EnumMountSkins {
    SKELETON_HORSE(new ItemStack(Material.SKULL_ITEM, 1, (short) 0), "Skeleton Horse", 4),
    ZOMBIE_HORSE(new ItemStack(Material.SKULL_ITEM, 1, (short) 2), "Zombie Horse", 3);

    private ItemStack selectionItem;
    private String displayName;
    private int meta;
    
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
            if (ems.name().equalsIgnoreCase(rawName))
                return ems;
        return null;
    }
}
