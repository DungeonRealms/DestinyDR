package net.dungeonrealms.game.player.statistics;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;

import java.util.UUID;

/**
 * Created by Kieran Quigley (Proxying) on 26-Jun-16.
 */
public class PlayerStatistics {

    @Getter
    @Setter
    private int playerKills;
    @Getter
    @Setter
    private int lawfulKills;
    @Getter
    @Setter
    private int unlawfulKills;
    @Getter
    @Setter
    private int deaths;
    @Getter
    @Setter
    private int t1MobsKilled;
    @Getter
    @Setter
    private int t2MobsKilled;
    @Getter
    @Setter
    private int t3MobsKilled;
    @Getter
    @Setter
    private int t4MobsKilled;
    @Getter
    @Setter
    private int t5MobsKilled;
    @Getter
    @Setter
    private int mayelKills;
    @Getter
    @Setter
    private int burickKills;
    @Getter
    @Setter
    private int infernalAbyssKills;
    @Getter
    @Setter
    private int lootChestsOpened;
    @Getter
    @Setter
    private int duelsWon;
    @Getter
    @Setter
    private int duelsLost;
    @Getter
    @Setter
    private int oreMined;
    @Getter
    @Setter
    private int fishCaught;
    @Getter
    @Setter
    private int orbsUsed;
    @Getter
    @Setter
    private int timePlayed;
    @Getter
    @Setter
    private int successfulEnchants;
    @Getter
    @Setter
    private int failedEnchants;
    @Getter
    @Setter
    private int ecashSpent;
    @Getter
    @Setter
    private int gemsEarned;
    @Getter
    @Setter
    private int gemsSpent;
    @Getter
    private UUID playerUUID;

    public PlayerStatistics(UUID uuid) {
        this.playerUUID = uuid;
        this.playerKills = (int) DatabaseAPI.getInstance().getData(EnumData.PLAYER_KILLS, uuid);
        this.lawfulKills = (int) DatabaseAPI.getInstance().getData(EnumData.LAWFUL_KILLS, uuid);
        this.unlawfulKills = (int) DatabaseAPI.getInstance().getData(EnumData.UNLAWFUL_KILLS, uuid);
        this.deaths = (int) DatabaseAPI.getInstance().getData(EnumData.DEATHS, uuid);
        this.t1MobsKilled = (int) DatabaseAPI.getInstance().getData(EnumData.T1_MOB_KILLS, uuid);
        this.t2MobsKilled = (int) DatabaseAPI.getInstance().getData(EnumData.T2_MOB_KILLS, uuid);
        this.t3MobsKilled = (int) DatabaseAPI.getInstance().getData(EnumData.T3_MOB_KILLS, uuid);
        this.t4MobsKilled = (int) DatabaseAPI.getInstance().getData(EnumData.T4_MOB_KILLS, uuid);
        this.t5MobsKilled = (int) DatabaseAPI.getInstance().getData(EnumData.T5_MOB_KILLS, uuid);
        this.mayelKills = (int) DatabaseAPI.getInstance().getData(EnumData.BOSS_KILLS_MAYEL, uuid);
        this.burickKills = (int) DatabaseAPI.getInstance().getData(EnumData.BOSS_KILLS_BURICK, uuid);
        this.infernalAbyssKills = (int) DatabaseAPI.getInstance().getData(EnumData.BOSS_KILLS_INFERNALABYSS, uuid);
        this.lootChestsOpened = (int) DatabaseAPI.getInstance().getData(EnumData.LOOT_OPENED, uuid);
        this.duelsWon = (int) DatabaseAPI.getInstance().getData(EnumData.DUELS_WON, uuid);
        this.duelsLost = (int) DatabaseAPI.getInstance().getData(EnumData.DUELS_LOST, uuid);
        this.oreMined = (int) DatabaseAPI.getInstance().getData(EnumData.ORE_MINED, uuid);
        this.fishCaught = (int) DatabaseAPI.getInstance().getData(EnumData.FISH_CAUGHT, uuid);
        this.orbsUsed = (int) DatabaseAPI.getInstance().getData(EnumData.ORBS_USED, uuid);
        this.successfulEnchants = (int) DatabaseAPI.getInstance().getData(EnumData.SUCCESSFUL_ENCHANTS, uuid);
        this.failedEnchants = (int) DatabaseAPI.getInstance().getData(EnumData.FAILED_ENCHANTS, uuid);
        this.ecashSpent = (int) DatabaseAPI.getInstance().getData(EnumData.ECASH_SPENT, uuid);
        this.gemsEarned = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS_EARNED, uuid);
        this.gemsSpent = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS_SPENT, uuid);
    }

    public void updatePlayerStatistics() {
        if (playerKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.PLAYER_KILLS, playerKills, false);
        }
        if (lawfulKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.LAWFUL_KILLS, lawfulKills, false);
        }
        if (unlawfulKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.UNLAWFUL_KILLS, unlawfulKills, false);
        }
        if (deaths > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.DEATHS, deaths, false);
        }
        if (t1MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T1_MOB_KILLS, t1MobsKilled, false);
        }
        if (t2MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T2_MOB_KILLS, t2MobsKilled, false);
        }
        if (t3MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T3_MOB_KILLS, t3MobsKilled, false);
        }
        if (t4MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T4_MOB_KILLS, t4MobsKilled, false);
        }
        if (t5MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T5_MOB_KILLS, t5MobsKilled, false);
        }
        if (mayelKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.BOSS_KILLS_MAYEL, mayelKills, false);
        }
        if (burickKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.BOSS_KILLS_BURICK, burickKills, false);
        }
        if (infernalAbyssKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.BOSS_KILLS_INFERNALABYSS, infernalAbyssKills, false);
        }
        if (lootChestsOpened > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.LOOT_OPENED, lootChestsOpened, false);
        }
        if (duelsWon > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.DUELS_WON, duelsWon, false);
        }
        if (duelsLost > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.DUELS_LOST, duelsLost, false);
        }
        if (oreMined > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.ORE_MINED, oreMined, false);
        }
        if (fishCaught > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.FISH_CAUGHT, fishCaught, false);
        }
        if (orbsUsed > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.ORBS_USED, orbsUsed, false);
        }
        //Leave this seperate I think.?
        if (timePlayed > 0) {
            //stored in seconds, lets convert it to minutes for easier mongo storage.
            int timeInMins = Math.round(timePlayed / 60);
            if (timeInMins > 0) {
                DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.TIME_PLAYED, timeInMins, false);
            }
            timePlayed = 0;
        }
        if (successfulEnchants > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.SUCCESSFUL_ENCHANTS, successfulEnchants, false);
        }
        if (failedEnchants > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.FAILED_ENCHANTS, failedEnchants, false);
        }
        if (ecashSpent > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.ECASH_SPENT, ecashSpent, false);
        }
        if (gemsEarned > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.GEMS_EARNED, gemsEarned, false);
        }
        if (gemsSpent > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.GEMS_SPENT, gemsSpent, false);
        }
    }

    public int getTotalMobKills() {
        return t1MobsKilled + t2MobsKilled + t3MobsKilled + t4MobsKilled + t5MobsKilled;
    }
}
