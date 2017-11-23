package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemMageCocktail extends ItemGeneric {
    protected String name = CC.DarkPurpleB + "Mage\'s Cocktail";
    protected String lore = CC.Gray + "The source of Mage magic.";

    public ItemMageCocktail(){
        super(ItemType.ITEM_MAGE_COCKTAIL);
    }

    public ItemMageCocktail(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.DRAGONS_BREATH);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
