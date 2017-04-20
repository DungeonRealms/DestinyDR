package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import com.google.common.collect.Sets;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.tab.Column;
import net.dungeonrealms.game.world.item.Item;
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
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null) return null;
                            int calculatedValue;

                            try {
                                calculatedValue = gp.getStaticAttributeVal(Item.ArmorAttributeType.ENERGY_REGEN);
                            } catch (NullPointerException ignored) {
                                return "";
                            }

                            return String.valueOf(calculatedValue);
                        }
                    },
                    new Variable("hps") {
                        @Override
                        public String getReplacement(Player player) {
                            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                            if (wrapper == null) return null;

                            return String.valueOf((HealthHandler.getInstance().getPlayerHPRegenLive(player) + wrapper.getPlayerStats().getHPRegen()));
                        }
                    },
                    new Variable("dps") {
                        @Override
                        public String getReplacement(Player player) {
                            GamePlayer gp = GameAPI.getGamePlayer(player);
                            if (gp == null || !gp.isAttributesLoaded()) return null;

                            return String.valueOf(gp.getAttributes().get("dps")[0] + " - " + gp.getAttributes().get("dps")[1]);
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
}
