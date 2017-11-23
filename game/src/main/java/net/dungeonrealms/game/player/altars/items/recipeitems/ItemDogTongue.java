package net.dungeonrealms.game.player.altars.items.recipeitems;

import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.items.core.ItemGear;
import net.dungeonrealms.game.item.items.core.ItemGeneric;
import net.dungeonrealms.game.world.item.CC;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ItemDogTongue extends ItemGeneric {
    protected String name = CC.DarkRedB + "Tongue of Dog";
    protected String lore = CC.Gray + "A tongue gruesomly yanked out of a dogs mouth.";

    public ItemDogTongue(){
        super(ItemType.ITEM_DOG_TONGUE);
    }

    public ItemDogTongue(ItemStack item) {
        super(item);
    }

    @Override
    protected ItemStack getStack() {
        return new ItemStack(Material.RABBIT_FOOT);
    }

    @Override
    public void updateItem() {
        setCustomName(name);
        setCustomLore(lore);
        super.updateItem();
    }
}
