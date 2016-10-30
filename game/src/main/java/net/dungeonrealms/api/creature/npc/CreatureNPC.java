package net.dungeonrealms.api.creature.npc;

import net.dungeonrealms.api.creature.CreatureType;
import net.dungeonrealms.api.creature.ICreature;
import net.minecraft.server.v1_9_R2.Entity;

/**
 * Created by Matthew E on 10/29/2016 at 12:45 PM.
 */
public class CreatureNPC implements ICreature {

    @Override
    public Entity getEntity() {
        return null;
    }

    @Override
    public CreatureType getCreatureType() {
        return CreatureType.NPC;
    }
}
