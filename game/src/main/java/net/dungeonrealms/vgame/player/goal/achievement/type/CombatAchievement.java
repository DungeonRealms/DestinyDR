package net.dungeonrealms.vgame.player.goal.achievement.type;

import lombok.Getter;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.goal.achievement.Achievement;
import net.dungeonrealms.vgame.player.goal.achievement.IAchievement;
import net.dungeonrealms.vgame.player.goal.objective.combat.CombatObjective;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CombatAchievement extends Achievement implements IAchievement
{
    @Getter
    private CombatObjective objective;

    public CombatAchievement(String name, String[] description, int expReward, String collectionName, CombatObjective combatObjective)
    {
        super(name, expReward, description, collectionName);
        this.objective = combatObjective;
    }

    @Override
    public void register()
    {
        Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () ->
        {
            switch (this.objective.getCombatGoal())
            {
                case BOSS:
                    // Dungeon boss?
                    if (this.objective.getDungeonBoss() != null)
                    {
                        Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().values()
                                .stream().filter(gamePlayer -> !gamePlayer.getData().getCollectionData().getAchievements()
                                .contains(this.getCollectionName()))
                                .filter(gamePlayer -> gamePlayer.getData().getGameData().getKilledBosses().contains(this.objective.getDungeonBoss().name())).forEach(this::reward);
                    }
                    // World boss?
                    if (this.objective.getBossType() != null)
                    {
                        Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().values()
                                .stream().filter(gamePlayer -> !gamePlayer.getData().getCollectionData().getAchievements()
                                .contains(this.getCollectionName()))
                                .filter(gamePlayer -> gamePlayer.getData().getGameData().getKilledBosses().contains(this.objective.getBossType().name())).forEach(this::reward);
                    }
                    break;
                case PLAYER:
                    Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().values()
                            .stream().filter(gamePlayer -> !gamePlayer.getData().getCollectionData().getAchievements()
                            .contains(this.getCollectionName()))
                            .filter(gamePlayer -> gamePlayer.getData().getGameData().getPlayerKills() >= this.objective.getObjective()).forEach(this::reward);
                    break;
                case MONSTER:
                    Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().values()
                            .stream().filter(gamePlayer -> !gamePlayer.getData().getCollectionData().getAchievements()
                            .contains(this.getCollectionName()))
                            .filter(gamePlayer -> gamePlayer.getData().getGameData().getMonsterKills() >= this.objective.getObjective()).forEach(this::reward);
                    break;
            }
        }, 0L, 20);
    }
}
