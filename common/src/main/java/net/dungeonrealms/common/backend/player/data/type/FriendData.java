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
public class FriendData implements IData
{
    @Getter
    private UUID owner;

    public FriendData(UUID uuid, Document document)
    {
        this.owner = uuid;
        this.friends = document.get("friends", ArrayList.class);
        this.friendRequesters = document.get("friendsRequests", ArrayList.class);
    }

    @Getter
    @Setter
    private List<String> friends;

    @Getter
    @Setter
    private List<String> friendRequesters;
}
