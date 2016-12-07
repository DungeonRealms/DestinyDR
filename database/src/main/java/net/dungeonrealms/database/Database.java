package net.dungeonrealms.database;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dungeonrealms.common.awt.database.connection.exception.ConnectionRunningException;
import net.dungeonrealms.common.awt.database.mongo.connection.MongoConnection;
import net.dungeonrealms.database.data.DataCache;
import net.dungeonrealms.database.packet.PacketPipeline;
import net.dungeonrealms.database.util.Logger;

import java.util.List;

/**
 * Created by Giovanni on 7-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class Database {

    @Getter
    private final String version = "1.0";

    @Getter
    private static Database instance;

    @Getter
    private static List<PacketPipeline> packetPipelines;

    @Getter
    private MongoConnection mongoConnection;

    public Database() {
        instance = this;
        packetPipelines = Lists.newArrayList();

        this.registerPipeline(new DataCache());

        // Connect the Mongo to the default Dungeon Realms database, so we can interact w/ player data
        this.mongoConnection = new MongoConnection();
        try {
            this.mongoConnection.runOn("", "dungeonrealms");
        } catch (ConnectionRunningException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Logger.info(false, "DUNGEON REALMS DATACACHE - STARTING");
        new Database();
        Logger.info(false, "Listening packet pipelines: " + packetPipelines.size());
    }

    public void registerPipeline(PacketPipeline packetPipeline) {
        packetPipelines.add(packetPipeline);
    }
}
