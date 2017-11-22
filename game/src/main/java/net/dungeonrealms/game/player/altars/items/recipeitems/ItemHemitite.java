package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemHemitite extends ItemGeneric {

    protected String name = CC.GrayB + "Hemitite";
    protected String lore = CC.Gray + "Metal Ore chipped from a mighty foe.";

    public ItemHemitite(){
        super(new ItemStack(Material.IRON_INGOT));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "hemitite");
        setGlowing(true);
    }

    @Override
    protected ItemStack getStack() {
        return this.item;
    }
}
