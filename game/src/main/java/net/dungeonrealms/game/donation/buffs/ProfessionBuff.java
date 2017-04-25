package net.dungeonrealms.game.donation.buffs;

import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mastery.Utils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by Alan on 7/28/2016.
 */
public class ProfessionBuff extends Buff {

    // empty constructor is needed for generic instantiation during deserialization
    public ProfessionBuff() {
    }

    public ProfessionBuff(int duration, float bonusAmount, String activatingPlayer, String fromServer) {
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
                        + " has just activated " + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Profession Rates" + ChatColor.GOLD
                        + " for " + formattedTime + " by using 'Global Profession Buff' from the store!");
        Bukkit.getServer().broadcastMessage("");
        DonationEffects.getInstance().setActiveProfessionBuff(this);
        DonationEffects.getInstance().updateLootBuff("activeProfessionBuff", this.serialize());
    }

    @Override
    public void deactivateBuff() {
        final DonationEffects de = DonationEffects.getInstance();
        final ProfessionBuff nextBuff = de.getQueuedProfessionBuffs().poll();

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + ChatColor.GOLD + "The " + ChatColor.UNDERLINE
                + "+" + bonusAmount + "% Global Profession Rates" + ChatColor.GOLD + " from " + activatingPlayer + ChatColor.GOLD + " has expired.");

        if (nextBuff != null) {

            de.updateLootBuff("queuedProfessionBuffs", de.serializeQueuedBuffs(de.getQueuedProfessionBuffs()));
            nextBuff.activateBuff();
        } else {
            de.setActiveProfessionBuff(null);
            de.updateLootBuff("activeProfessionBuff", null);
        }
    }

}