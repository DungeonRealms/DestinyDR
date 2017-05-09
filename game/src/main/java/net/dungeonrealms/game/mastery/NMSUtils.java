package net.dungeonrealms.game.mastery;

import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.EntityTypes;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * NMSUtils - Utils for interacting with NMS.
 * 
 * Redone on May 5th, 2017.
 * @author Kneesnap
 */
public class NMSUtils {

    public static void registerEntity(String name, int id, Class<? extends EntityInsentient> customClass) {
        try {

            List<Map<?, ?>> dataMaps = new ArrayList<>();
            for (Field f : EntityTypes.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
                    f.setAccessible(true);
                    dataMaps.add((Map<?, ?>) f.get(null));
                }
            }
            
            // Remove existing entities with this ids.
            // It's ok to register multiple entities with the same id this way.
            if (dataMaps.get(2).containsKey(id)) {
                dataMaps.get(0).remove(name);
                dataMaps.get(2).remove(id);
            }

            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id); // Register the entity in NMS.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static net.minecraft.server.v1_9_R2.Entity getNMSEntity(org.bukkit.entity.Entity ent) {
        return (((CraftEntity) ent).getHandle());
    }
}
