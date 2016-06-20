package net.dungeonrealms.game.network.bungeecord.serverpinger;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/19/2016
 */
public interface PingResponse {

    boolean isOnline();

    String getMotd();

    int getOnlinePlayers();

    int getMaxPlayers();

}
