package net.dungeonrealms.vgame.goal.objective.combat;

import lombok.Getter;
import net.dungeonrealms.vgame.goal.objective.EnumObjectiveType;
import net.dungeonrealms.vgame.goal.objective.Objective;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class CombatObjective extends Objective
{
    @Getter
    private EnumCombatGoal combatGoal;

    @Getter
    private int objective;

    public CombatObjective(EnumCombatGoal combatGoal, int goal)
    {
        super("newObjective", EnumObjectiveType.COMBAT, true);
        this.combatGoal = combatGoal;
        this.objective = goal;
    }
}
