package net.dungeonrealms.game.mastery;

import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagInt;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

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
        net.minecraft.server.v1_9_R2.Entity nmsEntity = ((CraftEntity) e).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsEntity.c(tag);
        tag.set("NoAI", new NBTTagInt(1));
        nmsEntity.f(tag);
        //Utils.log.info("Nullified " + e.getName() + "'s AI");
    }

}
