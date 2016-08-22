package net.dungeonrealms.game.world.entity.type.monster.boss.eggs.type;

import net.dungeonrealms.game.world.entity.type.monster.type.EnumBoss;
import net.dungeonrealms.game.world.entity.type.monster.boss.eggs.SummonedBoss;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public class TestBoss implements SummonedBoss {
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
}
