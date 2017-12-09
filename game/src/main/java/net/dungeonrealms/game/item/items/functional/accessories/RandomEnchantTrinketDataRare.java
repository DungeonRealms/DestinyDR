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

public class RandomEnchantTrinketDataRare extends EnchantTrinketData {

    private Map<Item.AttributeType, MinMax> enchantMap = new HashMap<>();

    public RandomEnchantTrinketDataRare() {
        super(null, -1, -1);
        enchantMap.put(Item.ArmorAttributeType.ENERGY_REGEN, new MinMax(4, 6));
        enchantMap.put(Item.ArmorAttributeType.HEALTH_POINTS, new MinMax(500, 800));
        enchantMap.put(Item.ArmorAttributeType.ARMOR, new MinMax(10, 16));
        enchantMap.put(Item.ArmorAttributeType.DAMAGE, new MinMax(10, 16));
        enchantMap.put(Item.ArmorAttributeType.BLOCK, new MinMax(6, 12));
        enchantMap.put(Item.ArmorAttributeType.DODGE, new MinMax(6, 12));
        enchantMap.put(Item.ArmorAttributeType.DEXTERITY, new MinMax(150, 250));
        enchantMap.put(Item.ArmorAttributeType.VITALITY, new MinMax(150, 250));
        enchantMap.put(Item.ArmorAttributeType.INTELLECT, new MinMax(150, 250));
        enchantMap.put(Item.ArmorAttributeType.GEM_FIND, new MinMax(8, 20));
        enchantMap.put(Item.ArmorAttributeType.ITEM_FIND, new MinMax(5, 10));

        enchantMap.put(Item.WeaponAttributeType.ARMOR_PENETRATION, new MinMax(5, 15));
        enchantMap.put(Item.WeaponAttributeType.ACCURACY, new MinMax(10, 20));
        enchantMap.put(Item.WeaponAttributeType.BLIND, new MinMax(8, 12));
        enchantMap.put(Item.WeaponAttributeType.CRITICAL_HIT, new MinMax(8, 15));
        enchantMap.put(Item.WeaponAttributeType.LIFE_STEAL, new MinMax(6, 8));
        enchantMap.put(Item.WeaponAttributeType.DAMAGE, new MinMax(40, 80));
    }

    public RandomEnchantTrinketDataRare(Item.AttributeType type, int min, int max) {
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
