package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemEyeOfBeholder extends ItemGeneric {
    protected String name = CC.GreenB + "Eye of The Beholder";
    protected String lore = CC.Gray + "The all seeing eye, capable of seeing the future and the past.";

    public ItemEyeOfBeholder(){
        super(ItemType.ITEM_EYE_OF_BEHOLDER);
    }

    public ItemEyeOfBeholder(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.EYE_OF_ENDER);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
