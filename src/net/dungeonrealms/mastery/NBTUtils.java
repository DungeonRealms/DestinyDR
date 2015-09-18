package net.dungeonrealms.mastery;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagInt;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffectType;

/**
 * Created by Nick on 9/17/2015.
 */
public class NBTUtils {
    /**
     * This method is used to create buffs.
     *
     * @param e
     * @param type
     * @param radius
     * @param duration
     * @since 1.0
     */
    public static void buffEntity(Entity e, PotionEffectType type, int radius, int duration) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) e).getHandle();
        NBTTagCompound tag = nmsEntity.getNBTTag() == null ? new NBTTagCompound() : nmsEntity.getNBTTag();
        nmsEntity.c(tag);
        tag.set("type", new NBTTagString("buff"));
        tag.set("radius", new NBTTagInt(radius));
        tag.set("duration", new NBTTagInt(duration));
        tag.set("effectType", new NBTTagString(type.getName()));
        nmsEntity.f(tag);
        Utils.log.info("Buffing " + e.getName() + "'s AI " + tag.getString("type") + tag.getInt("radius"));
    }

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
