package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.impl;

import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.AbstractCrateReward;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.Crate;
import net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates.Crates;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 7/6/2017.
 */
public class VoteCrate extends Crate {

    public VoteCrate(Player openingPlayer, Location location) {
        super(openingPlayer, location);
    }

    @Override
    public AbstractCrateReward getRandomReward() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rand = random.nextInt(10_000);
        //rand = random.nextInt(64);
        //if(getOpeningPlayer().getName().equalsIgnoreCase("ingot")) return getRareReward();
        //1/50k
        if(rand == 3) return getInsaneReward();
        //1/25k
        if(rand <= 2) return getVeryRareReward();
        //1/5k
        if(rand <= 13) return getRareReward();
        //1/1k
        if(rand <= 100) return getUncommonReward();



        return getCommonReward();
    }


    @Override
    public Crates getType() {
        return Crates.VOTE_CRATE;
    }
}
