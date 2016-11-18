package net.dungeonrealms.common.backend.player;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DataPlayer
{
    // This must be attached to a GamePlayer

    @Getter
    private UUID uniqueId;
    @Getter
    private Player player;

    @Getter
    @Setter
    private int health;

    @Getter
    @Setter
    private long firstLogin;

    @Getter
    @Setter
    private long lastLogin;

    @Getter
    @Setter
    private long lastLogout;

    @Getter
    @Setter
    private boolean isPlaying;

    @Getter
    @Setter
    private int level;
    @Getter
    @Setter
    private int exp;

    @Getter
    @Setter
    private int gems;

    @Getter
    @Setter
    private String hearthstoneLocation;

    @Getter
    @Setter
    private int ecash;

    @Getter
    @Setter
    private List friends;
    @Getter
    @Setter
    private List friendRequests;

    @Getter
    @Setter
    private String guild;
    @Getter
    @Setter
    private List guildInvities;

    @Getter
    @Setter
    private String alignment;

    @Getter
    @Setter
    private String currentLocation;

    @Getter
    @Setter
    private int currentFood;

    @Getter
    @Setter
    private int shopLevel;

    @Getter
    @Setter
    private boolean loggerDead;

    // Rank stuff
    @Getter
    @Setter
    private String rank;
    @Getter
    @Setter
    private long lastPurchase;
    @Getter
    @Setter
    private List purchaseHistory;

    // Attributes are removed, so no need to hold them here

    // Storage
    @Getter
    @Setter
    private String inventoryLevel;
    @Getter
    @Setter
    private String collectionBin;
    @Getter
    @Setter
    private String mule;
    @Getter
    @Setter
    private String storage;
    @Getter
    @Setter
    private String gameInventory;
    @Getter
    @Setter
    private boolean hasShop;
    @Getter
    @Setter
    private List armorContents;

    // Portal key shards
    @Getter
    @Setter
    private int keyShardsTierOne;
    @Getter
    @Setter
    private int keyShardsTierTwo;
    @Getter
    @Setter
    private int keyShardsTierThree;
    @Getter
    @Setter
    private int keyShardsTierFour;
    @Getter
    @Setter
    private int keyShardsTierFive;

    // Misc
    @Getter
    @Setter
    private List achievements;

    public DataPlayer(UUID uuid)
    {
        this.uniqueId = uuid;
        this.player = Bukkit.getPlayer(uuid);
    }
}
