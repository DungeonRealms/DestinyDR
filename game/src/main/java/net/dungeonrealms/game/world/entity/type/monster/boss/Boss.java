package net.dungeonrealms.game.world.entity.type.monster.boss;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/23/2016
 */
public interface Boss {

    Map<String, Integer[]> getAttributes();

    void onBossDeath();

    void onBossAttack(EntityDamageByEntityEvent event);

}
