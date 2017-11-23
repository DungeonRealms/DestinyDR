package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemSacrificialHeart extends ItemGeneric {
    protected String name = CC.DarkRedB+ "Sacrificial Heart";
    protected String lore = CC.Gray + "A heart torn out of the body of an Acolyte";

    public ItemSacrificialHeart(){
        super(ItemType.ITEM_SACRIFICIAL_HEART);
    }

    public ItemSacrificialHeart(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.BEETROOT);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}

