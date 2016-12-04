package net.dungeonrealms.frontend.vgame.item.gear.weapon;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.frontend.vgame.item.EnumGameItem;
import net.dungeonrealms.frontend.vgame.item.construct.Item;
import net.dungeonrealms.frontend.vgame.item.construct.ItemConstruction;
import net.dungeonrealms.frontend.vgame.item.construct.gear.EnumGearType;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemRarity;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemTier;
import net.dungeonrealms.frontend.vgame.item.security.UAI;
import net.dungeonrealms.frontend.vgame.item.security.exception.CompoundException;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagDouble;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Giovanni on 30-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemWeapon implements Item {

    /**
     * NBT Format
     * <p>
     * gameItem true
     * itemType
     * itemTier
     * itemRarity
     * gearType
     * {minDamage, maxDamage}
     * {attributes}
     * {soulbound/untradable}
     * atomicId
     */
    @Getter
    private UAI atomicId;

    @Getter
    private ItemStack itemStack;

    @Getter
    private EnumGameItem gameItem = EnumGameItem.WEAPON;

    @Getter
    private EnumItemTier tier;

    @Getter
    private EnumItemRarity rarity;

    @Getter
    private EnumGearType gearType;

    @Getter
    private String customName;

    @Getter
    @Setter
    private double minDmg, maxDmg;

    @Getter
    private boolean soulbound;

    @Getter
    private boolean untradeable;

    public ItemWeapon(ItemConstruction itemConstruction) {
        this.atomicId = new UAI();
        // Get values
        this.tier = itemConstruction.getItemTier();
        this.rarity = itemConstruction.getItemRarity();
        this.gearType = itemConstruction.getGearType();
        this.customName = itemConstruction.getCustomName();
        this.soulbound = itemConstruction.isSoulbound();
        this.untradeable = itemConstruction.isUntradeable();
        // Generate itemstack
        this.itemStack = new ItemStack(Material.AIR);
        ItemMeta itemMeta = this.itemStack.getItemMeta();
        itemMeta.setDisplayName(this.customName);
        // Item lore
        List<String> defaultLore = Lists.newArrayList();
        // Dmg
        defaultLore.add("&cDMG: " + this.getMinDmg() + " &c- " + this.getMaxDmg());
        // Attributes
        // TODO attributes
        // Soulbound / Untradable
        if (this.soulbound) defaultLore.add("&4&oSoulbound");
        if (this.untradeable) defaultLore.add("&7&oUntradable");
        // Fix the lore
        List<String> fixedLore = Lists.newArrayList();
        fixedLore.addAll(defaultLore.stream().map(string -> ChatColor.translateAlternateColorCodes('&', string)).collect(Collectors.toList()));
        itemMeta.setLore(fixedLore);
        for (ItemFlag itemFlag : ItemFlag.values()) {
            itemMeta.addItemFlags(itemFlag);
        }
        this.itemStack.setItemMeta(itemMeta);

        // Generate the NBT
        net.minecraft.server.v1_9_R2.ItemStack minecraftItem = CraftItemStack.asNMSCopy(this.itemStack);
        NBTTagCompound tagCompound = minecraftItem.getTag() == null ? new NBTTagCompound() : minecraftItem.getTag();
        // Remove Minecraft attribute data
        tagCompound.set("AttributeModifiers", new NBTTagList());
        // Add our own
        tagCompound.set("gameItem", new NBTTagString("true"));
        tagCompound.set("itemType", new NBTTagString(this.gameItem.name()));
        tagCompound.set("itemTier", new NBTTagString(this.tier.name()));
        tagCompound.set("itemRarity", new NBTTagString(this.rarity.name()));
        tagCompound.set("gearType", new NBTTagString(this.gearType.name()));
        tagCompound.set("minDmg", new NBTTagDouble(this.minDmg));
        tagCompound.set("maxDmg", new NBTTagDouble(this.maxDmg));
        // Change the attributelist to a json string
        // TODO tagCompound.set("attributes", new NBTTagString(new Gson().toJson(this.attributeList)));
        tagCompound.setBoolean("soulbound", this.soulbound);
        tagCompound.setBoolean("untradeable", this.untradeable);
        minecraftItem.setTag(tagCompound);
        // Attach the atomic id
        try {
            this.atomicId.attachTo(minecraftItem);
        } catch (CompoundException e) {
            e.printStackTrace();
        }
    }

    public ItemWeapon(ItemStack itemStack) throws CompoundException {
        net.minecraft.server.v1_9_R2.ItemStack minecraftItem = CraftItemStack.asNMSCopy(itemStack);
        if (minecraftItem.hasTag() || minecraftItem.getTag() != null) {
            // Is it a game item?
            if (minecraftItem.getTag().hasKey("gameItem")) {
                // Does it have an atomic id?
                if (minecraftItem.getTag().hasKey("atomicId")) {
                    // Is it a weapon?
                    if (minecraftItem.getTag().getString("itemType").equalsIgnoreCase(EnumGameItem.WEAPON.name())) {
                        // Read the NBT and construct the weapon
                        this.gameItem = EnumGameItem.valueOf(minecraftItem.getTag().getString("itemType"));
                        this.tier = EnumItemTier.valueOf(minecraftItem.getTag().getString("itemTier"));
                        this.rarity = EnumItemRarity.valueOf(minecraftItem.getTag().getString("itemRarity"));
                        this.gearType = EnumGearType.valueOf(minecraftItem.getTag().getString("gearType"));
                        this.minDmg = minecraftItem.getTag().getDouble("minDmg");
                        this.maxDmg = minecraftItem.getTag().getDouble("maxDmg");
                        this.soulbound = minecraftItem.getTag().getBoolean("soulbound");
                        this.untradeable = minecraftItem.getTag().getBoolean("untradeable");
                    }
                } else
                    throw new CompoundException(minecraftItem);
            }
        } else
            throw new CompoundException(minecraftItem);
    }
}
