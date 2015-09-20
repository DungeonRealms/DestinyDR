package net.dungeonrealms.items;

import net.dungeonrealms.mechanics.XRandom;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Nick on 9/19/2015.
 */
public class ItemGenerator {

    public ItemStack next() {
        return getWeapon(getRandomItemMaterial(), getRandomItemType(), getRandomItemTier(), getRandomItemModifier());
    }

    //Starting Items..

    ItemStack getWeapon(Item.ItemMaterial material, Item.ItemType type, Item.ItemTier tier, Item.ItemModifier modifier) {
        return null;
    }

   /*
    private static ItemStack editNBT(ItemStack item) {
        Attribute attributes = new Attribute(item);
    }
    */

    public Item.ItemType getRandomItemType() {
        return Item.ItemType.getById(new XRandom().nextInt(Item.ItemType.values().length));
    }

    public Item.ItemMaterial getRandomItemMaterial() {
        return Item.ItemMaterial.getById(new XRandom().nextInt(Item.ItemMaterial.values().length));
    }


    public Item.ItemTier getRandomItemTier() {
        return Item.ItemTier.getById(new XRandom().nextInt(Item.ItemTier.values().length));
    }

    public Item.ItemModifier getRandomItemModifier() {
        return Item.ItemModifier.getById(new XRandom().nextInt(Item.ItemModifier.values().length));
    }
}
