package net.dungeonrealms.vgame.stash.item.meta;

import lombok.Getter;
import net.dungeonrealms.vgame.stash.item.EnumItemType;
import net.dungeonrealms.vgame.stash.item.EnumItemTier;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Giovanni on 13-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DamageMeta
{
    @Getter
    private EnumItemType itemType;

    @Getter
    private EnumItemTier itemTier;

    public DamageMeta(EnumItemType itemType, EnumItemTier itemTier)
    {
        this.itemType = itemType;
        this.itemTier = itemTier;
    }

    public double x()
    {
        return new DamageCalculator(this.itemType, this.itemTier).calculateX();
    }

    public double y()
    {
        return new DamageCalculator(this.itemType, this.itemTier).calculateY();
    }
}

class DamageCalculator
{
    private EnumItemType itemType;

    private EnumItemTier itemTier;

    public DamageCalculator(EnumItemType itemType, EnumItemTier itemTier)
    {
        this.itemType = itemType;
        this.itemTier = itemTier;
    }

    private double getValueX()
    {
        if (this.itemType == EnumItemType.AXE || this.itemType == EnumItemType.SWORD)
        {
            switch (itemTier)
            {
                case ONE:
                    return 1;
                case TWO:
                    return 10;
                case THREE:
                    return 25;
                case FOUR:
                    return 65;
                case FIVE:
                    return 130;
                default:
                    break;

            }
        }
        if (this.itemType == EnumItemType.POLE_ARM || this.itemType == EnumItemType.STAFF)
        {
            switch (itemTier)
            {
                case ONE:
                    return 1;
                case TWO:
                    return 5;
                case THREE:
                    return 12;
                case FOUR:
                    return 32;
                case FIVE:
                    return 65;
                default:
                    break;
            }
        }
        if (this.itemType == EnumItemType.BOW)
        {
            switch (itemTier)
            {
                case ONE:
                    return 1;
                case TWO:
                    return 10;
                case THREE:
                    return 25;
                case FOUR:
                    return 65;
                case FIVE:
                    return 130;
                default:
                    break;
            }
        }
        return 0;
    }

    private double getValueZ()
    {
        if (this.itemType == EnumItemType.AXE || this.itemType == EnumItemType.SWORD)
        {
            switch (itemTier)
            {
                case ONE:
                    return 2;
                case TWO:
                    return 12;
                case THREE:
                    return 30;
                case FOUR:
                    return 80;
                case FIVE:
                    return 140;
                default:
                    break;
            }
        }
        if (this.itemType == EnumItemType.POLE_ARM || this.itemType == EnumItemType.STAFF)
        {
            switch (itemTier)
            {
                case ONE:
                    return 1;
                case TWO:
                    return 6;
                case THREE:
                    return 15;
                case FOUR:
                    return 40;
                case FIVE:
                    return 70;
                default:
                    break;
            }
        }
        if (this.itemType == EnumItemType.BOW)
        {
            switch (itemTier)
            {
                case ONE:
                    return 2;
                case TWO:
                    return 12;
                case THREE:
                    return 30;
                case FOUR:
                    return 80;
                case FIVE:
                    return 140;
                default:
                    break;
            }
        }
        return 0;
    }

    private double getValueY()
    {
        if (this.itemType == EnumItemType.AXE || this.itemType == EnumItemType.SWORD)
        {
            switch (itemTier)
            {
                case ONE:
                    return 8;
                case TWO:
                    return 17;
                case THREE:
                    return 45;
                case FOUR:
                    return 125;
                case FIVE:
                    return 210;
                default:
                    break;
            }
        }
        if (this.itemType == EnumItemType.POLE_ARM || this.itemType == EnumItemType.STAFF)
        {
            switch (itemTier)
            {
                case ONE:
                    return 4;
                case TWO:
                    return 9;
                case THREE:
                    return 23;
                case FOUR:
                    return 63;
                case FIVE:
                    return 115;
                default:
                    break;
            }
        }
        if (this.itemType == EnumItemType.BOW)
        {
            switch (itemTier)
            {
                case ONE:
                    return 5;
                case TWO:
                    return 17;
                case THREE:
                    return 45;
                case FOUR:
                    return 125;
                case FIVE:
                    return 210;
                default:
                    break;
            }
        }
        return 0;
    }

    double calculateX()
    {
        double x = ThreadLocalRandom.current().nextDouble(this.getValueX(), this.getValueZ());
        return x;
    }

    double calculateY()
    {
        double y = ThreadLocalRandom.current().nextDouble(this.getValueZ(), this.getValueY());
        return y;
    }
}

