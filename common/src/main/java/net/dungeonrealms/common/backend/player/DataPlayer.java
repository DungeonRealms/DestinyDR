package net.dungeonrealms.common.backend.player;

import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
    // This must be attached to a GamePlayer, all data in this is RAW, it must be converted first.

    // TODO realm stuff, more collectible data & clean up sections

    @Getter
    private UUID uniqueId;
    @Getter
    private Player player;

    @Getter
    @Setter
    private int health;

    @Getter
    @Setter
    private boolean isPlaying;

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
    private String hearthstoneLocation;

    @Getter
    @Setter
    private int ecash;

    @Getter
    @Setter
    private List<String> friends;
    @Getter
    @Setter
    private List<String> friendRequests;

    @Getter
    @Setter
    private String guild;
    @Getter
    @Setter
    private List<String> guildInvites;

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
    private int expirationDate;

    // Attributes are removed, so no need to hold them here

    // Settings
    @Getter
    @Setter
    private boolean globalChat;
    @Getter
    @Setter
    private boolean tradeChat;
    @Getter
    @Setter
    private boolean trading;
    @Getter
    @Setter
    private boolean receiveMessage;
    @Getter
    @Setter
    private boolean pvp;
    @Getter
    @Setter
    private boolean chaoticPrevention;
    @Getter
    @Setter
    private boolean allowDuels;

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
    private List<String> armorContents;

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
    private List<String> achievements;

    public DataPlayer(UUID uuid, Document document)
    {
        this.uniqueId = uuid;
        this.player = Bukkit.getPlayer(uuid);
        // Alright, here we go..
        Document playerInfo = (Document) document.get("pinfo");
        this.health = playerInfo.getInteger("health");
        this.ecash = playerInfo.getInteger("ecash");
        this.gems = playerInfo.getInteger("gems");
        this.level = playerInfo.getInteger("netLevel");
        this.exp = playerInfo.getDouble("experience");
        this.hearthstoneLocation = playerInfo.getString("hearthstone");
        this.currentLocation = playerInfo.getString("currentLocation");
        this.isPlaying = playerInfo.getBoolean("isPlaying");
        this.friends = playerInfo.get("friends", ArrayList.class);
        this.alignment = playerInfo.getString("alignment");
        this.guild = playerInfo.getString("guild");
        this.hasShop = playerInfo.getBoolean("shopOpen");
        this.currentFood = playerInfo.getInteger("foodLevel");
        this.shopLevel = playerInfo.getInteger("shopLevel");
        this.loggerDead = playerInfo.getBoolean("deadLogger");

        Document collectibles = (Document) document.get("collectibles");
        this.achievements = collectibles.get("achievements", ArrayList.class);

        Document group = (Document) document.get("group");
        this.rank = group.getString("currentRank");
        this.expirationDate = group.getInteger("expirationDate");

        // TODO
    }
}
