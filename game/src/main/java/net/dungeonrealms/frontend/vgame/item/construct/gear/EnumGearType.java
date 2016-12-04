package net.dungeonrealms.frontend.vgame.item.construct.gear;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Giovanni on 4-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumGearType {

    SWORD(0),
    BOW(1),
    AXE(2),
    POLE_ARM(3),
    HELMET(4),
    CHESTPLATE(5),
    LEGGINGS(6),
    BOOTS(7);

    @Getter
    private int id;

    EnumGearType(int id) {
        this.id = id;
    }

    public static EnumGearType random() {
        int id = new Random().nextInt(values().length);
        return getById(id);
    }

    public static EnumGearType randomWeapon() {
        List<EnumGearType> weaponList = Arrays.asList(SWORD, BOW, AXE, POLE_ARM);
        int id = new Random().nextInt(weaponList.size());
        return getById(id);
    }

    public static EnumGearType randomArmor() {
        List<EnumGearType> armorList = Arrays.asList(HELMET, CHESTPLATE, LEGGINGS, BOOTS);
        int id = new Random().nextInt(armorList.size());
        return getById(id);
    }

    public static EnumGearType getById(int id) {
        for (EnumGearType gearType : values()) {
            if (gearType.getId() == id) {
                return gearType;
            }
        }
        return null;
    }
}
