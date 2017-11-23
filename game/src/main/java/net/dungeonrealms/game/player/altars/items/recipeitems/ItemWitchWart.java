package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemWitchWart extends ItemGeneric {

    protected String name = CC.RedB + "Witch's Wart";
    protected String lore = CC.Gray + "A wart that was ripped from a Witch's Face.";

    public ItemWitchWart(){
        super(ItemType.ITEM_WITCH_WART);
    }

    public ItemWitchWart(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.NETHER_STALK);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
