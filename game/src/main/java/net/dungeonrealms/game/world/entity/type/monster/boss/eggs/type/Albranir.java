package net.dungeonrealms.game.world.entity.type.monster.boss.eggs.type;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumBoss;
import net.dungeonrealms.game.world.entity.type.monster.boss.eggs.SummonedBoss;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public class Albranir implements SummonedBoss {
    @Override
    public EnumBoss getEnumBoss() {
        return null;
    }

    @Override
    public Map<String, Integer[]> getAttributes() {
        return null;
    }

    @Override
    public void onBossDeath() {

    }

    @Override
    public void onBossHit(EntityDamageByEntityEvent event) {

    }

    @Override
    public ItemStack createEgg() {
        return null;
    }

    @Override
    public boolean checkRequirements(Player player) {
        return false;
    }
}
