package net.dungeonrealms.common.backend.database.mongo.nest;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bson.Document;

import java.util.UUID;

/**
 * Created by Giovanni on 18-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class NestDocument
{
    // TODO realm stuff, more collectible data & clean up sections

    @Getter
    private EnumNestType nestType;

    public NestDocument(EnumNestType nestType)
    {
        this.nestType = nestType;
    }

    public Document generate(UUID uuid)
    {
        Document document = null;
        switch (this.nestType)
        {
            case PLAYER:
                document = new Document("pinfo", new Document("uniqueId", uuid.toString())
                        // 1
                        .append("health", 50).append("ecash", 0).append("gems", 0).append("netLevel", 1).append("experience", 0D)
                        .append("hearthstone", "CYRENNICA").append("currentLocation", "").append("isPlaying", true).append("friends", Lists.newArrayList())
                        .append("alignment", "LAWFUL").append("guild", "").append("shopOpen", false).append("foodLevel", 20).append("shopLevel", 1).append("deadLogger", false))
                        // 2
                        .append("collectibles", new Document("achievements", Lists.newArrayList()))
                        // 3
                        .append("group", new Document("expirationDate", 0).append("currentRank", "DEFAULT"))
                        // 4
                        .append("settings", new Document("pvp", false).append("duel", false).append("receiveMessage", true).append("trading", true).append("tradeChat", true)
                                .append("chaoticPrevention", true).append("globalChat", true))
                        // 5
                        .append("notifications", new Document("guildInvitations", Lists.newArrayList()).append("friendRequests", Lists.newArrayList()))
                        // 6
                        .append("keyShards", new Document("tier1", 0).append("tier2", 0).append("tier3", 0).append("tier4", 0).append("tier5", 0))
                        // 7
                        .append("inventory", new Document("collectionBin", "").append("player", "").append("mule", "empty").append("storage", "").append("storageLvl", 0)
                                .append("armorContents", ""));
                break;
            default:
                break;
        }
        return document;
    }
}
