package net.dungeonrealms.vgame.player.goal.achievement.handle;

import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.goal.achievement.EnumAchievement;
import net.dungeonrealms.vgame.player.goal.achievement.type.CombatAchievement;
import net.dungeonrealms.vgame.player.goal.achievement.type.ExplorerAchievement;
import org.bukkit.ChatColor;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AchievementHandler implements SuperHandler.Handler {
    @Override
    public void prepare() {
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Registering " + ChatColor.YELLOW
                + EnumAchievement.values().length + ChatColor.GREEN + " achievements");
        for (EnumAchievement enumAchievement : EnumAchievement.values()) {
            if (enumAchievement.getAchievement() instanceof ExplorerAchievement) {
                ExplorerAchievement explorerAchievement = (ExplorerAchievement) enumAchievement.getAchievement();
                explorerAchievement.register();
            }
            if (enumAchievement.getAchievement() instanceof CombatAchievement) {
                CombatAchievement combatAchievement = (CombatAchievement) enumAchievement.getAchievement();
                combatAchievement.register();
            }
        }
    }
}
