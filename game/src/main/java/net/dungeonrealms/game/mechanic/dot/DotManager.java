package net.dungeonrealms.game.mechanic.dot;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DotManager implements GenericMechanic {

    //This uses a tuple so we can store a healing dot (regen) and a damage dot at the same time.
    private static ConcurrentHashMap<Entity, Tuple<DamageOverTime, DamageOverTime>> currentDots = new ConcurrentHashMap<>();

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for(Map.Entry<Entity, Tuple<DamageOverTime, DamageOverTime>> set : currentDots.entrySet()) {
                Entity entity = set.getKey();
                Tuple<DamageOverTime, DamageOverTime> dots = set.getValue();
                DamageOverTime dot = dots.a();
                DamageOverTime healDot = dots.b();
                boolean isCompletelyFinished = true;
                if(entity == null || entity.isDead()) {
                    currentDots.remove(entity);
                    continue;
                }
                if(dot != null) {
                    if(!dot.isFinished()) {
                        dot.tick();
                        isCompletelyFinished = false;
                    }
                }
                if(healDot != null) {
                    if(!healDot.isFinished()) {
                        healDot.tick();
                        isCompletelyFinished = false;
                    }
                }
                if(isCompletelyFinished) {
                    currentDots.remove(entity);
                }
            }
        }, 20,20);
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void stopInvocation() {
        currentDots.clear();
    }

    public static boolean hasDamageOverTime(Entity ent, DotType type) {
        Tuple<DamageOverTime, DamageOverTime> values = currentDots.get(ent);
        if(values == null) return false;

        if(type.isHeal()) return values.b() != null && values.b().getType().equals(type);

        return values.a() != null && values.a().getType().equals(type);
    }

    public static boolean hasDamageOverTime(Entity ent, boolean isHeal) {
        Tuple<DamageOverTime, DamageOverTime> values = currentDots.get(ent);
        if(values == null) return false;

        if(isHeal) return values.b() != null;
        return values.a() != null;
    }

    public static void addDamageOverTime(Entity ent,DamageOverTime dot, boolean override) {
        Tuple<DamageOverTime, DamageOverTime> values = currentDots.get(ent);
        if(values == null) {
            Tuple<DamageOverTime, DamageOverTime> newDots = new Tuple<>(dot.getType().isHeal() ? null : dot, !dot.getType().isHeal() ? null : dot);
            currentDots.put(ent, newDots);
            return;
        }

        DamageOverTime currentDot = values.a();
        DamageOverTime currentHeal = values.b();

        if(!override) {
            if(!dot.getType().isHeal()) {
                if(currentDot != null) return;
            } else {
                if(currentHeal != null) return;
            }
        }

        Tuple<DamageOverTime, DamageOverTime> newValues = new Tuple<>(dot.getType().isHeal() ? currentDot : dot, !dot.getType().isHeal() ? currentHeal : dot);
        currentDots.put(ent, newValues);
    }
}
