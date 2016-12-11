package net.dungeonrealms.database.api;

import lombok.Getter;
import net.dungeonrealms.database.Database;

/**
 * Created by Giovanni on 10-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class MongoAPI {

    @Getter
    private Database database;

    private GuildConnection guildConnection;

    private PlayerConnection playerConnection;

    public MongoAPI(Database database) {
        this.database = database;
        this.guildConnection = new GuildConnection(this.database);
        this.playerConnection = new PlayerConnection(this.database);
    }

    /**
     * Get the guilds database api
     *
     * @return The API(connection)
     */
    public GuildConnection guilds() {
        return this.guildConnection;
    }

    /**
     * Get the player database api
     *
     * @return The API(connection)
     */
    public PlayerConnection players() {
        return this.playerConnection;
    }
}
