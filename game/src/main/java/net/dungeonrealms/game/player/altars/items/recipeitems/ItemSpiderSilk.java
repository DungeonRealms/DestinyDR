package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSpiderSilk extends ItemGeneric {

    protected String name = CC.WhiteB + "Spider Silk";
    protected String lore = CC.Gray + "Silk spun by a spider.";

    public ItemSpiderSilk(){
        super(new ItemStack(Material.STRING));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "spiderSilk");
    }

    @Override
    protected ItemStack getStack() {
        return this.item;
    }
}
