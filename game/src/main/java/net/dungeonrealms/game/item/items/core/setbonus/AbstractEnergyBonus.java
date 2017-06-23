package net.dungeonrealms.game.item.items.core.setbonus;

import org.bukkit.entity.Player;

public class AbstractEnergyBonus extends SetBonus implements EnergyBonus {

    private int energy;

    public AbstractEnergyBonus(String description, int energy) {
        super(description);
        this.energy = energy;
    }

    @Override
    public int getEnergyAmount() {
        return energy;
    }

    @Override
    public void onSetBonusDeactivate(Player player) {
    }
}
