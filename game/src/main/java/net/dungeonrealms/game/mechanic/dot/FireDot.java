package net.dungeonrealms.game.mechanic.dot;

import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FireDot extends DamageOverTime {

    public FireDot(LivingEntity target, LivingEntity source, double damage, double decrement) {
        super(target, source, damage, decrement);
    }

    @Override
    public void handleFinish() {
        if (getTarget() instanceof Player) {
            getTarget().sendMessage(ChatColor.GRAY + "Your fire has been extinguished!");
        }
    }

    @Override
    public DotType getType() {
        return DotType.FIRE;
    }
}
