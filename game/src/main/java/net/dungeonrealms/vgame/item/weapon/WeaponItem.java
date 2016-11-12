package net.dungeonrealms.vgame.item.weapon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.dungeonrealms.api.creature.EnumCreatureType;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.item.*;
import net.dungeonrealms.vgame.item.attribute.AttributeMeta;
import net.dungeonrealms.vgame.item.weapon.attribute.EnumWeaponAttribute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class WeaponItem implements IStack
{
    // TODO weapon min & max dmg

    @Getter
    private UUID uniqueId;

    @Getter
    private Material material;

    @Getter
    private EnumItemRarity itemRarity;

    @Getter
    private EnumItemTier itemTier;

    @Getter
    private EnumItemTier attributeTier;

    @Getter
    private double minDmg, maxDmg; // TODO

    @Getter
    private int durability = 100; // Default value ?

    @Getter
    private List<EnumWeaponAttribute> weaponAttributes;

    @Getter
    private String name;

    @Getter
    private ItemStack itemStack;

    @Getter
    private EnumItemType itemType;

    @Getter
    private boolean soulbound;

    @Getter
    private boolean tradeable;

    public WeaponItem(boolean soulbound, boolean tradeable)
    {
        this.uniqueId = UUID.randomUUID();

        this.itemType = EnumItemType.randomItem(false); // Random weapon item
        this.itemTier = EnumItemTier.random(); // Random tier upon generation

        this.weaponAttributes = EnumWeaponAttribute.random(this.itemTier.getMaxAttributes()); // Random collection of attributes
        this.itemRarity = EnumItemRarity.random(); // Random rarity upon generation
        this.attributeTier = EnumItemTier.random(); // Random attribute tier, different than the item tier
        this.material = this.itemTier.getMaterial(this.itemType);

        this.soulbound = soulbound;
        this.tradeable = tradeable;

        // Remove duplicated attributes
        Set<EnumWeaponAttribute> attributeSet = Sets.newHashSet();
        attributeSet.addAll(this.weaponAttributes);
        this.weaponAttributes.clear();
        this.weaponAttributes.addAll(attributeSet);

        this.createKey(); // Actual item

        Game.getGame().getRegistryHandler().getWeaponRegistry().getMap().put(getItemStack(), this);
    }

    // Constructing a new weapon out of the database
    public WeaponItem(UUID uuid,
                      Material material,
                      EnumItemRarity rarity,
                      EnumItemTier itemTier,
                      EnumItemTier attributeTier,
                      EnumItemType type,
                      int durability,
                      String name,
                      List<EnumWeaponAttribute> attributes,
                      boolean soulbound,
                      boolean tradeable,
                      int minDmg,
                      int maxDmg)
    {
        this.uniqueId = uuid;
        this.material = material;
        this.itemRarity = rarity;
        this.itemTier = itemTier;
        this.attributeTier = attributeTier;
        this.itemType = type;
        this.durability = durability;
        this.name = name;
        this.weaponAttributes = attributes;
        this.soulbound = soulbound;
        this.tradeable = tradeable;
        this.minDmg = minDmg;
        this.maxDmg = maxDmg;

        this.createKey(); // Actual item

    }

    private void createKey()
    {
        // Create the atomic key (bukkit itemstack)
        this.itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(this.itemTier.getChatColor() + "Test Object");
        for (ItemFlag itemFlag : ItemFlag.values())
        {
            itemMeta.addItemFlags(itemFlag);
        }
        // Attach the lore
        itemMeta.setLore(this.generateLore());
        this.itemStack.setItemMeta(itemMeta);
    }

    private List<String> generateLore()
    {
        // Attach the lore
        List<String> lore = Lists.newArrayList();

        Collections.addAll(lore, "", ChatColor.RED + "DMG: " + Math.round(minDmg) + " - " + Math.round(maxDmg));

        // Add lore pieces {1, 2, 3, etc}
        if (!this.weaponAttributes.isEmpty())
        {
            this.weaponAttributes.stream().filter(weaponAttribute -> weaponAttribute != EnumWeaponAttribute.EMPTY).forEach(weaponAttribute ->
            {
                for (AttributeMeta attributeMeta : weaponAttribute.getAttributeMetas())
                    if (attributeMeta.getItemTier() == this.itemTier)
                    {
                        if (attributeMeta.isPercentage())
                            Collections.addAll(lore, "", weaponAttribute.getName() + ": " + Math.round(attributeMeta.getValueY()) + "%");
                        else
                            Collections.addAll(lore, "",  weaponAttribute.getName() + ": " + "+" + Math.round(attributeMeta.getValueY()));
                    }
            });
        }
        Collections.addAll(lore, "", this.itemRarity.getColor() + this.itemRarity.getName());
        if (this.soulbound)
        {
            Collections.addAll(lore, "", ChatColor.DARK_RED.toString() + ChatColor.ITALIC + "Soulbound");
        }
        if (!this.tradeable)
        {
            Collections.addAll(lore, ChatColor.GRAY.toString() + ChatColor.ITALIC + "Untradeable");
        }
        return lore;
    }

    public double calculateDamage(EnumCreatureType enumCreatureType)
    {
        double damage = 0;

        // Generate a random double between min & max DMG
        damage += ThreadLocalRandom.current().nextDouble(this.minDmg, this.maxDmg);

        // Calculate additional attribute data
        for (EnumWeaponAttribute attribute : this.weaponAttributes)
            for (AttributeMeta attributeMeta : attribute.getAttributeMetas())
            {
                if (attributeMeta.getItemTier() == this.attributeTier)
                {
                    if (attributeMeta.isPercentage()) // Is it a percentage?
                    {
                        // Add % damage, if Y = 50 & damage = 2 it would be 50 / 100 * 2 = 1
                        damage += attributeMeta.getValueY() / 100 * damage;
                    } else
                    {
                        // Is the attribute a vs Player or vs Monster attribute?
                        if (attribute != EnumWeaponAttribute.MON_DMG || attribute != EnumWeaponAttribute.PLAYER_DMG)
                        {
                            damage += attributeMeta.returnRandomValue();
                        } else
                        {
                            // Is the attribute vs Players & is the target creature a player?
                            if (attribute == EnumWeaponAttribute.PLAYER_DMG)
                            {
                                if (enumCreatureType == EnumCreatureType.PLAYER)
                                {
                                    damage += attributeMeta.returnRandomValue();
                                }
                            }
                            // Is the attribute vs Monsters & is the target creature a monster?
                            if (attribute == EnumWeaponAttribute.MON_DMG)
                            {
                                if (enumCreatureType == EnumCreatureType.ENTITY)
                                {
                                    damage += attributeMeta.returnRandomValue();
                                }
                            }
                        }
                    }
                }
            }
        return damage;
    }
}
