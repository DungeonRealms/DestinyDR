package net.dungeonrealms.api.creature.lib.craft.horse;

import net.dungeonrealms.api.creature.ICreature;
import net.minecraft.server.v1_9_R2.EntityHorse;
import net.minecraft.server.v1_9_R2.EnumHorseType;
import org.bukkit.entity.Horse;

/**
 * Created by Giovanni on 25-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public interface IHorse extends ICreature {
    default void setColour(Horse.Color colour) {
        ((Horse) this.getEntity()).setColor(colour);
    }

    default void setType(EnumHorseType horseType) {
        ((EntityHorse) this.getEntity()).setType(horseType);
    }
}
