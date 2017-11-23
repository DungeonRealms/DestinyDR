package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSpiderEye extends ItemGeneric {

    protected String name = CC.LightPurpleB+ "Spider\'s Eye";
    protected String lore = CC.Gray + "The eye of a spider oozing green fluid.";

    public ItemSpiderEye(){
        super(ItemType.ITEM_SPIDER_EYE);
    }

    public ItemSpiderEye(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.SPIDER_EYE);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
