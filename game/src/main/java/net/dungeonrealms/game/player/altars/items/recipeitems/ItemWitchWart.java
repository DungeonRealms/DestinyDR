package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemWitchWart extends ItemGeneric {

    protected String name = CC.GoldB + "Witch's Wart";
    protected String lore = CC.Gray + "A wart that was ripped from a Witch's Face.";

    public ItemWitchWart(){
        super(new ItemStack(Material.NETHER_WARTS));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "witchWart");
    }

    @Override
    protected ItemStack getStack() {
        return this.item;
    }
}
