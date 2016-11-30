package net.dungeonrealms.vgame.player.goal.achievement.type;

import lombok.Getter;
import net.dungeonrealms.vgame.Game;
import net.dungeonrealms.vgame.player.GamePlayer;
import net.dungeonrealms.vgame.player.goal.achievement.Achievement;
import net.dungeonrealms.vgame.player.goal.achievement.IAchievement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Giovanni on 22-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class ExplorerAchievement extends Achievement implements IAchievement {
    @Getter
    private String attachedRegion;

    public ExplorerAchievement(String name, String[] description, int expReward, String regionName, String collectionName) {
        super(name, expReward, description, collectionName);
        this.attachedRegion = regionName;
    }

    @Override
    public void register() {
        Game.getGame().getServer().getScheduler().scheduleAsyncRepeatingTask(Game.getGame(), () ->
        {
            for (Player player : Bukkit.getOnlinePlayers()) {
                GamePlayer gamePlayer = Game.getGame().getRegistryHandler().getPlayerRegistry().getMap().get(player.getUniqueId());
                if (!gamePlayer.getData().getCollectionData().getAchievements().contains(this.getCollectionName())) {
                    this.reward(gamePlayer);
                }
            }
        }, 0L, 20);
    }
}
