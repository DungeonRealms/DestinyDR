package net.dungeonrealms.backend;

import net.dungeonrealms.common.awt.handler.old.SuperHandler;
import net.dungeonrealms.common.old.game.database.player.PlayerToken;
import net.dungeonrealms.network.packet.type.ServerListPacket;
import net.dungeonrealms.vgame.old.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Giovanni on 30-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class PacketHandler implements SuperHandler.Handler {
    @Override
    public void prepare() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ServerListPacket packet = new ServerListPacket();

                final Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);

                packet.target = Game.getGame().getGameShard().getShardInfo();
                packet.tokens = new PlayerToken[onlinePlayers.length];


                for (int i = 0; i < onlinePlayers.length; i++) {
                    Player player = onlinePlayers[i];
                    packet.tokens[i] = new PlayerToken(player.getUniqueId().toString(), player.getName());
                }

                Game.getGame().getGameShard().getGameClient().sendTCP(packet);
            }
        }, 0L, 3000);
    }
}
