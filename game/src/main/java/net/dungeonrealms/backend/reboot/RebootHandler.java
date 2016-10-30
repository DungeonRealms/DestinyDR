package net.dungeonrealms.backend.reboot;

import net.dungeonrealms.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class RebootHandler implements SuperHandler.Handler
{
    private int rebootTask;

    private int rebootTime;

    private boolean lastSeconds;

    @Override
    public void prepare()
    {
        this.rebootTime = Game.getGame().getGameShard().getRebootTime();

        this.rebootTask = Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () -> {
            if (this.rebootTime <= 0)
            {
                // Save all data, handled by registries
                Game.getGame().getRegistryHandler().getRegistryMap().values().forEach((dataRegistry) -> dataRegistry.save());

                // Shutdown
                Game.getGame().getGameShard().manageSimpleStop();
                Game.getGame().getServer().shutdown();
            } else
            {
                this.rebootTime--;

                // Send a message + sound if there is 30 seconds before reboot
                if (this.rebootTime < 31 && this.rebootTime > 11 && !this.lastSeconds)
                {
                    for (Player player : Game.getGame().getServer().getOnlinePlayers())
                    {
                        player.sendMessage((new String[]{"", "", ChatColor.YELLOW.toString() + ChatColor.BOLD + "THIS SHARD IS REBOOTING IN 30 SECONDS"}));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                    }
                    this.lastSeconds = true;
                }
                // Send a message + sound if there is 10 seconds before reboot
                if (this.rebootTime < 11 && this.rebootTime > 5)
                {
                    for (Player player : Game.getGame().getServer().getOnlinePlayers())
                    {
                        player.sendMessage((new String[]{"", "", ChatColor.YELLOW.toString() + ChatColor.BOLD + "THIS SHARD IS REBOOTING IN 10 SECONDS"}));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                    }
                    // Play sound if there is 5 seconds before reboot
                } else if (this.rebootTime < 5)
                {
                    for (Player player : Game.getGame().getServer().getOnlinePlayers())
                    {
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
                    }
                }
            }
        }, 0L, 20L);
    }

    public void cancelTask()
    {
        Game.getGame().getServer().getScheduler().cancelTask(rebootTask);
    }
}
