package net.dungeonrealms.game.mechanic.dot;

import org.bukkit.entity.LivingEntity;

public class PoisonDot extends DamageOverTime {

    public PoisonDot(LivingEntity target, LivingEntity source, double damage, double decrement) {
        super(target, source, damage, decrement);
    }

    @Override
    public void handleFinish() {
    }

    @Override
    public DotType getType() {
        return DotType.POISON;
    }
}
