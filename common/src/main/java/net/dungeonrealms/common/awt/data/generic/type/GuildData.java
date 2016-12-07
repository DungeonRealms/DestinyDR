package net.dungeonrealms.common.awt.data.generic.type;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.common.awt.data.generic.IData;
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
public class GuildData implements IData {
    @Getter
    private UUID owner;

    public GuildData(UUID uuid, Document document) {
        this.owner = uuid;
        this.guild = document.getString("guild");
        this.guildInvitations = document.get("guildInvitations", ArrayList.class);
    }

    @Getter
    @Setter
    private String guild;

    @Getter
    @Setter
    private List<String> guildInvitations;
}
