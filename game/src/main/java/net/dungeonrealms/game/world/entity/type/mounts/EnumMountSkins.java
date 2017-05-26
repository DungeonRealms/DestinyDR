package net.dungeonrealms.game.world.entity.type.mounts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Kieran Quigley (Proxying) on 27-May-16.
 */
@AllArgsConstructor
@Getter
public enum EnumMountSkins {
    SKELETON_HORSE(new ItemStack(Material.SKULL_ITEM, 1, (short) 0), "Skeleton Horse Skin", "Transforms your horse into a conjured skeletal beast.", 1250, 4, Horse.Variant.SKELETON_HORSE),
    ZOMBIE_HORSE(new ItemStack(Material.SKULL_ITEM, 1, (short) 2), "Zombie Horse Skin", "Transforms your horse into a demonic death charger.", 1250, 3, Horse.Variant.UNDEAD_HORSE);

    private ItemStack selectionItem;
    private String displayName;
    private String description;
    private int ecashPrice;
    private int meta;
    private Horse.Variant variant;

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
