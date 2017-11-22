package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemFairyDust extends ItemGeneric {
    protected String name = CC.YellowB + "Fairy Dust";
    protected String lore = CC.Gray + "A fine dust extracted from a Nymph.";

    public ItemFairyDust() {
        super(new ItemStack(Material.PRISMARINE_CRYSTALS));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "fairyDust");
    }

    @Override
    protected ItemStack getStack() { return this.item;}
}
