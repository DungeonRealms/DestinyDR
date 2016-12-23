package net.dungeonrealms.network.packet.type;

import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.network.packet.Packet;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/19/2016
 */

public class ServerListPacket extends Packet {


    /**
     * Packet destination
     */
    public ShardInfo target;


    /**
     * Player list token
     */
    public PlayerToken[] tokens;

}
