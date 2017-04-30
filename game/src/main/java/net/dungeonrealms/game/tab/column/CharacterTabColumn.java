package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

import com.google.common.collect.Sets;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.tab.Column;
import net.dungeonrealms.game.world.item.Item.ArmorAttributeType;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.dungeonrealms.game.world.item.Item.WeaponAttributeType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */
public class CharacterTabColumn extends Column {

    @Override
    public Column register() {
        try {
            variablesToRegister.addAll(Sets.newHashSet(
                    new Variable("plevel") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            if (gp.getLevel() == 0) return "?";
                            return String.valueOf(gp.getLevel());
                        }
                    },
                    new Variable("exp") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            if (gp.getExperience() == 0) return "?";

                            double exp = ((double) gp.getExperience()) / ((double) gp.getEXPNeeded(gp.getLevel()));
                            exp *= 100;

                            if (gp.getLevel() == 100) return ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "MAX";
                            return (int) exp + "%";
                        }
                    },
                    new Variable("energy") {
                        @Override
                        public String getReplacement(Player player) {
                            return getAttribute(player, ArmorAttributeType.ENERGY_REGEN);
                        }
                    },
                    new Variable("hps") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;

                            return String.valueOf((HealthHandler.getRegen(player) + gp.getStats().getHPRegen()));
                        }
                    },
                    new Variable("dps") {
                        @Override
                        public String getReplacement(Player player) {
                            return getAttribute(player, WeaponAttributeType.DAMAGE);
                        }
                    },
                    new Variable("alignment") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;

                            KarmaHandler.EnumPlayerAlignments playerAlignment = gp.getPlayerAlignment();
                            String pretty_align = (playerAlignment == KarmaHandler.EnumPlayerAlignments.LAWFUL ? ChatColor.DARK_GREEN.toString() :
                                    playerAlignment.getAlignmentColor()) + ChatColor.UNDERLINE.toString() + playerAlignment.name();

                            if (pretty_align.contains("CHAOTIC") || pretty_align.contains("NEUTRAL")) {
                                String time = String.valueOf(KarmaHandler.getInstance().getAlignmentTime(player));
                                pretty_align = pretty_align + playerAlignment.getAlignmentColor().toString() + " " + ChatColor.BOLD + time + "s..";
                            }
                            return pretty_align;
                        }
                    }

            ));
        } catch (NullPointerException ignored) {

        }
        return this;
    }
    
    private String getAttribute(Player player, AttributeType type) {
    	GamePlayer gp = GameAPI.getGamePlayer(player);
    	return gp != null ? gp.getAttributes().getAttribute(type).toString() : "X";
    }
}
