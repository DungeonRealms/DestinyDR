package net.dungeonrealms.game.item.items.core.setbonus;

import org.bukkit.entity.Player;

public class AbstractSetBonus extends SetBonus {
    public AbstractSetBonus(String string) {
        super(string);
    }

    @Override
    public void onSetBonusActivate(Player player) {
        super.onSetBonusActivate(player);
    }

    @Override
    public void onSetBonusDeactivate(Player player) {
    }
}
