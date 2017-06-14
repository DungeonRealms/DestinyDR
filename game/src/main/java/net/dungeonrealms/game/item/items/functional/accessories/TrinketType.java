package net.dungeonrealms.game.item.items.functional.accessories;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public enum TrinketType {
    LURE("Lure", Material.END_ROD,
            ChatColor.GREEN,
            Lists.newArrayList(Trinket.FISH_DOUBLE_FISH, Trinket.FISH_DURABILITY, Trinket.FISH_TRIPLE_FISH, Trinket.FISH_TREASURE_FIND, Trinket.FISH_SCALER)),
    MINING_GLOVE("Mining Glove", Material.COAL, ChatColor.GREEN,
            Lists.newArrayList(Trinket.MINE_TRIPLE_ORE, Trinket.MIN_DOUBLE_ORE, Trinket.MINE_DURABILITY, Trinket.MINE_GEM_FIND, Trinket.MINE_TREASURE_FIND)),
    RIFT_RING("Rift Ring", Material.GOLD_NUGGET, ChatColor.LIGHT_PURPLE, Lists.newArrayList(Trinket.INCREASED_RIFT, Trinket.RIFT_LAVA_TRAIL, Trinket.RIFT_DAMAGE_INCREASE, Trinket.UPCOMING_RIFT)),
    COMBAT("Jewel", Material.RABBIT_FOOT, ChatColor.RED, Lists.newLinkedList());

    @Getter
    private Material material;
    @Getter
    private String name;
    @Getter
    private ChatColor nameColor;
    @Getter
    private List<Trinket> accessory;

    NavigableMap<Double, Trinket> table;
    double total;

    TrinketType(String name, Material material, ChatColor color, List<Trinket> list) {
        this.material = material;
        this.nameColor = color;
        this.name = name;
        this.accessory = list;
    }

    public Trinket getRandomTrinket() {
        if (table == null) {
            total = 0;
            table = new TreeMap<>();

            for (Trinket trinket : accessory) {
                total += trinket.getChance();
                table.put(total, trinket);
            }
        }

        return table.ceilingEntry(ThreadLocalRandom.current().nextDouble() * total).getValue();
    }

    public static TrinketType getFromName(String name) {
        return Arrays.stream(values()).filter(e -> e.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
