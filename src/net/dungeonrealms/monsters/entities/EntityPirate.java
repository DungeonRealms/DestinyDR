package net.dungeonrealms.monsters.entities;

import net.minecraft.server.v1_8_R3.*;

/**
 * Created by Nick on 9/17/2015.
 */
public class EntityPirate extends EntityZombie {
    public EntityPirate(World world) {
        super(world);

        NBTTagCompound tag = this.getNBTTag() == null ? new NBTTagCompound() : this.getNBTTag();
        this.c(tag);
        tag.set("type", new NBTTagString("mob"));
        tag.set("level", new NBTTagInt(1));
        this.f(tag);
    }


    @Override
    protected String z() {
        return "mob.zombie.say";
    }

    @Override
    protected String bo() {
        return "random.bowhit";
    }

    @Override
    protected String bp() {
        return "mob.zombie.death";
    }
}
