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
    }

    public void updatePlayerStatistics() {
        if (playerKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.PLAYER_KILLS, playerKills, false);
            playerKills = 0;
        }
        if (lawfulKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.LAWFUL_KILLS, lawfulKills, false);
            lawfulKills = 0;
        }
        if (unlawfulKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.UNLAWFUL_KILLS, unlawfulKills, false);
            unlawfulKills = 0;
        }
        if (deaths > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.DEATHS, deaths, false);
            deaths = 0;
        }
        if (t1MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T1_MOB_KILLS, t1MobsKilled, false);
            t1MobsKilled = 0;
        }
        if (t2MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T2_MOB_KILLS, t2MobsKilled, false);
            t2MobsKilled = 0;
        }
        if (t3MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T3_MOB_KILLS, t3MobsKilled, false);
            t3MobsKilled = 0;
        }
        if (t4MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T4_MOB_KILLS, t4MobsKilled, false);
            t4MobsKilled = 0;
        }
        if (t5MobsKilled > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.T5_MOB_KILLS, t5MobsKilled, false);
            t5MobsKilled = 0;
        }
        if (mayelKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.BOSS_KILLS_MAYEL, mayelKills, false);
            mayelKills = 0;
        }
        if (burickKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.BOSS_KILLS_BURICK, burickKills, false);
            burickKills = 0;
        }
        if (infernalAbyssKills > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.BOSS_KILLS_INFERNALABYSS, infernalAbyssKills, false);
            infernalAbyssKills = 0;
        }
        if (lootChestsOpened > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.LOOT_OPENED, lootChestsOpened, false);
            lootChestsOpened = 0;
        }
        if (duelsWon > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.DUELS_WON, duelsWon, false);
            duelsWon = 0;
        }
        if (duelsLost > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.DUELS_LOST, duelsLost, false);
            duelsLost = 0;
        }
        if (oreMined > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.ORE_MINED, oreMined, false);
            oreMined = 0;
        }
        if (fishCaught > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.FISH_CAUGHT, fishCaught, false);
            fishCaught = 0;
        }
        if (orbsUsed > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.ORBS_USED, orbsUsed, false);
            orbsUsed = 0;
        }
        if (timePlayed > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.TIME_PLAYED, timePlayed, false);
            timePlayed = 0;
        }
        if (successfulEnchants > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.SUCCESSFUL_ENCHANTS, successfulEnchants, false);
            successfulEnchants = 0;
        }
        if (failedEnchants > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.FAILED_ENCHANTS, failedEnchants, false);
            failedEnchants = 0;
        }
        if (ecashSpent > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.ECASH_SPENT, ecashSpent, false);
            ecashSpent = 0;
        }
        if (gemsEarned > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.GEMS_EARNED, gemsEarned, false);
            gemsEarned = 0;
        }
        if (gemsSpent > 0) {
            DatabaseAPI.getInstance().update(playerUUID, EnumOperators.$INC, EnumData.GEMS_SPENT, gemsSpent, false);
            gemsSpent = 0;
        }
    }
}
