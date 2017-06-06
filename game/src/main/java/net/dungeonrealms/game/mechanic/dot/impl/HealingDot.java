package net.dungeonrealms.game.mechanic.dot.impl;

import net.dungeonrealms.game.mechanic.dot.DamageOverTime;
import net.dungeonrealms.game.mechanic.dot.DotType;
import org.bukkit.entity.LivingEntity;

/**
 * Created by Rar349 on 6/6/2017.
 */
public class HealingDot extends DamageOverTime {

    public HealingDot(LivingEntity target, LivingEntity source, double initialDamage, double decrement) {
        super(target, source, initialDamage, decrement);
    }

    @Override
    public DotType getType() {
        return DotType.HEALING;
    }

    @Override
    public void handleFinish() {

    }
}
