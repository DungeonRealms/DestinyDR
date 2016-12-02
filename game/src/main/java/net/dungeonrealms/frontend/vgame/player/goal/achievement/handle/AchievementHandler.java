package net.dungeonrealms.frontend.vgame.player.goal.achievement.handle;

import lombok.Getter;
import net.dungeonrealms.common.awt.frame.handler.Handler;
import net.dungeonrealms.frontend.Game;
import net.dungeonrealms.frontend.vgame.player.goal.achievement.EnumAchievement;
import net.dungeonrealms.frontend.vgame.player.goal.achievement.type.CombatAchievement;
import net.dungeonrealms.frontend.vgame.player.goal.achievement.type.ExplorerAchievement;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AchievementHandler implements Handler {

    @Getter
    private boolean prepared;

    @Getter
    private UUID uniqueId;

    @Override
    public void prepare() {
        Game.getGame().getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Registering " + ChatColor.YELLOW
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
        this.uniqueId = UUID.randomUUID();
        this.prepared = true;
    }

    @Override
    public void disable() {
        this.uniqueId = null;
        this.prepared = false;
    }
}
