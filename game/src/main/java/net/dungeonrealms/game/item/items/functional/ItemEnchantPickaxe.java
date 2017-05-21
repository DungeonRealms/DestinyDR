package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import org.bukkit.inventory.ItemStack;

public class ItemEnchantPickaxe extends ItemEnchantProfession {

    public ItemEnchantPickaxe() {
        this((ItemPickaxe) null);
    }

    public ItemEnchantPickaxe(ItemStack stack) {
        super(stack);
    }

    public ItemEnchantPickaxe(ItemPickaxe item) {
        super(ItemType.ENCHANT_PICKAXE, "Pickaxe", item);
    }

    @Override
    protected ItemType applyTo() {
        return ItemType.PICKAXE;
    }

    public static boolean isEnchant(ItemStack item) {
        return isType(item, ItemType.ENCHANT_PICKAXE);
    }
}