package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemHemitite extends ItemGeneric {
    protected String name = CC.GrayB + "Hemitite";
    protected String lore = CC.Gray + "Metal Ore chipped from a mighty foe.";

    public ItemHemitite(){
        super(ItemType.ITEM_HEMITITE);
    }

    public ItemHemitite(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.IRON_INGOT);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
