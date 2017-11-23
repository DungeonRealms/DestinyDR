package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemWitherEssence extends ItemGeneric {
    protected String name = CC.BlackB + "Wither Essence";
    protected String lore = CC.Gray + "An eerie item that is the source of the Wither race's power.";

    public ItemWitherEssence(){
        super(ItemType.ITEM_WITHER_ESSENCE);
    }

    public ItemWitherEssence(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.MELON_SEEDS);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
