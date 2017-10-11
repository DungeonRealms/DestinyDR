package net.dungeonrealms.game.player.altars.recipes;

import net.dungeonrealms.game.player.altars.Altars;
import org.bukkit.entity.Player;

/**
 * Created by Rar349 on 8/3/2017.
 */
public abstract class AbstractRecipe {

    public abstract boolean checkRecipe(Altars altarType);

    public abstract void giveReward(Player player);

    public abstract String getRewardDisplayName();

    public abstract String getRewardDescription();

    public abstract boolean isUnlocked(Player player);

    public abstract long getRitualTime();
}
