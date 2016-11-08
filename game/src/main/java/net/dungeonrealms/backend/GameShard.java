package net.dungeonrealms.backend;

import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.database.sql.SQLDatabase;
import net.dungeonrealms.common.game.database.sql.enumeration.EnumSQLPurpose;
import net.dungeonrealms.common.network.enumeration.EnumShardType;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.updater.UpdateTask;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.vgame.Game;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class GameShard
{
    @Getter
    private GameClient gameClient;

    @Getter
    private EnumShardType shardType = EnumShardType.BETA; // Default

    @Getter
    private String shardId = "Giovanni-01"; // Default

    @Getter
    private String bungeeIdentifier = "US-0"; // Default

    @Getter
    private boolean instanceServer;

    @Getter
    private int realmsNumber;

    @Getter
    private int realmsPort;

    @Getter
    private int maxRealms;

    @Getter
    private int maxRealmPlayers;

    @Getter
    private ShardInfo shardInfo;

    @Getter
    private int rebootTime = 7200; // 2 hours by default.

    @Getter
    private int saveTime = 1800; // 30 minutes by default.

    @Getter
    private SQLDatabase sqlDatabase;

    public GameShard(FileReader fileReader)
    {
        Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "[ DUNGEON REALMS SHARD, INIT]");
        try
        {
            this.loadShardData(fileReader);
            this.shardInfo = ShardInfo.getByShardID(shardId);
            this.setupDatabase();
            this.connect();
            this.clearPlayerData();
            this.managePlayerData();

            new UpdateTask(Game.getGame()); // Init the updater
        } catch (Exception e)
        {
            e.printStackTrace();
            Game.getGame().getInstanceLogger().sendMessage(ChatColor.RED + "Failed to load the GameShard, shutting down.. (10)");
            for (int i = 0; i < 10; i++)
            {
                Game.getGame().getServer().shutdown();
            }
        }

        Game.getGame().getServer().setWhitelist(false);
    }

    public void manageSimpleStop()
    {
        // Just for instances/handlers that require a manual stop, no saving of data will take place here
        DatabaseAPI.getInstance().stopInvocation();

        // TODO stop all mechanics
    }

    private void managePlayerData()
    {
        UpdateResult playerFixResult = DatabaseInstance.playerData.updateMany(Filters.eq("info.current", shardInfo.getPseudoName()),
                new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false)));
        if (playerFixResult.wasAcknowledged())
        {
            Game.getGame().getInstanceLogger().sendMessage(ChatColor.GREEN + "Updated online player results");
        }
    }

    private void clearPlayerData()
    {
        try
        {
            FileUtils.deleteDirectory(new File("world" + File.separator + "playerdata"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void connect()
    {
        BungeeUtils.setPlugin(Game.getGame());
        this.gameClient = new GameClient();
        try
        {
            this.gameClient.connect();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void setupDatabase()
    {
        DatabaseInstance.getInstance().startInitialization(true);
        DatabaseAPI.getInstance().startInitialization(bungeeIdentifier);

        // MySQL for items
        this.sqlDatabase = new SQLDatabase(Constants.SQL_HOSTNAME, Constants.SQL_PORT,
                Constants.SQL_DATABASE, Constants.SQL_PASSWORD,
                Constants.SQL_USERNAME, EnumSQLPurpose.ITEM);
        Game.getGame().getInstanceLogger().sendMessage(new String[]{"",
                ChatColor.YELLOW + "[ v-ITEM DATABASE ]",
                ChatColor.GREEN + "IP: " + Constants.SQL_HOSTNAME,
                ChatColor.GREEN + "Port: " + Constants.SQL_PORT,
                ChatColor.GREEN + "Database: " + Constants.SQL_DATABASE,
                ChatColor.GREEN + "Username: " + Constants.SQL_USERNAME,
                ChatColor.GREEN + "Purpose: " + EnumSQLPurpose.ITEM.name(), ""}); // {0}
    }

    private void loadShardData(FileReader fileReader)
    {
        Ini ini = new Ini();
        try
        {
            ini.load(fileReader);

            this.instanceServer = ini.get("Backend", "instance", Boolean.class);
            this.shardId = ini.get("Backend", "shardId", String.class);
            this.bungeeIdentifier = ini.get("Backend", "bungeeId", String.class);

            this.realmsNumber = ini.get("RealmData", "number", Integer.class);
            this.realmsPort = ini.get("RealmData", "backendport", Integer.class);
            this.maxRealmPlayers = ini.get("RealmData", "maxplayers", Integer.class);
            this.maxRealms = ini.get("RealmData", "maxrealms", Integer.class);

            this.shardType = EnumShardType.valueOf(ini.get("Settings", "shard"));
            this.rebootTime = ini.get("Settings", "rebootTime", Integer.class);
            this.saveTime = ini.get("Settings", "saveTime", Integer.class);
        } catch (Exception e) // No multi exception catching here
        {
            e.printStackTrace();
        }
    }
}
