package net.dungeonrealms.game.item.items.functional.accessories;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.game.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RandomEnchantTrinketData extends EnchantTrinketData {

    private Map<Item.AttributeType, MinMax> enchantMap = new HashMap<>();

    public RandomEnchantTrinketData() {
        super(null, -1, -1);
        enchantMap.put(Item.ArmorAttributeType.ENERGY_REGEN, new MinMax(1, 3));
        enchantMap.put(Item.ArmorAttributeType.HEALTH_POINTS, new MinMax(1, 30));
        enchantMap.put(Item.ArmorAttributeType.ARMOR, new MinMax(1, 3));
        enchantMap.put(Item.ArmorAttributeType.BLOCK, new MinMax(1, 2));
        enchantMap.put(Item.ArmorAttributeType.DODGE, new MinMax(1, 2));
        enchantMap.put(Item.ArmorAttributeType.DEXTERITY, new MinMax(1, 50));
        enchantMap.put(Item.ArmorAttributeType.VITALITY, new MinMax(1, 50));
        enchantMap.put(Item.ArmorAttributeType.INTELLECT, new MinMax(1, 50));
        enchantMap.put(Item.ArmorAttributeType.FIRE_RESISTANCE, new MinMax(1, 20));
        enchantMap.put(Item.ArmorAttributeType.POISON_RESISTANCE, new MinMax(1, 20));
        enchantMap.put(Item.ArmorAttributeType.ICE_RESISTANCE, new MinMax(1, 20));
        enchantMap.put(Item.ArmorAttributeType.GEM_FIND, new MinMax(1, 5));
        enchantMap.put(Item.ArmorAttributeType.ITEM_FIND, new MinMax(1, 5));

        enchantMap.put(Item.WeaponAttributeType.ARMOR_PENETRATION, new MinMax(1, 5));
        enchantMap.put(Item.WeaponAttributeType.ACCURACY, new MinMax(1, 5));
        enchantMap.put(Item.WeaponAttributeType.BLIND, new MinMax(1, 10));
        enchantMap.put(Item.WeaponAttributeType.CRITICAL_HIT, new MinMax(1, 5));
        enchantMap.put(Item.WeaponAttributeType.LIFE_STEAL, new MinMax(1, 3));
        enchantMap.put(Item.WeaponAttributeType.DAMAGE, new MinMax(1, 5));
    }

    public RandomEnchantTrinketData(Item.AttributeType type, int min, int max) {
        super(type, min, max);
    }


    public Tuple<Item.AttributeType, Integer> getRandomAttribute() {
        List<Item.AttributeType> types = Lists.newArrayList(enchantMap.keySet());

        Item.AttributeType type = types.get(ThreadLocalRandom.current().nextInt(types.size()));
        if (type != null) {
            MinMax minMax = enchantMap.get(type);
            if (minMax != null) {
                int value = ThreadLocalRandom.current().nextInt(minMax.getMax() - minMax.getMin()) + minMax.getMin();
                return new Tuple<>(type, value);
            }
        }

        return null;
    }

    @AllArgsConstructor
    @Getter
    class MinMax {
        int min, max;
    }
}
