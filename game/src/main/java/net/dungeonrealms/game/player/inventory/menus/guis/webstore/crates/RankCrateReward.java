package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import net.dungeonrealms.common.game.database.player.PlayerRank;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * Created by Rar349 on 7/10/2017.
 */
public class RankCrateReward extends AbstractCrateReward {

    private PlayerRank rank;
    private int days;

    public RankCrateReward(PlayerRank rank, int days) {
        super(Material.EMERALD, rank.getFullPrefix(), days + " days of " + rank.getFullPrefix());
        this.rank = rank;
        this.days = days;
    }

    @Override
    public void giveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        wrapper.setRankExpiration((int)((System.currentTimeMillis() / 1000) + TimeUnit.DAYS.toSeconds(days)));
        wrapper.setRank(rank);
    }

    @Override
    public boolean canReceiveReward(Player player) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        return !wrapper.getRank().isAtLeast(rank);
    }
}
