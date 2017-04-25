package net.dungeonrealms.game.donation.buffs;

import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mastery.Utils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by Alan on 7/28/2016.
 */
public class LevelBuff extends Buff {

    // empty constructor is needed for generic instantiation during deserialization
    public LevelBuff() {}

    public LevelBuff(int duration, float bonusAmount, String activatingPlayer, String fromServer) {
        this.duration = duration;
        this.bonusAmount = bonusAmount;
        this.activatingPlayer = activatingPlayer;
        this.fromServer = fromServer;
    }

    @Override
    public void onActivateBuff() {
        String formattedTime = DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
        Bukkit.getServer().broadcastMessage("");
        Bukkit.getServer().broadcastMessage(
                ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + "(" + Utils.getFormattedShardName(fromServer) + ") " + ChatColor.RESET + activatingPlayer + ChatColor.GOLD
                        + " has just activated " + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Character Level XP Rates" + ChatColor.GOLD
                        + " for " + formattedTime + " by using 'Global Level EXP Buff' from the store!");
        Bukkit.getServer().broadcastMessage("");
        DonationEffects.getInstance().setActiveLevelBuff(this);
        DonationEffects.getInstance().updateLootBuff("activeLevelBuff", this.serialize());
//        DatabaseAPI.getInstance().updateShardCollection(DungeonRealms.getInstance().bungeeName, EnumOperators.$SET,
//                "buffs.activeLevelBuff", this.serialize(), true);
    }

    @Override
    public void deactivateBuff() {
        final DonationEffects de = DonationEffects.getInstance();
        final LevelBuff nextBuff = de.getQueuedLevelBuffs().poll();

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + ChatColor.GOLD + activatingPlayer + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE
                + "+" + bonusAmount + "% Global Character Level XP Rates" + ChatColor.GOLD + " has expired.");

        if (nextBuff != null) {
            //So its just clearing the queue after it gets 1 or?

            de.updateLootBuff("queuedLevelBuffs", de.serializeQueuedBuffs(de.getQueuedLevelBuffs()));
            nextBuff.activateBuff();
        } else {
            de.setActiveLevelBuff(null);
            de.updateLootBuff("activeLevelBuff", null);
        }
    }
}