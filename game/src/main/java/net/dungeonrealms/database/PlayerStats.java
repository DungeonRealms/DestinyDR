package net.dungeonrealms.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.ResultSet;

public class PlayerStats extends LoadableData {

    @Getter
    private int characterID;

    @Getter
    @Setter
    private int playerKills, lawfulKills, unlawfulKills, deaths;

    @Getter
    @Setter
    //Mob kils
    private int t1MonsterKills, t2MonsterKills, t3MonsterKills, t4MonsterKills, t5MonsterKills;

    @Getter
    @Setter
    private int bossMayelKills, bossBurickKills, bossInfernalAbyss;

    @Getter
    @Setter
    private int lootOpened, duelsWon, duelsLost, oreMined, fishCaught, orbsUsed, timePlayed, successfulEnchants, failedEnchants, ecashSpent, gemsEarned, gemsSpent;

    public PlayerStats(int id){
        this.characterID = id;
    }

    @Override
    @SneakyThrows
    public void extractData(ResultSet resultSet) {
        this.playerKills = resultSet.getInt(StatColumn.PLAYER_KILLS.get());
        this.lawfulKills = resultSet.getInt(StatColumn.LAWFUL_KILLS.get());
        this.unlawfulKills = resultSet.getInt(StatColumn.UNLAWFUL_KILLS.get());
        this.deaths = resultSet.getInt(StatColumn.DEATHS.get());
        this.t1MonsterKills = resultSet.getInt(StatColumn.T1_MOB_KILLS.get());
        this.t2MonsterKills = resultSet.getInt(StatColumn.T2_MOB_KILLS.get());
        this.t3MonsterKills = resultSet.getInt(StatColumn.T3_MOB_KILLS.get());
        this.t4MonsterKills = resultSet.getInt(StatColumn.T4_MOB_KILLS.get());
        this.t5MonsterKills = resultSet.getInt(StatColumn.T5_MOB_KILLS.get());

        this.bossMayelKills = getInt(resultSet, StatColumn.BOSS_KILLS_MAYEL);
        this.bossBurickKills = getInt(resultSet, StatColumn.BOSS_KILLS_BURICK);
        this.bossInfernalAbyss = getInt(resultSet, StatColumn.BOSS_KILLS_INFERNALABYSS);

        this.lootOpened = getInt(resultSet, StatColumn.LOOT_OPENED);
        this.duelsWon = getInt(resultSet, StatColumn.DUELS_WON);
        this.duelsLost = getInt(resultSet, StatColumn.DUELS_LOST);
        this.oreMined = getInt(resultSet, StatColumn.ORE_MINED);
        this.fishCaught = getInt(resultSet, StatColumn.FISH_CAUGHT);
        this.timePlayed = getInt(resultSet, StatColumn.TIME_PLAYED);
        this.successfulEnchants = getInt(resultSet, StatColumn.SUCCESSFUL_ENCHANTS);
        this.failedEnchants = getInt(resultSet, StatColumn.FAILED_ENCHANTS);
        this.ecashSpent = getInt(resultSet, StatColumn.ECASH_SPENT);
        this.gemsEarned = getInt(resultSet, StatColumn.GEMS_EARNED);
        this.gemsSpent = getInt(resultSet, StatColumn.GEMS_SPENT);
    }

    public String getUpdateStatement(){
        return String.format("UPDATE statistics SET players_kills = %s, lawful_kills = %s, unlawful_kills = %s, deaths = %s, " +
                "monster_kills_t1 = %s, monster_kills_t2 = %s, monster_kills_t3 = %s, monster_kills_t4 = %s, monster_kills_t5 = %s," +
                "boss_kills_mayel = %s, boss_kills_burick = %s, boss_kills_infernalAbyss = %s, loot_opened = %s, duels_won = %s, duels_lost = %s, ore_mined = %s," +
                "fish_caught = %s, orbs_used = %s, time_played = %s, successful_enchants = %s, failed_enchants = %s, ecash_spent = %s, gems_earned = %s, gems_spent = %s " +
                "WHERE character_id = '%s';",
                getPlayerKills(), getLawfulKills(), getUnlawfulKills(), getDeaths(),
                getT1MonsterKills(), getT2MonsterKills(), getT3MonsterKills(), getT4MonsterKills(), getT5MonsterKills(),
                getBossMayelKills(), getBossBurickKills(), getBossInfernalAbyss(), getLootOpened(), getDuelsWon(), getDuelsLost(), getOreMined(),
                getFishCaught(), getOrbsUsed(), getTimePlayed(), getSuccessfulEnchants(), getFailedEnchants(), getEcashSpent(), getGemsEarned(), getGemsSpent(),
                getCharacterID());
    }

    @SneakyThrows
    private int getInt(ResultSet rs, StatColumn column) {
        return rs.getInt(column.get());
    }

    @AllArgsConstructor
    enum StatColumn {
        PLAYER_KILLS("player_kills"),
        LAWFUL_KILLS("lawful_kills"),
        UNLAWFUL_KILLS("unlawful_kills"),
        DEATHS("deaths"),
        T1_MOB_KILLS("monster_kills_t1"),
        T2_MOB_KILLS("monster_kills_t2"),
        T3_MOB_KILLS("monster_kills_t3"),
        T4_MOB_KILLS("monster_kills_t4"),
        T5_MOB_KILLS("monster_kills_t5"),
        BOSS_KILLS_MAYEL("boss_kills_mayel"),
        BOSS_KILLS_BURICK("boss_kills_burick"),
        BOSS_KILLS_INFERNALABYSS("boss_kills_infernalAbyss"),
        LOOT_OPENED("loot_opened"),
        DUELS_WON("duels_won"),
        DUELS_LOST("duels_lost"),
        ORE_MINED("ore_mined"),
        FISH_CAUGHT("fish_caught"),
        ORBS_USED("orbs_used"),
        TIME_PLAYED("time_played"),
        SUCCESSFUL_ENCHANTS("successful_enchants"),
        FAILED_ENCHANTS("failed_enchants"),
        ECASH_SPENT("ecash_spent"),
        GEMS_EARNED("gems_earned"),
        GEMS_SPENT("gems_spent");

        @Getter
        private String columnName;

        public String get() {
            return this.columnName;
        }
    }
}
