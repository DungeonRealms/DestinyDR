package net.dungeonrealms.vgame.player;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.vgame.quest.Quest;
import net.dungeonrealms.vgame.quest.QuestPhase;
import net.dungeonrealms.vgame.tutorial.TutorialQuest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GamePlayer implements IPlayer
{
    @Setter
    @Getter
    private UUID uuid;

    @Setter
    @Getter
    private int gems;

    @Setter
    @Getter
    private int exp;

    @Setter
    @Getter
    private int level;

    @Setter
    @Getter
    private Quest quest;
    @Getter
    @Setter
    private QuestPhase questPhase;
    @Setter
    @Getter
    private int questPhaseId;

    @Getter
    private Player player;

    public GamePlayer(UUID uuid)
    {
        this.uuid = uuid;
        this.gems = 0;
        this.exp = 0;
        this.level = 0;
        this.quest = new TutorialQuest();
        this.player = Bukkit.getPlayer(uuid);
    }

    public void addExp(int par1)
    {
        this.exp += par1;
    }

    public void removeExp(int par1)
    {
        this.exp -= par1;
    }

    public void addGems(int par1)
    {
        this.gems += par1;
    }

    public void removeGems(int par1)
    {
        this.gems -= par1;
    }
}
