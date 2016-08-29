package net.dungeonrealms.game.mastery;

import net.minecraft.server.v1_9_R2.BiomeBase;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.EntityTypes;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Nick on 9/17/2015.
 * From Bukkit, to register our NPCs.
 */
public class NMSUtils {

    public void registerEntity(String name, int id, Class<? extends EntityInsentient> nmsClass,
                               Class<? extends EntityInsentient> customClass) {
        try {

            List<Map<?, ?>> dataMaps = new ArrayList<>();
            for (Field f : EntityTypes.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals(Map.class.getSimpleName())) {
                    f.setAccessible(true);
                    dataMaps.add((Map<?, ?>) f.get(null));
                }
            }

            if (dataMaps.get(2).containsKey(id)) {
                dataMaps.get(0).remove(name);
                dataMaps.get(2).remove(id);
            }

            Method method = EntityTypes.class.getDeclaredMethod("a", Class.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(null, customClass, name, id);
            for (Field f : BiomeBase.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals(BiomeBase.class.getSimpleName())) {
                    if (f.get(null) != null) {

                        for (Field list : BiomeBase.class.getDeclaredFields()) {
                            if (list.getType().getSimpleName().equals(List.class.getSimpleName())) {
                                list.setAccessible(true);
                                @SuppressWarnings("unchecked")
                                List<BiomeBase.BiomeMeta> metaList = (List<BiomeBase.BiomeMeta>) list.get(f.get(null));

                                for (BiomeBase.BiomeMeta meta : metaList) {
                                    Field clazz = BiomeBase.BiomeMeta.class.getDeclaredFields()[0];
                                    if (clazz.get(meta).equals(nmsClass)) {
                                        clazz.set(meta, customClass);
                                    }
                                }
                            }
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static net.minecraft.server.v1_9_R2.Entity getNMSEntity(org.bukkit.entity.Entity ent) {
        return (((CraftEntity) ent).getHandle());
    }
}
