package net.dungeonrealms.vgame.anticheat.flag;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 10/30/2016 at 12:26 PM.
 */
public class Flag {

    private Player player;

    @Getter
    @Setter
    private int speedFlags;

    @Getter
    @Setter
    private int flightFlags;

    public Flag(Player player) {
        this.player = player;
    }

    public int getTotalFlags() {
        int total = 0;
        total += speedFlags;
        total += flightFlags;
        return total;
    }

    public void addSpeedFlag() {
        speedFlags++;
    }

    public void addFightFlag() {
        flightFlags++;
    }
}
