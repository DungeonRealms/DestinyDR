package net.dungeonrealms.common.game.database.sql.handle;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 30-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SaveHandler implements SuperHandler.Handler
{
    private int saveItemTask;

    @Override
    public void prepare()
    {
        // Backup database
        Game.getGame().getServer().getScheduler().runTaskTimerAsynchronously(Game.getGame(), GameAPI::backupDatabase, 0L, 12000L);

        // Save all items
        this.saveItemTask = Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () -> {
            for (Player player : Game.getGame().getServer().getOnlinePlayers())
            {
                player.sendMessage((new String[]{"", "", ChatColor.YELLOW.toString() + ChatColor.BOLD + "SAVING ITEM DATA, PLEASE WAIT.."}));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1f, 1f);
            }

            // Save all data, handled by registries
            Game.getGame().getRegistryHandler().getRegistryMap().values().forEach((dataRegistry) -> dataRegistry.save());
        }, 0L, 20 * Game.getGame().getGameShard().getSaveTime());
    }
}
