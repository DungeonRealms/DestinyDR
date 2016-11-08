package net.dungeonrealms.vgame.tutorial;

import net.dungeonrealms.vgame.quest.EnumQuestObjective;
import net.dungeonrealms.vgame.quest.Quest;
import net.dungeonrealms.vgame.quest.QuestPhase;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.UUID;

/**
 * Created by Giovanni on 8-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TutorialQuest extends Quest
{
    // TODO
    public TutorialQuest()
    {
        super(ChatColor.GREEN.toString() + ChatColor.BOLD + "PROLOGUE", new String[]{"&eWelcome to Dungeon Realms!"},
                new String[]{"",
                        "&eSailor: &fAgh! Oi lad, mind givin me a help instead of standing der aye?"}, new Location(null, 0, 0, 0));

        this.addPhase(new QuestPhase(UUID.fromString("a847b8d6-a5f2-11e6-80f5-76304dec7eb7"),
                new String[]{"&7> &aTalk to the &eship's sailor"},
                new String[]{"&eSailor:"}, EnumQuestObjective.TALK_NPC));
    }
}
