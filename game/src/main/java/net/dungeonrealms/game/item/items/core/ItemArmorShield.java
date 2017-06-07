package net.dungeonrealms.game.item.items.core;

import net.dungeonrealms.game.item.ItemType;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Rar349 on 6/6/2017.
 */
public class ItemArmorShield extends ItemArmor {

    public ItemArmorShield() {
        super(ItemType.SHIELD);
    }

    public ItemArmorShield(ItemStack item) {
        super(item);
    }

    public static boolean isShield(ItemStack i) {
        return isType(i, ItemType.SHIELD);
    }

}
