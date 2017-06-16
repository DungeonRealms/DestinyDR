package net.dungeonrealms.game.item.items.functional.cluescrolls;

import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 6/15/2017.
 */
public interface AbstractClueReward {

    void giveReward(Player player, ClueDifficulty difficulty, String npcName);
}
