package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemGoldenCharm extends ItemGeneric {
    protected String name = CC.GoldB + "Golden Charm";
    protected String lore = CC.Gray + "A golden charm looted from a dead Daemon.";

    public ItemGoldenCharm() {
        super(new ItemStack(Material.GOLD_NUGGET));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "goldenCharm");
    }

    @Override
    protected ItemStack getStack() { return this.item;}

}

