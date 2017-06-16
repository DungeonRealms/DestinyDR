package net.dungeonrealms.game.item.items.functional.cluescrolls;

import net.dungeonrealms.game.item.items.functional.accessories.Trinket;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 6/15/2017.
 */
public class TrinketClueReward implements AbstractClueReward {

    private List<Trinket> possibleRewards;
    private NavigableMap<Double, Trinket> table;
    private double total;
    public TrinketClueReward(TrinketType type) {
        possibleRewards = type.getAccessory();
    }

    public TrinketClueReward(Trinket... trinkets) {
        possibleRewards = Arrays.asList(trinkets);
    }

    @Override
    public void giveReward(Player player) {

    }

    public Trinket getRandomTrinket(ClueDifficulty difficulty) {
        if (table == null) {
            total = 0;
            table = new TreeMap<>();

            for (Trinket trinket : possibleRewards) {
                total += trinket.getChance();
                table.put(total, trinket);
            }
        }

        return table.ceilingEntry(ThreadLocalRandom.current().nextDouble() * total).getValue();
    }
}
