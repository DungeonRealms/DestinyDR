package net.dungeonrealms.vgame.tutorial;

import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TutorialHandler implements SuperHandler.ListeningHandler
{
    @Override
    public void prepare()
    {
        Game.getGame().getServer().getPluginManager().registerEvents(this, Game.getGame());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(event.getPlayer().getUniqueId());

        if (gamePlayer.getQuest() instanceof TutorialQuest)
        {
            gamePlayer.getQuest().sendNextPhase(gamePlayer);
            gamePlayer.getPlayer().playSound(gamePlayer.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 2f, 1f);
            gamePlayer.confuse(5);
            gamePlayer.freeze(5);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(event.getPlayer().getUniqueId());

        if (gamePlayer.getQuest() instanceof TutorialQuest)
        {
            event.getRecipients().remove(event.getPlayer());
        }
    }
}
