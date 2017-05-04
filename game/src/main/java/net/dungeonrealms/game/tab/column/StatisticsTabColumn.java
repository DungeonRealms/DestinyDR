package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import com.google.common.collect.Sets;
import net.dungeonrealms.common.util.TimeUtil;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.ChatColor;
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
                    },
                    new Variable("pk") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getPlayerKills());
                        }
                    },
                    new Variable("t1") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getT1MonsterKills());
                        }
                    },
                    new Variable("t2") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getT2MonsterKills());
                        }
                    },
                    new Variable("t3") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getT3MonsterKills());
                        }
                    },
                    new Variable("t4") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getT4MonsterKills());
                        }
                    },
                    new Variable("t5") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getT5MonsterKills());
                        }
                    },
                    new Variable("deaths") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getDeaths());
                        }
                    },
                    new Variable("played") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return convertMins(wrapper.getPlayerGameStats().getTimePlayed());
                        }
                    }
                    ,
                    new Variable("loot") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getLootOpened());
                        }
                    }
                    ,
                    new Variable("mined") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getOreMined());
                        }
                    }
                    ,
                    new Variable("fish") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;
                            return Utils.format(wrapper.getPlayerGameStats().getFishCaught());
                        }
                    }
            ));
        } catch (NullPointerException ignored) {

        }
        return this;
    }

    private String convertMins(int mins) {
//        TimeUtil.formatDifference(mins * 60);
        return TimeUtil.formatDifference(mins, ChatColor.YELLOW, ChatColor.BOLD);
//        return ChatColor.YELLOW.toString() + mins / 24 / 60 + ChatColor.BOLD + "d " + ChatColor.YELLOW + mins / 60 % 24 + ChatColor.BOLD + "h " + ChatColor.YELLOW + mins % 60 + ChatColor.BOLD + "m";
    }
}
