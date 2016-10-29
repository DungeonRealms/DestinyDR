package net.dungeonrealms.awt.database;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.awt.SuperHandler;
import net.dungeonrealms.awt.database.sql.MySQL;
import net.dungeonrealms.awt.database.sql.SQLResult;
import net.dungeonrealms.awt.events.DatabaseConnectEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class DatabaseHandler implements SuperHandler.Handler {

    @Getter
    private ExecutorService pool = Executors.newFixedThreadPool(2);
    @Getter
    private MySQL mySQL;

    private boolean locked = false;

    @Override
    public void prepare() {
        setupConnection();
        if (!locked) {
            locked = true;
        }
    }

    private void setupConnection() {
        FileConfiguration sql = YamlConfiguration.loadConfiguration(new File(DungeonRealms.getInstance().getDataFolder() +  "/", "sql.yml"));
        this.mySQL = new MySQL(this, sql.getString("hostname"), sql.getInt("port"), sql.getString("database"), sql.getString("username"), sql.getString("password"));
        SQLResult sqlResult;
        try {
            this.mySQL.updateConnection();
            sqlResult = SQLResult.SUCCESS;
            getLogger().log(sqlResult.getLevel(), "Connected to database " + mySQL.getHostname() + " on port " + mySQL.getPort() + " database " + mySQL.getDatabase());
        } catch (SQLException e) {
            e.printStackTrace();
            sqlResult = SQLResult.FAILED;
            getLogger().log(sqlResult.getLevel(), "Failed to connect to database");
        }
        DatabaseConnectEvent event = new DatabaseConnectEvent(true, this.mySQL, sqlResult);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
