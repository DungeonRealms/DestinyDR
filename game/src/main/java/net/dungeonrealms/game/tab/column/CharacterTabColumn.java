package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import com.google.common.collect.Sets;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.tab.type.Column;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/4/2016
 */
public class CharacterTabColumn extends Column {

    @Override
    public Column register() {
        variablesToRegister.addAll(Sets.newHashSet(
                new Variable("level") {
                    @Override
                    public String getReplacement(Player player) {
                        GamePlayer gp = GameAPI.getGamePlayer(player);
                        if (gp == null) return null;

                        return String.valueOf(gp.getLevel());
                    }
                },
                new Variable("exp") {
                    @Override
                    public String getReplacement(Player player) {
                        GamePlayer gp = GameAPI.getGamePlayer(player);
                        if (gp == null) return null;

                        return gp.getExperience() + "/"
                                + gp.getEXPNeeded(gp.getLevel());
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
                            pretty_align = pretty_align + "\n" + playerAlignment.getAlignmentColor().toString() + ChatColor.BOLD + time + "s..";
                        }
                        return pretty_align;
                    }
                }

        ));
        return this;
    }
}
