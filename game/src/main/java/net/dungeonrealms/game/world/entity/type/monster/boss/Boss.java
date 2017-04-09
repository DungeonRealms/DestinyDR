package net.dungeonrealms.game.world.entity.type.monster.boss;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/23/2016
 */
public interface Boss {

    void onBossDeath();

    void onBossAttack(EntityDamageByEntityEvent event);

}
