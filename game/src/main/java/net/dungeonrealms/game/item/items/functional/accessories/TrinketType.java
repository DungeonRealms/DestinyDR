package net.dungeonrealms.game.item.items.functional.accessories;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public enum TrinketType {
    LURE("Lure", new MaterialData(Material.SHEARS, (byte) 1),
            ChatColor.GREEN,
            Lists.newArrayList(
                    Trinket.FISH_DOUBLE_FISH,
                    Trinket.FISH_DURABILITY,
                    Trinket.FISH_TRIPLE_FISH,
                    Trinket.FISH_CATCH_SUCCESS,
                    Trinket.FISH_JUNK_FIND,
                    Trinket.FISH_TREASURE_FIND,
                    Trinket.FISH_SCALER,
                    Trinket.FISH_DAY_SUCCESS,
                    Trinket.FISH_NIGHT_SUCCESS)),
    MINING_GLOVE("Mining Glove",
            new MaterialData(Material.COAL, (byte) 5), ChatColor.GREEN,
            Lists.newArrayList(
                    Trinket.MINE_TRIPLE_ORE,
                    Trinket.MIN_DOUBLE_ORE,
                    Trinket.MINE_DURABILITY,
                    Trinket.MINE_GEM_FIND,
                    Trinket.MINE_TREASURE_FIND,
                    Trinket.MINE_GEM_TELEPORT,
                    Trinket.NO_MINING_FATIGUE)),
    RIFT_RING("Rift Ring",
            new MaterialData(Material.SHEARS, (byte) 3),
            ChatColor.LIGHT_PURPLE, Lists.newArrayList(Trinket.INCREASED_RIFT,
            Trinket.RIFT_LAVA_TRAIL,
            Trinket.RIFT_DAMAGE_INCREASE,
            Trinket.UPCOMING_RIFT,
            Trinket.DUNGEON_TELEPORT,
            Trinket.REDUCED_BOOK_COOLDOWN)),
    COMBAT("Circlet", new MaterialData(Material.SHEARS, (byte) 4),
            ChatColor.YELLOW, Lists.newArrayList(Trinket.COMBAT));

    @Getter
    private MaterialData material;
    @Getter
    private String name;
    @Getter
    private ChatColor nameColor;
    @Getter
    private List<Trinket> accessory;

    NavigableMap<Double, Trinket> table;
    double total;

    TrinketType(String name, MaterialData material, ChatColor color, List<Trinket> list) {
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
        return Arrays.stream(values()).filter(e -> e.name().equals(name) || e.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
