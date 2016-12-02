package net.dungeonrealms.old.game.donation.buffs;

import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.data.EnumOperators;
import net.dungeonrealms.old.game.donation.DonationEffects;
import net.dungeonrealms.old.game.mastery.Utils;
import net.dungeonrealms.vgame.old.Game;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Created by Alan on 7/28/2016.
 */
public class LootBuff extends Buff {

    // empty constructor is needed for generic instantiation during deserialization
    public LootBuff() {
    }

    public LootBuff(int duration, float bonusAmount, String activatingPlayer, String fromServer) {
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
                        + " has just activated " + ChatColor.UNDERLINE + "+" + bonusAmount + "% Global Drop Rates" + ChatColor.GOLD
                        + " for " + formattedTime + " by using 'Global Loot Buff' from the E-CASH store!");
        Bukkit.getServer().broadcastMessage("");
        DonationEffects.getInstance().setActiveLootBuff(this);
        DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$SET,
                "buffs.activeLootBuff", this.serialize(), true);
    }

    @Override
    public void deactivateBuff() {
        final DonationEffects de = DonationEffects.getInstance();
        final LootBuff nextBuff = de.getQueuedLootBuffs().poll();

        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + ChatColor.GOLD + "The " + ChatColor.UNDERLINE
                + "+" + bonusAmount + "% Global Drop Rates" + ChatColor.GOLD + " from " + activatingPlayer + ChatColor.GOLD + " has expired.");

        if (nextBuff != null) {
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$POP,
                    "buffs.queuedLootBuffs", -1, true);
            nextBuff.activateBuff();
        } else {
            de.getInstance().setActiveLootBuff(null);
            DatabaseAPI.getInstance().updateShardCollection(Game.getGame().getGameShard().getBungeeIdentifier(), EnumOperators.$UNSET,
                    "buffs.activeLootBuff", "", true);
        }
    }
}