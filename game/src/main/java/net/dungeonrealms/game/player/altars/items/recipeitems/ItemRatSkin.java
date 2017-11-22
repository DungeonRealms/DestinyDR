package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemRatSkin extends ItemGeneric {
    protected String name = CC.DarkGrayB + "Rat Skin";
    protected String lore = CC.Gray + "The remaining skin off of a dead rat.";

    public ItemRatSkin() {
        super(new ItemStack(Material.QUARTZ));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "rat skin");
    }

    @Override
    protected ItemStack getStack() { return this.item;}
}
