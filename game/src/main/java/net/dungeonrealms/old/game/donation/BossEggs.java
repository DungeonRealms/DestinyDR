package net.dungeonrealms.old.game.donation;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.old.game.database.player.CachedClientProvider;
import net.dungeonrealms.old.game.donation.eggs.WBInstance;
import net.dungeonrealms.old.game.listener.world.BossEggListener;
import net.dungeonrealms.old.game.mastery.NMSUtils;
import net.dungeonrealms.old.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.old.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.WorldBoss;
import net.dungeonrealms.old.game.world.entity.type.monster.boss.type.world.Albranir;
import net.minecraft.server.v1_9_R2.EntityPigZombie;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/22/2016
 */

public class BossEggs extends CachedClientProvider<WBInstance> implements GenericMechanic {

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    protected WBInstance cache(OfflinePlayer player, Object... params) {
        return getCache().put(player.getUniqueId(), (WBInstance) params[0]);
    }


    @Override
    public void startInitialization() {
        Bukkit.getPluginManager().registerEvents(new BossEggListener(), DungeonRealms.getInstance());

        // REGISTER BOSSES //
        NMSUtils nmsUtils = new NMSUtils();
        nmsUtils.registerEntity("Albranir", 57, EntityPigZombie.class, Albranir.class);
    }

    @Override
    public void stopInvocation() {

    }

    public WBInstance createInstance(Player host, Location location, WorldBoss egg) {
        return cache(host, location, egg);
    }
}
