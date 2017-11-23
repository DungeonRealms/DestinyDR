package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSpiderSilk extends ItemGeneric {

    protected String name = CC.WhiteB + "Spider Silk";
    protected String lore = CC.Gray + "Silk spun by a spider.";

    public ItemSpiderSilk(){
        super(ItemType.ITEM_SPIDER_SILK);
    }

    public ItemSpiderSilk(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.STRING);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
