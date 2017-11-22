package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemFireDust extends ItemGeneric {
    protected String name = CC.RedB + "Fire Dust";
    protected String lore = CC.Gray + "A very hot dust found on the dead body of a Blaze.";

    public ItemFireDust() {
        super(new ItemStack(Material.BLAZE_POWDER));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "fireDust");
    }

    @Override
    protected ItemStack getStack() { return this.item;}
}
