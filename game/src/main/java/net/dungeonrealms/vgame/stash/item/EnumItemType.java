package net.dungeonrealms.vgame.stash.item;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumItemType
{
    SWORD("Sword"), POLE_ARM("Polearm"), AXE("Axe"), BOW("Bow"), STAFF("Staff"),

    HELMET("Helmet"), CHESTPLATE("Chestplate"), LEGGINGS("Leggings"), BOOTS("Boots");

    @Getter
    private String name;

    EnumItemType(String name)
    {
        this.name = name;
    }

    public static EnumItemType randomItem(boolean armor)
    {
        List<EnumItemType> itemTypes;
        if (armor)
        {
            itemTypes = Arrays.asList(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
            return itemTypes.get(new Random().nextInt(itemTypes.size()));
        } else
        {
            itemTypes = Arrays.asList(SWORD, POLE_ARM, AXE, BOW, STAFF);
            return itemTypes.get(new Random().nextInt(itemTypes.size()));
        }
    }
}
