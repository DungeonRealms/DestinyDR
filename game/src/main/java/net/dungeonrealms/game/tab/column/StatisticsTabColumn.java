package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import com.google.common.collect.Sets;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.mastery.GamePlayer;
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
                            return Utils.format((Integer) DatabaseAPI.getInstance().getData(EnumData.GEMS, player.getUniqueId()));
                        }
                    },
                    new Variable("ecash") {
                        @Override
                        public String getReplacement(Player player) {
                            return Utils.format((Integer) DatabaseAPI.getInstance().getData(EnumData.ECASH, player.getUniqueId()));
                        }
                    },
                    new Variable("pk") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getPlayerKills());
                        }
                    },
                    new Variable("t1") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getT1MobsKilled());
                        }
                    },
                    new Variable("t2") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getT2MobsKilled());
                        }
                    },
                    new Variable("t3") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getT3MobsKilled());
                        }
                    },
                    new Variable("t4") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getT4MobsKilled());
                        }
                    },
                    new Variable("t5") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getT5MobsKilled());
                        }
                    },
                    new Variable("deaths") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getDeaths());
                        }
                    },
                    new Variable("played") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;

                            return convertMins((Integer) DatabaseAPI.getInstance().getData(EnumData.TIME_PLAYED, player.getUniqueId()));
                        }
                    }
                    ,
                    new Variable("loot") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getLootChestsOpened());
                        }
                    }
                    ,
                    new Variable("mined") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getOreMined());
                        }
                    }
                    ,
                    new Variable("fish") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            return Utils.format(gp.getPlayerStatistics().getFishCaught());
                        }
                    }
            ));
        } catch (NullPointerException ignored) {

        }
        return this;
    }

    private String convertMins(int mins) {
        return ChatColor.YELLOW.toString() + mins / 24 / 60 + ChatColor.BOLD + "d " + ChatColor.YELLOW + mins / 60 % 24 + ChatColor.BOLD + "h " + ChatColor.YELLOW + mins % 60 + ChatColor.BOLD + "m";
    }
}
