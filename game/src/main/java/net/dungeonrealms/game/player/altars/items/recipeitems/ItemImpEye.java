package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.glow.GlowAPI;

public class ItemImpEye extends ItemGeneric {
    protected String name = CC.AquaB + "Imp's Eye";
    protected String lore = CC.Gray + "An eye forcefully spooned out of the carcass if an Imp.";

    public ItemImpEye() {
        super(new ItemStack(Material.SPIDER_EYE));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "impEye");
    }

    @Override
    protected ItemStack getStack() { return this.item;}

}

