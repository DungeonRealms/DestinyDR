package net.dungeonrealms.vgame.item.meta;

import lombok.Getter;
import net.dungeonrealms.vgame.item.EnumItemTier;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Giovanni on 3-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AttributeMeta
{
    @Getter
    private EnumItemTier itemTier;

    @Getter
    private boolean percentage;

    @Getter
    private double valueX;

    @Getter
    private double valueY;

    public AttributeMeta(EnumItemTier enumItemTier, double valueX, double valueY, boolean percentage)
    {
        this.itemTier = enumItemTier;
        this.valueX = valueX;
        this.valueY = valueY;
        this.percentage = percentage;
    }

    public AttributeMeta(EnumItemTier enumItemTier, double valueX, double valueY)
    {
        this.itemTier = enumItemTier;
        this.valueX = valueX;
        this.valueY = valueY;
        this.percentage = false;
    }

    public double returnRandomValue()
    {
        return ThreadLocalRandom.current().nextDouble(this.valueX, this.valueY);
    }
}
