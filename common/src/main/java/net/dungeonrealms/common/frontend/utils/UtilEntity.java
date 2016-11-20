package net.dungeonrealms.common.frontend.utils;

import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Created by Evoltr on 11/20/2016.
 */
public class UtilEntity {

    private static Random random = new Random();

    public static int getRandomEntityID() {
        return random.nextInt(100000);
    }

    public static Packet spawnArmorStand(Location location, String name, int entityID) {
        EntityArmorStand entityArmorStand = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle());
        entityArmorStand.setLocation(location.getX(), location.getY(), location.getZ(), 0, 0);
        if (name.equals("clearline")) {
            entityArmorStand.setCustomNameVisible(false);
        } else {
            entityArmorStand.setCustomNameVisible(true);
            entityArmorStand.setCustomName(name);
        }
        entityArmorStand.setInvisible(true);
        try {
            Field field = Entity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.setInt(entityArmorStand, entityID);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new PacketPlayOutSpawnEntityLiving(entityArmorStand);
    }

    public static Packet destroyEntity(int entityID) {
        return new PacketPlayOutEntityDestroy(new int[]{entityID});
    }
}
