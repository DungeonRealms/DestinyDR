package net.dungeonrealms.vgame.player.goal.objective.combat;

import lombok.Getter;
import net.dungeonrealms.vgame.player.goal.objective.EnumObjectiveType;
import net.dungeonrealms.vgame.player.goal.objective.Objective;
import net.dungeonrealms.vgame.world.entity.boss.EnumBossType;
import net.dungeonrealms.vgame.world.entity.boss.EnumDungeonBoss;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CombatObjective extends Objective {
    @Getter
    private EnumCombatGoal combatGoal;

    @Getter
    private EnumBossType bossType;

    @Getter
    private EnumDungeonBoss dungeonBoss;

    @Getter
    private int objective;

    public CombatObjective(EnumCombatGoal combatGoal, int goal) {
        super("newObjective", EnumObjectiveType.COMBAT, true);
        this.combatGoal = combatGoal;
        this.objective = goal;
    }

    public CombatObjective(EnumBossType bossType) {
        super("newObjective", EnumObjectiveType.COMBAT, true);
        this.bossType = bossType;
        this.combatGoal = EnumCombatGoal.BOSS;
    }

    public CombatObjective(EnumDungeonBoss dungeonBoss) {
        super("newObjective", EnumObjectiveType.COMBAT, true);
        this.dungeonBoss = dungeonBoss;
        this.combatGoal = EnumCombatGoal.BOSS;
    }
}
