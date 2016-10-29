package net.dungeonrealms.vgame.item;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.api.collection.AtomicCollection;
import org.bukkit.Material;

import java.util.Map;


/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumItemTier
{
    ONE(0, 2), TWO(1, 3), THREE(2, 4), FOUR(3, 5), FIVE(4, 6);

    @Getter
    private int maxAttributes;

    @Getter
    private Map<EnumItemType, Material> materialMap;

    private static AtomicCollection<EnumItemTier> atomicCollection = new AtomicCollection<>();

    private static boolean loaded;

    EnumItemTier(int id, int maxAttributes)
    {
        this.maxAttributes = maxAttributes;
        this.materialMap = Maps.newHashMap();

        this.materialMap.put(EnumItemType.BOW, Material.BOW);

        switch (id)
        {
            case 0:
                this.materialMap.put(EnumItemType.SWORD, Material.WOOD_SWORD);
                this.materialMap.put(EnumItemType.POLE_ARM, Material.WOOD_SPADE);
                this.materialMap.put(EnumItemType.AXE, Material.WOOD_AXE);
                this.materialMap.put(EnumItemType.STAFF, Material.WOOD_HOE);
                this.materialMap.put(EnumItemType.HELMET, Material.LEATHER_HELMET);
                this.materialMap.put(EnumItemType.CHESTPLATE, Material.LEATHER_CHESTPLATE);
                this.materialMap.put(EnumItemType.LEGGINGS, Material.LEATHER_LEGGINGS);
                this.materialMap.put(EnumItemType.BOOTS, Material.LEATHER_BOOTS);
                break;
            case 1:
                this.materialMap.put(EnumItemType.SWORD, Material.STONE_SWORD);
                this.materialMap.put(EnumItemType.POLE_ARM, Material.STONE_SPADE);
                this.materialMap.put(EnumItemType.AXE, Material.STONE_AXE);
                this.materialMap.put(EnumItemType.STAFF, Material.STONE_HOE);
                this.materialMap.put(EnumItemType.HELMET, Material.CHAINMAIL_HELMET);
                this.materialMap.put(EnumItemType.CHESTPLATE, Material.CHAINMAIL_CHESTPLATE);
                this.materialMap.put(EnumItemType.LEGGINGS, Material.CHAINMAIL_LEGGINGS);
                this.materialMap.put(EnumItemType.BOOTS, Material.CHAINMAIL_BOOTS);
                break;
            case 2:
                this.materialMap.put(EnumItemType.SWORD, Material.IRON_SWORD);
                this.materialMap.put(EnumItemType.POLE_ARM, Material.IRON_SPADE);
                this.materialMap.put(EnumItemType.AXE, Material.IRON_AXE);
                this.materialMap.put(EnumItemType.STAFF, Material.IRON_HOE);
                this.materialMap.put(EnumItemType.HELMET, Material.IRON_HELMET);
                this.materialMap.put(EnumItemType.CHESTPLATE, Material.IRON_CHESTPLATE);
                this.materialMap.put(EnumItemType.LEGGINGS, Material.IRON_LEGGINGS);
                this.materialMap.put(EnumItemType.BOOTS, Material.IRON_BOOTS);
                break;
            case 3:
                this.materialMap.put(EnumItemType.SWORD, Material.GOLD_SWORD);
                this.materialMap.put(EnumItemType.POLE_ARM, Material.GOLD_SPADE);
                this.materialMap.put(EnumItemType.AXE, Material.GOLD_AXE);
                this.materialMap.put(EnumItemType.STAFF, Material.GOLD_HOE);
                this.materialMap.put(EnumItemType.HELMET, Material.GOLD_HELMET);
                this.materialMap.put(EnumItemType.CHESTPLATE, Material.GOLD_CHESTPLATE);
                this.materialMap.put(EnumItemType.LEGGINGS, Material.GOLD_LEGGINGS);
                this.materialMap.put(EnumItemType.BOOTS, Material.GOLD_BOOTS);
                break;
            case 4:
                this.materialMap.put(EnumItemType.SWORD, Material.DIAMOND_SWORD);
                this.materialMap.put(EnumItemType.POLE_ARM, Material.DIAMOND_SPADE);
                this.materialMap.put(EnumItemType.AXE, Material.DIAMOND_AXE);
                this.materialMap.put(EnumItemType.STAFF, Material.DIAMOND_HOE);
                this.materialMap.put(EnumItemType.HELMET, Material.DIAMOND_HELMET);
                this.materialMap.put(EnumItemType.CHESTPLATE, Material.DIAMOND_CHESTPLATE);
                this.materialMap.put(EnumItemType.LEGGINGS, Material.DIAMOND_LEGGINGS);
                this.materialMap.put(EnumItemType.BOOTS, Material.DIAMOND_BOOTS);
                break;
        }
    }

    public Material getMaterial(EnumItemType itemType)
    {
        return this.materialMap.get(itemType);
    }

    public static EnumItemTier random()
    {
        if (loaded)
        {
            return atomicCollection.next();
        } else
        {
            // Weight is not final
            atomicCollection.getMap().get().put(0.8, ONE);
            atomicCollection.getMap().get().put(0.6, TWO);
            atomicCollection.getMap().get().put(0.4, THREE);
            atomicCollection.getMap().get().put(0.2, FOUR);
            atomicCollection.getMap().get().put(0.05, FIVE);
            loaded = true;
            return atomicCollection.next();
        }
    }
}
