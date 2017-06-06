package net.dungeonrealms.game.mechanic.dot;

import lombok.Getter;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.listener.combat.AttackResult;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@Getter
public abstract class DamageOverTime {

    private LivingEntity target;
    private LivingEntity source;
    private double damage;
    private double decrement;
    private boolean hasFinished = false;

    public DamageOverTime(LivingEntity target, LivingEntity source, double initialDamage, double decrement) {
        this.target = target;
        this.source = source;
        this.damage = initialDamage;
        this.decrement = decrement;
    }

    public void tick() {
        if(!getType().isHeal()) {
            AttackResult res = new AttackResult(source, target);
            res.setDamage(damage);
            HealthHandler.damageEntity(res);
            if(source instanceof Player)DamageAPI.createDamageHologram((Player)source,target.getLocation(), getDamageIndicator((int)Math.ceil(damage)));
        } else {
            HealthHandler.heal(target,(int)Math.ceil(damage), true);
            if(source instanceof Player)DamageAPI.createDamageHologram((Player)source,target.getLocation(), getDamageIndicator((int)Math.ceil(damage)));
        }
        damage -= decrement;
        if(isFinished() && !hasFinished) {
            handleFinish();
            hasFinished = true;
        }
    }

    public String getDamageIndicator(int damage) {
         return getType().getPrefix() + getType().getSign() + damage + getType().getSuffix();
    }


    public abstract DotType getType();


    public boolean isFinished() {
        return damage <= 0;
    }

    public abstract void handleFinish();
}
