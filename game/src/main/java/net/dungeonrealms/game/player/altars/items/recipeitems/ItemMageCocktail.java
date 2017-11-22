package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemMageCocktail extends ItemGeneric {
    protected String name = CC.DarkPurpleB + "Mage's Cocktail";
    protected String lore = CC.Gray + "The source of Mage magic.";

    public ItemMageCocktail() {
        super(new ItemStack(Material.DRAGONS_BREATH));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "mageCocktail");
    }

    @Override
    protected ItemStack getStack() { return this.item;}
}
