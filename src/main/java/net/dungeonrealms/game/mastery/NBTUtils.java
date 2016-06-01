package net.dungeonrealms.game.mastery;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;

/**
 * Created by Nick on 9/17/2015.
 */
public class NBTUtils {

    /**
     * Will completely remove the Entities AI.
     *
     * @param e
     * @since 1.0
     */
    public static void nullifyAI(Entity e) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) e).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag() == null ? new NBTTagCompound() : nmsEntity.getNBTTag();
        nmsEntity.c(tag);
        tag.set("NoAI", new NBTTagInt(1));
        nmsEntity.f(tag);
        Utils.log.info("Nullified " + e.getName() + "'s AI");
    }

}
