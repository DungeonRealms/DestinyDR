package net.dungeonrealms.game.network;

import com.jmr.wrapper.common.Connection;
import com.jmr.wrapper.common.listener.SocketListener;
import com.sk89q.jchronic.handlers.Handler;

import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.network.handlers.PacketHandler;

public class Listener implements SocketListener {

    @Override
    public void received(Connection con, Object object) {
    	PacketHandler.handlePacket(object);
    }

    @Override
    public void connected(Connection con) {
		Utils.log.warning("Connected to the DungeonRealms master server!");
    }

    @Override
    public void disconnected(Connection con) {
		Utils.log.warning("Disconnected from the DungeonRealms Master Server.");
    }

}
