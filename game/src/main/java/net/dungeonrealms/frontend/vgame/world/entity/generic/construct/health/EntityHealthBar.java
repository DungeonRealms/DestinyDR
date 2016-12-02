package net.dungeonrealms.frontend.vgame.world.entity.generic.construct.health;

import lombok.Getter;
import net.dungeonrealms.frontend.vgame.world.entity.generic.IGameEntity;
import org.bukkit.ChatColor;

import java.text.DecimalFormat;

/**
 * Created by Giovanni on 28-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class EntityHealthBar {

    @Getter
    private String asString;

    public EntityHealthBar(IGameEntity gameEntity) {
        // [Lvl. $] {health in bar format}
        DecimalFormat decimalFormat = new DecimalFormat("##.#");

        String defaultName = ChatColor.AQUA + "[Lvl. " + gameEntity.getEntityData().getLevel() + "] ";

        String formattedHealth = decimalFormat.format(gameEntity.getEntity().getHealth() / gameEntity.getEntity().getMaxHealth());
        float percentageX = Math.round(100.0F * Float.parseFloat(formattedHealth)); // as %

        ChatColor chatColor = ChatColor.GREEN; // Default
        if (percentageX <= 50) {
            chatColor = ChatColor.YELLOW;
        }
        if (percentageX <= 25) {
            chatColor = ChatColor.RED;
        }

        // Actual display
        int displaySize = gameEntity.getEntityData().getEntityTier().getHealthSize();
        int healthBars = 0;

        defaultName += chatColor + ChatColor.BOLD.toString() + "║" + ChatColor.RESET.toString() + chatColor + "";

        while (percentageX > 0 && healthBars < displaySize) {
            percentageX -= (100.0F / displaySize);
            healthBars++;
            defaultName += "|";
        }

        defaultName += chatColor + ChatColor.BOLD.toString() + "║";

        this.asString = defaultName;
    }
}
