package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemEyeOfBeholder extends ItemGeneric {

    protected String name = CC.GreenB + "Eye of The Beholder";
    protected String lore = CC.Gray + "The all seeing eye, capable of seeing the future and the past.";

    public ItemEyeOfBeholder(){
        super(new ItemStack(Material.EYE_OF_ENDER));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "eyeOfBeholder");
    }

    @Override
    protected ItemStack getStack() {
        return this.item;
    }
}
