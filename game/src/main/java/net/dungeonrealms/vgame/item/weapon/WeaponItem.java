package net.dungeonrealms.vgame.item.weapon;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.vgame.item.EnumItemRarity;
import net.dungeonrealms.vgame.item.EnumItemTier;
import net.dungeonrealms.vgame.item.EnumItemType;
import net.dungeonrealms.vgame.item.IStack;
import net.dungeonrealms.vgame.item.weapon.attribute.EnumWeaponAttribute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class WeaponItem implements IStack
{
    // TODO weapon min & max dmg

    private UUID uuid;

    @Getter
    private Material material;

    @Getter
    private EnumItemRarity itemRarity;

    @Getter
    private EnumItemTier itemTier;

    @Getter
    private double minDmg, maxDmg; // TODO

    @Getter
    private int durability = 100; // Default value ?

    @Getter
    private List<EnumWeaponAttribute> weaponAttributes;

    @Getter
    private String name;

    private ItemStack itemStack;

    private EnumItemType itemType;

    private boolean soulbound;

    private boolean tradeable;

    public WeaponItem(boolean soulbound, boolean tradeable)
    {
        this.uuid = UUID.randomUUID();

        this.itemType = EnumItemType.randomItem(false); // Random weapon item

        this.weaponAttributes = EnumWeaponAttribute.random(this.itemTier.getMaxAttributes()); // Random collection of attributes
        this.itemRarity = EnumItemRarity.random(); // Random rarity upon generation
        this.itemTier = EnumItemTier.random(); // Random tier upon generation
        this.material = this.itemTier.getMaterial(this.itemType);

        this.soulbound = soulbound;
        this.tradeable = tradeable;

        this.createKey(); // Actual item
    }

    // Constructing a new weapon out of the database
    public WeaponItem(UUID uuid,
                      Material material,
                      EnumItemRarity rarity,
                      EnumItemTier itemTier,
                      EnumItemType type,
                      int durability,
                      String name,
                      List<EnumWeaponAttribute> attributes)
    {
        this.uuid = uuid;
        this.material = material;
        this.itemRarity = rarity;
        this.itemTier = itemTier;
        this.itemType = type;
        this.durability = durability;
        this.name = name;
        this.weaponAttributes = attributes;

        this.createKey(); // Actual item

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

    @Override
    public boolean isSoulbound()
    {
        return soulbound;
    }

    @Override
    public boolean isTradeable()
    {
        return tradeable;
    }

    private void calculateAttributes()
    {

    }

    private void createKey()
    {
        // Create the atomic key (bukkit itemstack)
        this.itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(this.itemRarity.getColor() + "Test Object");

        // Attach the lore
        itemMeta.setLore(this.generateLore());

        itemMeta.getItemFlags().clear();
        this.itemStack.setItemMeta(itemMeta);
        this.itemStack.setDurability((short) durability);
    }

    private List<String> generateLore()
    {
        // Attach the lore
        List<String> lore = Lists.newArrayList();

        Collections.addAll(lore, "", ChatColor.RED + "DMG: " + ChatColor.WHITE + minDmg + " - " + maxDmg, "");

        // Add lore pieces {1, 2, 3, etc}
        if (!this.weaponAttributes.isEmpty())
        {
            lore.addAll(this.weaponAttributes.stream().map(attribute -> attribute.getName() + ": VALUE").collect(Collectors.toList()));
            // TODO ^
        }
        if (this.soulbound)
        {
            Collections.addAll(lore, "", ChatColor.DARK_RED.toString() + ChatColor.ITALIC + "Soulbound");
        }
        if (!this.tradeable)
        {
            Collections.addAll(lore, "", ChatColor.GRAY.toString() + ChatColor.ITALIC + "Untradeable");
        }
        return lore;
    }
}
