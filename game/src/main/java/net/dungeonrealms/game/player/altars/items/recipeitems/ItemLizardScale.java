package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemLizardScale extends ItemGeneric {
    protected String name = CC.GreenB + "Lizard Scale";
    protected String lore = CC.Gray + "The slimy scales of a reptile.";

    public ItemLizardScale(){
        super(ItemType.ITEM_LIZARD_SCALE);
    }

    public ItemLizardScale(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.BEETROOT_SEEDS);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
