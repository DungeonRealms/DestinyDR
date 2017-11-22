package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSpiderEye extends ItemGeneric {

    protected String name = CC.LightPurpleB+ "Spider's Eye";
    protected String lore = CC.Gray + "The eye of a spider oozing green fluid.";

    public ItemSpiderEye(){
        super(new ItemStack(Material.SPIDER_EYE));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "spiderEye");
    }

    @Override
    protected ItemStack getStack() {
        return this.item;
    }
}
