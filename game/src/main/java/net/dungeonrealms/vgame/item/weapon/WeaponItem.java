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

    private ItemStack itemStack;

    private EnumItemType itemType;

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

    public WeaponItem()
    {
        this.uuid = UUID.randomUUID();

        this.itemType = EnumItemType.randomItem(false); // Random weapon item

        this.weaponAttributes = EnumWeaponAttribute.random(this.itemTier.getMaxAttributes()); // Random collection of attributes
        this.itemRarity = EnumItemRarity.random(); // Random rarity upon generation
        this.itemTier = EnumItemTier.random(); // Random tier upon generation
        this.material = this.itemTier.getMaterial(this.itemType);

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

    private void calculateAttributes()
    {
        if(this.weaponAttributes.contains(EnumWeaponAttribute.PURE_DAMAGE))
        {

        }
    }

    private void createKey()
    {
        // Create the atomic key (bukkit itemstack)
        this.itemStack = new ItemStack(material);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(this.itemRarity.getColor() + "Test Object");

        // Attach the lore
        List<String> lore = Lists.newArrayList();

        // Add lore pieces {1, 2, 3, etc}
        for (String string : new String[]{"",
                ChatColor.RED + "DMG: " + ChatColor.WHITE + minDmg + " - " + maxDmg,
                this.itemRarity.getColor() + this.itemRarity.getName()})
        {
            lore.add(string);
        }

        // Add weapon attributes to the lore
        lore.addAll(weaponAttributes.stream().map(EnumWeaponAttribute::getName).collect(Collectors.toList()));

        itemMeta.setLore(lore);

        itemMeta.getItemFlags().clear();
        this.itemStack.setItemMeta(itemMeta);
        this.itemStack.setDurability((short) durability);
    }
}
