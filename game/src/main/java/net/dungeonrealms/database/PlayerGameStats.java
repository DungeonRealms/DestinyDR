package net.dungeonrealms.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.dungeonrealms.game.mastery.Utils;

import org.bukkit.entity.Player;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

public class PlayerGameStats implements LoadableData, SaveableData {

	@Getter @Setter private int characterID;
    private Map<StatColumn, Integer> statMap = new HashMap<>();

    public PlayerGameStats(int id){
        this.characterID = id;
    }

    public int getStat(StatColumn s) {
    	return statMap.containsKey(s) ? statMap.get(s) : 0;
    }

    @Override
    @SneakyThrows
    public void extractData(ResultSet resultSet) {
        for (StatColumn s : StatColumn.values())
        	setStat(s, resultSet.getInt("statistics." + s.getColumnName()));
    }

    public void addStat(StatColumn s) {
    	addStat(s, 1);
    }

    public void addStat(StatColumn s, int amt) {
    	setStat(s, getStat(s) + amt);
    }

    public void setStat(StatColumn s, int val) {
    	statMap.put(s, val);
    }

    public int getTotalMobKills(){
    	return getStat(StatColumn.T1_MOB_KILLS) + getStat(StatColumn.T2_MOB_KILLS) + getStat(StatColumn.T3_MOB_KILLS)
    			+ getStat(StatColumn.T4_MOB_KILLS) + getStat(StatColumn.T5_MOB_KILLS);
    }

    public String getUpdateStatement(){
    	String sql = "UPDATE statistics SET ";

    	for (StatColumn s : StatColumn.values())
    		sql += (s == StatColumn.values()[0] ? "" : ", ") + s.getColumnName() + " = '" + getStat(s) + "'";

    	return sql + " WHERE character_id = '" + characterID + "';";
    }

    @AllArgsConstructor
	public enum StatColumn {
        PLAYER_KILLS("player_kills", "pk"),
        T1_MOB_KILLS("monster_kills_t1", "t1"),
        T2_MOB_KILLS("monster_kills_t2", "t2"),
        T3_MOB_KILLS("monster_kills_t3", "t3"),
        T4_MOB_KILLS("monster_kills_t4", "t4"),
        T5_MOB_KILLS("monster_kills_t5", "t5"),
        DEATHS("deaths", "deaths"),
        TIME_PLAYED("time_played", "played"),
        LOOT_OPENED("loot_opened", "loot"),
        ORE_MINED("ore_mined", "mined"),
        FISH_CAUGHT("fish_caught", "fished"),

        LAWFUL_KILLS("lawful_kills"),
        UNLAWFUL_KILLS("unlawful_kills"),
        BOSS_KILLS_MAYEL("boss_kills_mayel"),
        BOSS_KILLS_BURICK("boss_kills_burick"),
        BOSS_KILLS_INFERNALABYSS("boss_kills_infernal_abyss"),
        DUELS_WON("duels_won"),
        DUELS_LOST("duels_lost"),
        ORBS_USED("orbs_used"),
        SUCCESSFUL_ENCHANTS("enchants_succeeded"),
        FAILED_ENCHANTS("enchants_failed"),
        ECASH_SPENT("ecash_spent"),
        GEMS_EARNED("gems_earned"),
        GEMS_SPENT("gems_spent");

        @Getter private String columnName;
        private String btlpVariableName;

        StatColumn(String sqlName) {
        	this(sqlName, null);
        }

        public Variable getVariable() {
        	if (btlpVariableName == null)
        		return null;

        	final StatColumn stat = this;
        	return new Variable(btlpVariableName) {
				@Override
				public String getReplacement(Player player) {
					PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
					if(wrapper == null) return null;
					if(stat.equals(TIME_PLAYED)) {
					    int seconds = wrapper.getPlayerGameStats().getStat(stat);
					    long hours = TimeUnit.SECONDS.toHours((long) seconds);
					    long minutes = seconds / 60 - hours * 60;
					    return hours + "h " + minutes + "m";
                    }
                    return Utils.format(wrapper.getPlayerGameStats().getStat(stat));
				}
        	};
        }
    }
}
