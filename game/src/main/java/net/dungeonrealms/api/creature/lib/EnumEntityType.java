package net.dungeonrealms.api.creature.lib;

import lombok.Getter;

/**
 * Created by Giovanni on 24-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public enum EnumEntityType
{
    OCELOT(EnumEquipmentType.NONE),
    SPIDER(EnumEquipmentType.NONE),
    SKELETON(EnumEquipmentType.GENERIC),
    WITHER_SKELETON(EnumEquipmentType.GENERIC),
    PIG(EnumEquipmentType.SADDLE),
    COW(EnumEquipmentType.NONE),
    SHEEP(EnumEquipmentType.NONE),
    IRON_GOLEM(EnumEquipmentType.NONE),
    SNOW_GOLEM(EnumEquipmentType.NONE),
    HORSE(EnumEquipmentType.SADDLE),
    ZOMBIE(EnumEquipmentType.GENERIC),
    WITHER(EnumEquipmentType.NONE),
    END_DRAGON(EnumEquipmentType.NONE),
    CRYSTAL(EnumEquipmentType.NONE);

    @Getter
    private EnumEquipmentType equipmentType;

    EnumEntityType(EnumEquipmentType equipmentType)
    {
        this.equipmentType = equipmentType;
    }
}
