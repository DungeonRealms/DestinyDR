package net.dungeonrealms.game.donation;

import lombok.Data;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.donation.eggs.BossEgg;
import net.dungeonrealms.game.listener.world.BossEggListener;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.type.monster.boss.Boss;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public class BossEggs implements GenericMechanic {

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Data
    public class Instance {

        private UUID host;
        private BossEgg egg;

        private Boss boss;

        private long initTime;

        public Instance(UUID host, BossEgg egg) {
            this.host = host;
            this.egg = egg;

            initTime = System.currentTimeMillis();
        }

    }

    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(new BossEggListener(), DungeonRealms.getInstance());

    }

    @Override
    public void stopInvocation() {

    }

}
