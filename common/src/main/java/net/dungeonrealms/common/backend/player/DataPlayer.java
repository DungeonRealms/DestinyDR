package net.dungeonrealms.common.backend.player;

import lombok.Getter;
import net.dungeonrealms.common.backend.database.mongo.nest.EnumNestType;
import net.dungeonrealms.common.backend.database.mongo.nest.NestDocument;
import net.dungeonrealms.common.backend.player.data.type.*;
import net.dungeonrealms.common.backend.player.data.type.GameData;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DataPlayer
{
    /*
     A container of all data of a specific Dungeon Realms player.
     A DataPlayer container must be attached to a GamePlayer.
      */

    @Getter
    private UUID uniqueId;
    @Getter
    private Player player;

    @Getter
    private GameData gameData;

    @Getter
    private GuildData guildData;

    @Getter
    private FriendData friendData;

    @Getter
    private InventoryData inventoryData;

    @Getter
    private RankData rankData;

    @Getter
    private SettingsData settingsData;

    @Getter
    private CollectionData collectionData;

    public DataPlayer(UUID uuid, Document document)
    {
        this.uniqueId = uuid;
        this.player = Bukkit.getPlayer(uuid);
        try
        {
            this.gameData = new GameData(uuid, (Document) document.get("genericData"));
            this.guildData = new GuildData(uuid, (Document) document.get("guildData"));
            this.friendData = new FriendData(uuid, (Document) document.get("friendData"));
            this.inventoryData = new InventoryData(uuid, (Document) document.get("inventoryData"));
            this.rankData = new RankData(uuid, (Document) document.get("rankData"));
            this.settingsData = new SettingsData(uuid, (Document) document.get("settingsData"));
            this.collectionData = new CollectionData(uuid, (Document) document.get("keyShardData"), (Document) document.get("collectionData"));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Document constructRawDocument()
    {
        Document document = null;
        try
        {
            document = new NestDocument(EnumNestType.PLAYER).generateExistent(this);
        } catch (Exception e)
        {
            System.out.println("Failed to construct a raw document for: " + this.uniqueId);
            e.printStackTrace();
        }
        return document;
    }
}
