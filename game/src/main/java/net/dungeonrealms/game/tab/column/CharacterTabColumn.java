package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

import com.google.common.collect.Sets;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.handler.KarmaHandler.EnumPlayerAlignments;
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
                        	PlayerWrapper pw = PlayerWrapper.getWrapper(player);
                        	return pw != null && pw.getLevel() > 0 ? String.valueOf(pw.getLevel()) : "?";
                        }
                    },
                    new Variable("exp") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null || wrapper.getExperience() == 0)
                            	return "?";

                            double exp = (double) wrapper.getExperience() / (double) wrapper.getEXPNeeded();
                            exp *= 100;

                            return wrapper.getLevel() == 100 ? ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "MAX" : (int) exp + "%";
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
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null)
                            	return null;
                            return String.valueOf((HealthHandler.getRegen(player) + wrapper.getPlayerStats().getRegen()));
                        }
                    },
                    new Variable("dps") {
                        @Override
                        public String getReplacement(Player player) {
                            return getAttribute(player, ArmorAttributeType.DAMAGE);
                        }
                    },
                    new Variable("alignment") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;

                            KarmaHandler.EnumPlayerAlignments playerAlignment = wrapper.getAlignment();
                            String pretty_align = (playerAlignment == KarmaHandler.EnumPlayerAlignments.LAWFUL ? ChatColor.DARK_GREEN.toString() :
                                    playerAlignment.getAlignmentColor()) + ChatColor.UNDERLINE.toString() + playerAlignment.name();

                            if (playerAlignment != EnumPlayerAlignments.LAWFUL) {
                                String time = String.valueOf(wrapper.getAlignmentTime());
                                return pretty_align + playerAlignment.getAlignmentColor().toString() + " " + ChatColor.BOLD + time + "s..";
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
    	PlayerWrapper pw = PlayerWrapper.getWrapper(player);
    	return pw != null ? pw.getAttributes().getAttribute(type).toString() : "?";
    }
}
