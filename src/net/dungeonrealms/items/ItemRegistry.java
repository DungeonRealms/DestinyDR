package net.dungeonrealms.items;

import net.dungeonrealms.mechanics.ReflectionAPI;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.Item;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Created by Kieran on 9/27/2015.
 */
public final class ItemRegistry {

    int id = 500;

    /**
     * Registers a new item and overrides the old one if present.
     *
     * @param id the id
     * @param item the item
     */
    public void register(String id, Item item) {
        if (!id.startsWith("minecraft:")) {
            Item.REGISTRY.a(this.id++, new MinecraftKey(id), item);
        } else {
            /**
             * We will also override the fields in the Items class to avoid issues.
             */
            RegistryMaterials registry = Item.REGISTRY;

            try {
                MinecraftKey key = new MinecraftKey(id);

                Item item0 = (Item) registry.get(key);
                Integer id0 = registry.b(item0);

                Map<Object, Object> map0 = null;
                RegistryID map1 = null;

                Field field0 = ReflectionAPI.findField(RegistrySimple.class, Map.class, 0);
                field0.setAccessible(true);

                Field field1 = ReflectionAPI.findField(RegistryMaterials.class, RegistryID.class, 0);
                field1.setAccessible(true);

                map0 = (Map<Object, Object>) field0.get(registry);
                map1 = (RegistryID) field1.get(registry);

                map0.remove(key);
                map0.put(key, item);
                map0.put(new MinecraftKey("removed:" + id.replace("minecraft:", "")), item0);
                map1.a(item, id0);

                for (Field field : Items.class.getFields()) {
                    int modifiers = field.getModifiers();

                    if (Modifier.isStatic(modifiers)) {
                        try {
                            if (field.get(null).equals(item0)) {
                                Field field2 = Field.class.getDeclaredField("modifiers");
                                field2.setAccessible(true);
                                field2.set(field, modifiers & ~Modifier.FINAL);

                                field.setAccessible(true);
                                field.set(null, item);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                Field field2 = ReflectionAPI.findField(CraftingStatistic.class, Item.class, 0);
                field2.setAccessible(true);

                List<Statistic> list = Arrays.asList(StatisticList.USE_ITEM_COUNT);

                for (Statistic aList : list) {
                    CraftingStatistic stat = (CraftingStatistic) aList;

                    if (stat != null && field2.get(stat) == item0) {
                        Field field3 = Field.class.getDeclaredField("modifiers");
                        field3.setAccessible(true);
                        field3.set(field2, field2.getModifiers() & ~Modifier.FINAL);

                        field2.setAccessible(true);
                        field2.set(stat, item);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}