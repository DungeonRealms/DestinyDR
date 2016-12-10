package net.dungeonrealms.database.api.player.generic.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.frontend.rank.EnumPlayerRank;
import net.dungeonrealms.database.api.player.generic.IData;
import org.bson.Document;

import java.util.UUID;

/**
 * Created by Giovanni on 19-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class RankData implements IData {

    @Getter
    private UUID owner;

    public RankData(UUID owner, Document document) {
        this.owner = owner;
        this.rank = EnumPlayerRank.valueOf(document.getString("currentRank"));
    }

    @Getter
    @Setter
    private EnumPlayerRank rank;
}
