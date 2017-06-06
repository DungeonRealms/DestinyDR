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
    private boolean isHeal;

    public DamageOverTime(LivingEntity target, LivingEntity source, double initialDamage, double decrement, boolean isHeal) {
        this.target = target;
        this.source = source;
        this.damage = initialDamage;
        this.decrement = decrement;
        this.isHeal = isHeal;
    }

    public void tick() {
        if(!isHeal) {
            AttackResult res = new AttackResult(source, target);
            res.setDamage(damage);
            HealthHandler.damageEntity(res);
            if(source instanceof Player)DamageAPI.createDamageHologram((Player)source,target.getLocation(), getDamageIndicator((int)Math.ceil(damage)));
        } else {
            HealthHandler.heal(target,(int)Math.ceil(damage), true);
        }
        damage -= decrement;
    }

    public String getDamageIndicator(int damage) {
         return getSymbol() + getSign() + damage;
    }

    public abstract String getSymbol();

    public char getSign() {
        return isHeal ? '+' : '-';
    }

    public boolean isFinished() {
        return damage <= 0;
    }

    public abstract void handleFinish();
}
