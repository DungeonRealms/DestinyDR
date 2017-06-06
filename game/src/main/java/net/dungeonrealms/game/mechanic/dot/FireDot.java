package net.dungeonrealms.game.mechanic.dot;

import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class FireDot extends DamageOverTime {

    public FireDot(LivingEntity target, LivingEntity source, double damage, double decrement) {
        super(target, source, damage, decrement, false);
    }

    @Override
    public void handleFinish() {
        if (getTarget() instanceof Player) {
            getTarget().sendMessage(ChatColor.GRAY + "Your fire has been extinguished!");
            if (getSource() instanceof Player)
                getSource().sendMessage(ChatColor.GRAY + getTarget().getName() + " has been extinguished!");
        }
    }

    @Override
    public String getSymbol() {
        return "\uD83D\uDD25";
    }
}
