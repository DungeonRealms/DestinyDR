package net.dungeonrealms.vgame.player.goal.achievement;

import lombok.Getter;
import net.dungeonrealms.vgame.player.GamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Achievement {
    @Getter
    private String name;

    @Getter
    private String[] description;

    @Getter
    private int expReward;

    @Getter
    private String collectionName;

    public Achievement(String name, int expReward, String[] description, String collectionName) {
        this.name = name;
        this.description = description;
        this.expReward = expReward;
        this.collectionName = collectionName;
    }

    public void reward(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (!gamePlayer.getData().getCollectionData().getAchievements().contains(this.collectionName)) {
            gamePlayer.getData().getCollectionData().getAchievements().add(this.collectionName);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7ACHIEVEMENT GET: " + this.name));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.description[1]));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.65f);
        }
    }
}
