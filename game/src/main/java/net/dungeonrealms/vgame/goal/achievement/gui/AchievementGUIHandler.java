package net.dungeonrealms.vgame.goal.achievement.gui;

import net.dungeonrealms.common.awt.handler.SuperHandler;
import net.dungeonrealms.vgame.Game;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class AchievementGUIHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getInventory().getName() != null)
        {
            if (event.getInventory().getName().equalsIgnoreCase("Achievements"))
            {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                if (event.getCurrentItem() != null || event.getCurrentItem().getType() != Material.AIR)
                {
                    if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName())
                    {
                        ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                        if(itemMeta.getDisplayName().contains("Exploration"))
                        {

                        }
                        if(itemMeta.getDisplayName().contains("Combat"))
                        {

                        }
                    }
                }
            }
        }
    }
}
