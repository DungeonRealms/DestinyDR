package net.dungeonrealms;

import net.dungeonrealms.game.listeners.channel.ProxyChannelListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mongo.Database;
import net.md_5.bungee.api.plugin.Plugin;

public class DungeonRealmsProxy extends Plugin {

    private static DungeonRealmsProxy instance;

    @Override
    public void onEnable() {
        instance = this;
        Utils.log.info("DungeonRealmsProxy onEnable() ... STARTING UP");
        Database.getInstance().startInitialization();

        this.getProxy().getPluginManager().registerListener(this, ProxyChannelListener.getInstance());
    }

    public static DungeonRealmsProxy getInstance() {
        return instance;
    }
}
