package net.dungeonrealms.game.mechanic;

import lombok.Getter;
import net.dungeonrealms.common.game.util.ChatColor;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class HealTracker {

    private long HEAL_DEBUFF_DELAY = TimeUnit.SECONDS.toMillis(10);
    private long lastHeal = 0;

    private int sicknessLevel;

    @Getter
    private Player playerHealing;

    public HealTracker(Player player) {
        this.playerHealing = player;
        this.sicknessLevel = 1;
        this.lastHeal = System.currentTimeMillis();
    }

    private static final DecimalFormat format = new DecimalFormat("#.#");

    public String getHealTimer() {
        long timeSinceHealed = getTimeSinceHealed();
        if (timeSinceHealed <= HEAL_DEBUFF_DELAY) {
            //Still cant do it..
            long millisLeft = HEAL_DEBUFF_DELAY - timeSinceHealed;

            double secondsLeft = millisLeft / 1000D;

//            Bukkit.getLogger().info("Seconds left: " + secondsLeft);
            if (secondsLeft <= 0) return null;
            if (secondsLeft < .1) secondsLeft = .1;
            String time = format.format(secondsLeft) + "s";

            return ChatColor.RED + "Potion Sickness " + getRomanNumeral(sicknessLevel) + " (" + time + ") " + ChatColor.BOLD + "-" + (int) ((1 - getHealMultiplier()) * 100) + "% Healing";
        }

        return null;
    }


    public double getHealMultiplier() {
        return sicknessLevel == 1 ? .75D : sicknessLevel == 2 ? .6D : sicknessLevel == 3 ? .5D : .3D;
    }

    private String getRomanNumeral(int num) {
        return num == 1 ? "I" : num == 2 ? "II" : num == 3 ? "III" : num == 4 ? "IV" : String.valueOf(num);
    }

    public boolean isOnCooldown() {
        return getTimeSinceHealed() <= HEAL_DEBUFF_DELAY;
    }

    private long getTimeSinceHealed() {
        return System.currentTimeMillis() - this.lastHeal;
    }

    public void trackHeal() {

        if (isOnCooldown())
            sicknessLevel++;
        else
            sicknessLevel = 1;


        this.lastHeal = System.currentTimeMillis();
    }
}
