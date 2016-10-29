package net.dungeonrealms.vgame.item.weapon;

import lombok.Getter;
import net.dungeonrealms.vgame.item.EnumItemTier;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.IStack;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class WeaponItem implements IStack
{
    private UUID uuid;

    private ItemStack itemStack;

    private EnumItemType itemType; // TODO

    @Getter
    private EnumItemTier itemTier;

    @Getter
    private double minDmg, maxDmg; // TODO

    @Getter
    private int durability = 100; // Default value ?

    public WeaponItem()
    {
        this.uuid = UUID.randomUUID();

        this.itemTier = EnumItemTier.random(); // Random tier upon generation

        // Create the atomic key (bukkit itemstack)
        this.itemStack = new ItemStack(Material.AIR);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(this.itemTier.getColor() + "Test Object");
        itemMeta.setLore(Arrays.asList("", this.itemTier.getColor() + this.itemTier.getName()));
        itemMeta.getItemFlags().clear();
        this.itemStack.setItemMeta(itemMeta);
        this.itemStack.setDurability((short) durability);
    }

    @Override
    public UUID getUniqueID()
    {
        return uuid;
    }

    @Override
    public ItemStack getItemStack()
    {
        return itemStack;
    }

    @Override
    public EnumItemType getType()
    {
        return itemType;
    }
}
