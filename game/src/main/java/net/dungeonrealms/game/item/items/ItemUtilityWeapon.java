package net.dungeonrealms.game.item.items;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.CombatItem;
import net.dungeonrealms.game.item.items.core.ItemWeaponMelee;
import net.dungeonrealms.game.item.items.core.ItemWeaponRanged;
import net.dungeonrealms.game.world.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Leave some room for more utility weapons later?
 */
public class ItemUtilityWeapon extends CombatItem {

    public final static ItemType[] UTILITY_WEAPONS = new ItemType[]{ItemType.MARKSMAN_BOW};

    public ItemUtilityWeapon() {
        this(UTILITY_WEAPONS);
    }

    public ItemUtilityWeapon(ItemType... type) {
        super(type);
    }

    public ItemUtilityWeapon(ItemStack item) {
        super(item);
    }

    @Override
    protected void applyEnchantStats() {
        getAttributes().multiplyStat(Item.WeaponAttributeType.DAMAGE_BOOST, 1.05);
    }

    @Override
    protected double getBaseRepairCost() {
        return getAttributes().getAttribute(Item.WeaponAttributeType.DAMAGE).getMiddle() /  10;
    }

    @Override
    protected void onItemBreak(Player player) {
        //Don't need to do anything.
    }

    public static boolean isUtilityWeapon(ItemStack item) {
        return ItemWeaponRanged.isRangedWeapon(item);
    }

    /**
     * Getting the NBT data from EACH item everytime is a bit consuming, just check type since thats all we need.
     * @param item
     * @return
     */
    public static boolean isWeaponFromMaterial(ItemStack item) {
        return item.getType() == Material.BOW;
    }
}
