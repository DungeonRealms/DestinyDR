package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import lombok.Setter;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.player.cosmetics.particles.impl.CrateOpeningEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 7/6/2017.
 */
public abstract class Crate {

    private final Player openingPlayer;
    private final Location location;
    private AbstractCrateReward reward;
    @Setter
    private Runnable onComplete;

    public Crate(Player openingPlayer, Location location) {
        this.openingPlayer = openingPlayer;
        this.location = location;
        do {
            reward = getRandomReward();
        } while(!reward.canReceiveReward(openingPlayer));
    }

    public abstract AbstractCrateReward getRandomReward();

    public Player getOpeningPlayer() {
        return openingPlayer;
    }

    public Location getLocation() {
        return location;
    }

    public AbstractCrateReward getReward() {
        return this.reward;
    }

    public void open() {
        new CrateOpeningEffect(location, openingPlayer,reward.getDisplayMaterial(), () -> {
            if(openingPlayer == null || !openingPlayer.isOnline()) return; //They disconnected before it stopped.
            giveReward();
            if(onComplete != null) onComplete.run();
        });
    }

    //public abstract void giveReward();

    public void giveReward() {
        reward.giveReward(openingPlayer);
        openingPlayer.sendMessage(ChatColor.GRAY + "You have received a " + reward.getDisplayName() + ChatColor.GRAY + " from your crate!");
    }

    public abstract Crates getType();

    public AbstractCrateReward getInsaneReward() {
        AbstractCrateReward[] possibilities = getType().getInsaneRewards();
        return possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)];
    }

    public AbstractCrateReward getVeryRareReward() {
        AbstractCrateReward[] possibilities = getType().getVeryRareRewards();
        return possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)];
    }

    public AbstractCrateReward getRareReward() {
        AbstractCrateReward[] possibilities = getType().getRareRewards();
        return possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)];
    }

    public AbstractCrateReward getUncommonReward() {
        AbstractCrateReward[] possibilities = getType().getUncommonRewards();
        return possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)];
    }

    public AbstractCrateReward getCommonReward() {
        AbstractCrateReward[] possibilities = getType().getCommonRewards();
        return possibilities[ThreadLocalRandom.current().nextInt(possibilities.length)];
    }

}
