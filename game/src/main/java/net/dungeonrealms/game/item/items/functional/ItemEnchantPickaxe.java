package net.dungeonrealms.game.item.items.functional;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemPickaxe;
import net.dungeonrealms.game.world.item.Item.PickaxeAttributeType;
import net.dungeonrealms.game.world.item.Item.ProfessionAttribute;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class ItemEnchantPickaxe extends ItemEnchantProfession {

    public ItemEnchantPickaxe(PickaxeAttributeType type) {
        this();
        add(type);
    }

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
    protected boolean isApplicable(ItemStack item) {
        return ItemPickaxe.isPickaxe(item);
    }

    @Override
    protected String getDisplayName() {
        return ChatColor.WHITE + ChatColor.BOLD.toString() + "Scroll: " + ChatColor.YELLOW + "Pickaxe Enchant";
    }

    @Override
    protected String[] getLore() {
        return new String[]{"Imbues a pickaxe with special attributes."};
    }

    @Override
    protected ProfessionAttribute[] getValues() {
        return PickaxeAttributeType.values();
    }

    public static boolean isEnchant(ItemStack item) {
        return isType(item, ItemType.ENCHANT_PICKAXE);
    }
}