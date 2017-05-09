package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

import com.google.common.collect.Sets;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.tab.Column;

import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */
public class StatisticsTabColumn extends Column {


    @Override
    public Column register() {
        try {
            variablesToRegister.addAll(Sets.newHashSet(
                    new Variable("gems") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getGems());
                        }
                    },
                    new Variable("ecash") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getEcash());
                        }
                    }));
            
            // Add stats.
            for (StatColumn stat : StatColumn.values()) {
            	Variable v = stat.getVariable();
            	if (v != null)
            		variablesToRegister.add(v);
            }
        } catch (NullPointerException ignored) {

        }
        return this;
    }
}
