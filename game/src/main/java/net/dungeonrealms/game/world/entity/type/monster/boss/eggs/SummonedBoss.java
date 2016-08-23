package net.dungeonrealms.game.world.entity.type.monster.boss.eggs;

import net.dungeonrealms.game.world.entity.type.monster.boss.Boss;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public interface SummonedBoss extends Boss {


    ItemStack createEgg();

    boolean checkRequirements(Player player);

}
