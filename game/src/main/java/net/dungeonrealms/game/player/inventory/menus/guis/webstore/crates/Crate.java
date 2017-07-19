package net.dungeonrealms.game.player.inventory.menus.guis.webstore.crates;

import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.player.cosmetics.particles.impl.CrateOpeningEffect;
import net.dungeonrealms.game.player.json.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Rar349 on 7/6/2017.
 */
public abstract class Crate {

    protected static int UNCOMMON_REWARD = 0, RARE_REWARD = 1, VERY_RARE_REWARD = 2, INSANE_REWARD = 3;

    private final Player openingPlayer;
    private final Location location;
    private AbstractCrateReward reward;
    protected int rewardTier = -1;
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
        ChatColor color = null;
        String rewardString = "NaN";
        if(rewardTier == INSANE_REWARD) {
            rewardString = "INSANE";
            color = ChatColor.DARK_RED;
        }
        else if(rewardTier == VERY_RARE_REWARD){
            rewardString = "VERY RARE";
            color = ChatColor.RED;
        }
        else if(rewardTier == RARE_REWARD) {
            rewardString = "RARE";
            color = ChatColor.GOLD;
        }
        else if(rewardTier == UNCOMMON_REWARD) {
            rewardString = "UNCOMMON";
            color = ChatColor.YELLOW;
        }

            if(color != null) {
                JSONMessage normal = new JSONMessage(color + openingPlayer.getName() + ChatColor.RESET + ChatColor.GRAY + " opened a Mystery Vote Chest and received a(n) " + color + ChatColor.BOLD.toString() + rewardString + ChatColor.GRAY + " reward! ", ChatColor.WHITE);
                List<String> hoveredChat = new ArrayList<>();
                hoveredChat.add(color + reward.getDisplayName());
                for(String s : reward.getDisplayLore()) hoveredChat.add(ChatColor.GRAY + s);
                normal.addHoverText(hoveredChat, "SHOW", ChatColor.WHITE, true);
                if(rewardTier >= RARE_REWARD)GameAPI.sendNetworkMessage("BroadcastRaw", normal.toString());
                else Bukkit.getOnlinePlayers().forEach(normal::sendToPlayer);
            }
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
