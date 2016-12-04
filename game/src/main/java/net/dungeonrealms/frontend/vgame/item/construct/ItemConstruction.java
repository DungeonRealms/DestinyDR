package net.dungeonrealms.frontend.vgame.item.construct;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.item.EnumGameItem;
import net.dungeonrealms.frontend.vgame.item.construct.gear.EnumGearType;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemRarity;
import net.dungeonrealms.frontend.vgame.item.construct.generic.EnumItemTier;

/**
 * Created by Giovanni on 4-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ItemConstruction {

    @Getter
    private EnumItemRarity itemRarity;

    @Getter
    private EnumItemTier itemTier;

    @Getter
    private EnumGearType gearType;

    @Getter
    private String customName;

    @Getter
    private boolean soulbound;

    @Getter
    private boolean untradeable;

    public ItemConstruction(EnumGameItem gameItem, EnumItemRarity itemRarity, boolean soulbound, boolean untradeable) {
        this.itemRarity = itemRarity;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
        // Randomized data
        this.itemTier = EnumItemTier.random();
        switch (gameItem) {
            case WEAPON:
                this.gearType = EnumGearType.randomWeapon();
                break;
            case ARMOR:
                this.gearType = EnumGearType.randomArmor();
                break;
            default:
                break;
        }
        // name
    }

    public ItemConstruction(EnumGameItem gameItem, EnumItemTier itemTier, boolean soulbound, boolean untradeable) {
        this.itemTier = itemTier;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
        // Randomized data
        this.itemRarity = EnumItemRarity.random();
        switch (gameItem) {
            case WEAPON:
                this.gearType = EnumGearType.randomWeapon();
                break;
            case ARMOR:
                this.gearType = EnumGearType.randomArmor();
                break;
            default:
                break;
        }
        // name
    }

    public ItemConstruction(EnumGameItem gameItem, String customName, boolean soulbound, boolean untradeable) {
        this.customName = customName;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
        // Randomized data
        this.itemTier = EnumItemTier.random();
        this.itemRarity = EnumItemRarity.random();
        switch (gameItem) {
            case WEAPON:
                this.gearType = EnumGearType.randomWeapon();
                break;
            case ARMOR:
                this.gearType = EnumGearType.randomArmor();
                break;
            default:
                break;
        }
    }

    public ItemConstruction(EnumGearType gearType, boolean soulbound, boolean untradeable) {
        this.gearType = gearType;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
        // Randomized data
        this.itemTier = EnumItemTier.random();
        this.itemRarity = EnumItemRarity.random();
        // name
    }

    public ItemConstruction(EnumGameItem gameItem, EnumItemRarity rarity, EnumItemTier itemTier, boolean soulbound, boolean untradeable) {
        this.itemRarity = rarity;
        this.itemTier = itemTier;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
        // Randomized data
        switch (gameItem) {
            case WEAPON:
                this.gearType = EnumGearType.randomWeapon();
                break;
            case ARMOR:
                this.gearType = EnumGearType.randomArmor();
                break;
            default:
                break;
        }
        // name
    }

    public ItemConstruction(EnumItemRarity rarity, EnumItemTier itemTier, EnumGearType gearType, boolean soulbound, boolean untradeable) {
        this.itemRarity = rarity;
        this.itemTier = itemTier;
        this.gearType = gearType;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
        // name
    }

    public ItemConstruction(EnumItemRarity rarity, EnumItemTier itemTier, EnumGearType gearType, String customName, boolean soulbound, boolean untradeable) {
        this.itemRarity = rarity;
        this.itemTier = itemTier;
        this.gearType = gearType;
        this.customName = customName;
        this.soulbound = soulbound;
        this.untradeable = untradeable;
    }
}
