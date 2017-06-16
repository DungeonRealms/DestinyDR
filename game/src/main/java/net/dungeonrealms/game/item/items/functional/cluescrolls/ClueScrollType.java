package net.dungeonrealms.game.item.items.functional.cluescrolls;

import io.netty.util.internal.ThreadLocalRandom;
import lombok.Getter;
import net.dungeonrealms.game.item.items.functional.accessories.TrinketType;

import java.sql.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Rar349 on 6/13/2017.
 */
@Getter
public enum ClueScrollType {

    FISHING("Maybe the fisherman can translate this?","Take this to the Fisherman to claim your reward", new TrinketClueReward(TrinketType.LURE),Arrays.asList(Clue.PUFFER_FISH, Clue.UNDER_WATER_ORB, Clue.SPAWN_GUARDIAN_UNDER_WATER, Clue.FISH_GEAR, Clue.USE_FISHING_ENCHANT)),
    MINING("Maybe the miner can translate this?","Take this to the Miner to claim your reward",new TrinketClueReward(TrinketType.MINING_GLOVE),Arrays.asList(Clue.KILL_WITH_PICK, Clue.MINE_ORE, Clue.TREASURE_FIND, Clue.HIT_A_RABBIT, Clue.MINE_ON_HORSE, Clue.USE_PICK_ENCHANT)),
    COMBAT("Maybe Isaam the trainer can translate this?", "Take this to Isaam the trainer to claim your reward", new TrinketClueReward(TrinketType.COMBAT), Arrays.asList(Clue.KILL_MOBS, Clue.KILL_A_PLAYER, Clue.KILL_AN_ELITE, Clue.KILL_TIERED_ELITE, Clue.KILL_NUM_TIERED_ELITE, Clue.USE_ARMOR_ENCHANT, Clue.USE_WEAPON_ENCHANT));

    private List<Clue> clues;
    private AbstractClueReward rewards;
    private String tattered;
    private String whoForReward;
    ClueScrollType(String tattered,String whoForReward,AbstractClueReward rewards,List<Clue> clues) {
        this.tattered = tattered;
        this.whoForReward = whoForReward;
        this.clues = clues;
        this.rewards = rewards;
    }

    public Clue getRandomClue() {
        return clues.get(ThreadLocalRandom.current().nextInt(clues.size()));
    }
}
