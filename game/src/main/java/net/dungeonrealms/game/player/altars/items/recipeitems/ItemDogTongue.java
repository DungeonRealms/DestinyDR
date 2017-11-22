package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemDogTongue extends ItemGeneric {

    protected String name = CC.DarkRedB + "Tongue of Dog";
    protected String lore = CC.Gray + "A tongue gruesomly yanked out of a dogs mouth.";

    public ItemDogTongue(){
        super(new ItemStack(Material.NETHER_WARTS));
        setCustomName(name);
        setCustomLore(lore);
        setTagString("recipeItem", "dogTongue");
    }

    @Override
    protected ItemStack getStack() {
        return this.item;
    }
}
