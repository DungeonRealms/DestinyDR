package net.dungeonrealms.vgame.quest;

import lombok.Getter;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.util.UUID;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class QuestPhase
{
    @Getter
    private EnumQuestObjective questObjective;

    @Getter
    private String[] description;

    @Getter
    private String[] conversation;

    @Getter
    private UUID uuid;

    public QuestPhase(UUID uuid, String[] description, String[] conversation, EnumQuestObjective objective)
    {
        this.description = description;
        this.questObjective = objective;
        this.uuid = uuid;
        this.conversation = conversation;
    }

    public void complete(GamePlayer gamePlayer)
    {
        if (gamePlayer.getQuestPhase() == this)
        {
            gamePlayer.getPlayer().sendMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + "✦ OBJECTIVE COMPLETE!");
            for (String string : description)
            {
                gamePlayer.getPlayer().sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + string);
            }
            gamePlayer.getPlayer().playSound(gamePlayer.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }
        Game.getGame().getServer().getScheduler().scheduleAsyncDelayedTask(Game.getGame(), () -> gamePlayer.getQuest().sendNextPhase(gamePlayer), 35);
    }
}
