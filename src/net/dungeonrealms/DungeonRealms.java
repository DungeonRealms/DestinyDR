package net.dungeonrealms;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.commands.*;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.listeners.*;
import net.dungeonrealms.mastery.FTPUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.WebAPI;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.party.PartyMechanics;
import net.dungeonrealms.teleportation.Teleportation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Nick on 9/17/2015.
 */
public class DungeonRealms extends JavaPlugin {

    static DungeonRealms instance = null;

    public static DungeonRealms getInstance() {
        return instance;
    }

    public void onLoad() {
        Utils.log.info("DungeonRealms onLoad() ... STARTING UP");
        instance = this;
    }

    public void onEnable() {
        long START_TIME = System.currentTimeMillis() / 1000L;
        Utils.log.info("DungeonRealms onEnable() ... STARTING UP");
        Database.getInstance().initConnection();
        DatabaseAPI.getInstance().startInitialization();
        PluginManager pm = Bukkit.getPluginManager();
        BankMechanics.init();
        Utils.log.info("DungeonRealms Registering Events() ... STARTING ...");
        pm.registerEvents(new MainListener(), this);
        pm.registerEvents(new DamageListener(), this);
        pm.registerEvents(new ItemListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new BankListener(), this);
        Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");

        WebAPI.fetchPrerequisites();

        PetUtils.getInstance().startInitialization();
        Teleportation.getInstance().startInitialization();
        CombatLog.getInstance().startInitialization();
        PartyMechanics.getInstance().startInitialization();

        Utils.log.info("DungeonRealms Registering Monsters() ... STARTING ...");
        Entities.getInstance().startInitialization();
        Utils.log.info("DungeonRealms Registering Monsters() ... FINISHED!");

        Utils.log.info("DungeonRealms Registering Commands() ... STARTING ...");
        //If the command doesn't return true; the command will print in chat.
        getCommand("spawn").setExecutor(new CommandSpawn());
        getCommand("add").setExecutor(new CommandAdd());
        getCommand("analyze").setExecutor(new CommandAnalyze());
        getCommand("lag").setExecutor(new CommandLag());
        getCommand("party").setExecutor(new CommandParty());
        getCommand("set").setExecutor(new CommandSet());
        Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");
        FTPUtils.startInitialization();
        Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");

        Utils.log.info("DungeonRealms Registering Commands() ... FINISHED!");

        Utils.log.info("DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000l) / START_TIME) + "s/");

    }

    public void onDisable() {
        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
        Database.mongoClient.close();
    }

}
