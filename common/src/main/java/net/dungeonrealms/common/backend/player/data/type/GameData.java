package net.dungeonrealms.common.backend.player.data.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.backend.player.data.IData;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 19-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameData implements IData {
    @Getter
    private UUID owner;

    public GameData(UUID uuid, Document document) {
        this.owner = uuid;
        this.hasDeadLogger = document.getBoolean("hasDeadLogger");
        this.hearthstoneLocationBlob = document.getString("hearthstoneLocation");
        this.alignmentBlob = document.getString("currentAlignment");
        this.currentLocationBlob = document.getString("currentLocation");
        this.level = document.getInteger("level");
        this.exp = document.getDouble("experience");
        this.gems = document.getInteger("gems");
        this.currentFood = document.getInteger("currentFoodLevel");
        this.ecash = document.getInteger("ecash");
        this.health = document.getInteger("health");
        this.playerKills = document.getInteger("playerKills");
        this.monsterKills = document.getInteger("monsterKills");
        this.killedBosses = document.get("bossKills", ArrayList.class);
    }

    @Getter
    @Setter
    private boolean hasDeadLogger;

    @Getter
    @Setter
    private String hearthstoneLocationBlob;
    @Getter
    @Setter
    private String alignmentBlob;
    @Getter
    @Setter
    private String currentLocationBlob;

    // Numeric
    @Getter
    @Setter
    private int level;
    @Getter
    @Setter
    private double exp;
    @Getter
    @Setter
    private int gems;
    @Getter
    @Setter
    private int currentFood;
    @Getter
    @Setter
    private int ecash;
    @Getter
    @Setter
    private int health;
    @Getter
    @Setter
    private int playerKills;
    @Getter
    @Setter
    private int monsterKills;
    @Getter
    @Setter
    private List<String> killedBosses;
}
