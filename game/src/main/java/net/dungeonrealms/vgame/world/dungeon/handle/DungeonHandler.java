package net.dungeonrealms.vgame.world.dungeon.handle;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.awt.BungeeHandler;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.old.game.world.teleportation.Teleportation;
import net.dungeonrealms.vgame.Game;
import org.bukkit.Bukkit;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 11/1/2016 at 2:33 PM.
 */
public class DungeonHandler implements SuperHandler.ListeningHandler {

    @Override
    public void prepare() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Game.getGame(), this::checkTask, 5L, 5L);
    }

    private void checkTask() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getWorld().getName().startsWith("DUNGEON_")) {
                if (!GameAPI.getGamePlayer(player).isInParty()) {
                    player.teleport(Teleportation.Cyrennica);
                }
            }
        });
    }
}
