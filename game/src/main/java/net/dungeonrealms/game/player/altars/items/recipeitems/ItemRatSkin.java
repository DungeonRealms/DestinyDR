package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemRatSkin extends ItemGeneric {
    protected String name = CC.DarkGrayB + "Rat Skin";
    protected String lore = CC.Gray + "The remaining skin off of a dead rat.";

    public ItemRatSkin(){
        super(ItemType.ITEM_RAT_SKIN);
    }

    public ItemRatSkin(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.RABBIT_HIDE);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
