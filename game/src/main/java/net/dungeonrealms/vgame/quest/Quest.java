package net.dungeonrealms.vgame.quest;

import lombok.Getter;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.TreeMap;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Quest
{
    @Getter
    private String name;

    @Getter
    private String[] description;

    @Getter
    private String[] conversation;

    @Getter
    private TreeMap<Integer, QuestPhase> questPhases;

    @Getter
    private Location start;

    public Quest(String name, String[] description, String[] conversation, Location location)
    {
        this.name = name;
        this.description = description;
        this.questPhases = new TreeMap<>();
        this.start = location;
        this.conversation = conversation;
    }

    public void addPhase(QuestPhase questPhase)
    {
        int id = this.questPhases.size();
        this.questPhases.put(id++, questPhase);
    }

    public void sendPhase(QuestPhase questPhase, GamePlayer gamePlayer)
    {
        if (gamePlayer.getQuest() == this)
        {
            gamePlayer.setQuestPhase(questPhase);
            for (String string : questPhase.getDescription())
            {
                String message = ChatColor.translateAlternateColorCodes('&', string);
                gamePlayer.getPlayer().sendMessage(message);
            }
        }
    }

    public void sendPhase(int id, GamePlayer gamePlayer)
    {
        if (gamePlayer.getQuest() == this)
        {
            gamePlayer.setQuestPhase(this.questPhases.get(id));
            gamePlayer.setQuestPhaseId(id);

            for (String string : this.questPhases.get(id).getDescription())
            {
                String message = ChatColor.translateAlternateColorCodes('&', string);
                gamePlayer.getPlayer().sendMessage(message);
            }
        }
    }

    public void sendNextPhase(GamePlayer gamePlayer)
    {
        if (gamePlayer.getQuest() == this)
        {
            try
            {
                QuestPhase nextPhase = this.questPhases.get(gamePlayer.getQuestPhaseId() + 1);
                gamePlayer.setQuestPhase(nextPhase);
                gamePlayer.setQuestPhaseId(gamePlayer.getQuestPhaseId() + 1);
                for (String string : nextPhase.getDescription())
                {
                    String message = ChatColor.translateAlternateColorCodes('&', string);
                    gamePlayer.getPlayer().sendMessage(message);
                }
            } catch (Exception e)
            {
                gamePlayer.setQuest(null); // No new phase? Quest finished.
            }
        }
    }

    private void complete(GamePlayer gamePlayer)
    {

    }
}
