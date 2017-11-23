package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemOrcTooth extends ItemGeneric {
    protected String name = CC.WhiteB + "Orc Tooth";
    protected String lore = CC.Gray + "A strong durable tooth from a dead Orc.";

    public ItemOrcTooth(){
        super(ItemType.ITEM_ORC_TOOTH);
    }

    public ItemOrcTooth(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.GHAST_TEAR);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
