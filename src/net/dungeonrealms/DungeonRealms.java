package net.dungeonrealms;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.commands.CommandAccept;
import net.dungeonrealms.commands.CommandAdd;
import net.dungeonrealms.commands.CommandEss;
import net.dungeonrealms.commands.CommandGlobalChat;
import net.dungeonrealms.commands.CommandGuild;
import net.dungeonrealms.commands.CommandInvoke;
import net.dungeonrealms.commands.CommandLag;
import net.dungeonrealms.commands.CommandList;
import net.dungeonrealms.commands.CommandLogout;
import net.dungeonrealms.commands.CommandMail;
import net.dungeonrealms.commands.CommandModeration;
import net.dungeonrealms.commands.CommandPAccept;
import net.dungeonrealms.commands.CommandPChat;
import net.dungeonrealms.commands.CommandPLeave;
import net.dungeonrealms.commands.CommandPRemove;
import net.dungeonrealms.commands.CommandPl;
import net.dungeonrealms.commands.CommandRank;
import net.dungeonrealms.commands.CommandRedeem;
import net.dungeonrealms.commands.CommandRoll;
import net.dungeonrealms.commands.CommandSet;
import net.dungeonrealms.commands.CommandSpawn;
import net.dungeonrealms.commands.CommandStats;
import net.dungeonrealms.commands.CommandStop;
import net.dungeonrealms.commands.CommandStuck;
import net.dungeonrealms.commands.generic.CommandManager;
import net.dungeonrealms.donate.DonationEffects;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.handlers.EnergyHandler;
import net.dungeonrealms.handlers.HealthHandler;
import net.dungeonrealms.handlers.KarmaHandler;
import net.dungeonrealms.handlers.ScoreboardHandler;
import net.dungeonrealms.listeners.AntiCheatListener;
import net.dungeonrealms.listeners.BankListener;
import net.dungeonrealms.listeners.BlockListener;
import net.dungeonrealms.listeners.BossListener;
import net.dungeonrealms.listeners.DamageListener;
import net.dungeonrealms.listeners.EnergyListener;
import net.dungeonrealms.listeners.InventoryListener;
import net.dungeonrealms.listeners.ItemListener;
import net.dungeonrealms.listeners.MainListener;
import net.dungeonrealms.listeners.ShopListener;
import net.dungeonrealms.loot.LootManager;
import net.dungeonrealms.mastery.AsyncUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.DungeonManager;
import net.dungeonrealms.mechanics.generic.MechanicManager;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.achievements.AchievementManager;
import net.dungeonrealms.network.NetworkAPI;
import net.dungeonrealms.network.NetworkServer;
import net.dungeonrealms.party.Affair;
import net.dungeonrealms.profession.Fishing;
import net.dungeonrealms.profession.Mining;
import net.dungeonrealms.rank.Rank;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.BuffManager;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.dungeonrealms.teleportation.Teleportation;
import net.dungeonrealms.world.realms.Instance;

/* Copyright (C) 2015 CherryIO, LLC - All Rights Reserved http://cherryio.com

 * Unauthorized copying and or modifying of this file, via any medium is
 * STRICTLY prohibited Proprietary and confidential
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * Written by Nick Doran (xFinityPro) <nick@cherryio.com>, October 2015 -
 * Written by Kieran Quigley (Proxying) <Proxying@cherryio.com>, October 2015 -
 * Written by Chase Myers  (Xwaffle) <chase@cherryio.com>, October 2015 -
 *
 *
 * (a) Anyone who violates any of the exclusive rights of the copyright owners as
 * provided by sections 106 through 122 or of the author as provided in section 106A(a),
 * or who imports copies or phone-records into the United States in violation of section 602,
 * is an infringer of the copyright or right of the author, as the case may be. For purposes
 * of this chapter (other than section 506), any reference to copyright shall be deemed to
 * include the rights conferred by section 106A(a). As used in this subsection, the term anyone
 * includes any State, any instrumentality of a State, and any officer or employee of a State or
 * instrumentality of a State acting in his or her official capacity. Any State, and any such
 * instrumentality, officer, or employee, shall be subject to the provisions of this title in
 * the same manner and to the same extent as any nongovernmental entity.
 *
 * For more information regarding this legally bound copyright of individuals
 * freedom development please reference United States Copyright Laws below.
 *
 * (http://www.copyright.gov/title17/92chap5.html)
 *
 * Big thanks to our proud sponsor(s) (Casey Keeling).
 * Who is nothing more or less than a sponsor.
 * Hereinafter declaring that the individual shall NOT
 * hold ANY legal grounds over the source.
 *
 */
public class DungeonRealms extends JavaPlugin {

    private static DungeonRealms instance = null;

    public static DungeonRealms getInstance() {
        return instance;
    }

    public void onLoad() {
        Utils.log.info("DungeonRealms onLoad() ... STARTING UP");
        instance = this;
    }

    public MechanicManager mm = null;
    boolean hasFinishedSetup = false;

    public boolean hasFinishedSetup() {
        return hasFinishedSetup;
    }

    public void setFinishedSetup(boolean bool) {
        hasFinishedSetup = bool;
    }

    public void onEnable() {
        long START_TIME = System.currentTimeMillis() / 1000L;
        Utils.log.info("DungeonRealms onEnable() ... STARTING UP");
        saveDefaultConfig();

        Database.getInstance().startInitialization();
        DatabaseAPI.getInstance().startInitialization();
        NetworkAPI.getInstance().startInitialization();

        PluginManager pm = Bukkit.getPluginManager();
        Utils.log.info("DungeonRealms Registering Events() ... STARTING ...");
        pm.registerEvents(new MainListener(), this);
        pm.registerEvents(new DamageListener(), this);
        pm.registerEvents(new ItemListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new BlockListener(), this);
        pm.registerEvents(new BankListener(), this);
        pm.registerEvents(new EnergyListener(), this);
        pm.registerEvents(new AntiCheatListener(), this);
        pm.registerEvents(new BossListener(), this);
        pm.registerEvents(new ShopListener(), this);
        pm.registerEvents(new AchievementManager(), this);
        pm.registerEvents(Instance.getInstance(), this);
        /*
        In development
        pm.registerEvents(new RiftPortal(), this);
        pm.registerEvents(new Runes(), this);
         */
        Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");

        mm = new MechanicManager();

        mm.registerMechanic(PetUtils.getInstance());
        mm.registerMechanic(Teleportation.getInstance());
        mm.registerMechanic(CombatLog.getInstance());
        mm.registerMechanic(EnergyHandler.getInstance());
        mm.registerMechanic(Rank.getInstance());
        mm.registerMechanic(DonationEffects.getInstance());
        mm.registerMechanic(HealthHandler.getInstance());
        mm.registerMechanic(KarmaHandler.getInstance());
        mm.registerMechanic(BankMechanics.getInstance());
        mm.registerMechanic(NetworkServer.getInstance());
        mm.registerMechanic(DungeonManager.getInstance());
        mm.registerMechanic(new Entities());
        mm.registerMechanic(ScoreboardHandler.getInstance());
        /*
        Working on instance
        mm.registerMechanic(RealmManager.getInstance());
         */
        mm.registerMechanic(new ShopMechanics());
        mm.registerMechanic(Mining.getInstance());
        mm.registerMechanic(Instance.getInstance());
        mm.registerMechanic(Fishing.getInstance());
        mm.registerMechanic(SpawningMechanics.getInstance());
        mm.registerMechanic(AchievementManager.getInstance());
        mm.registerMechanic(BuffManager.getInstance());
        mm.registerMechanic(new LootManager());
        mm.registerMechanic(Affair.getInstance());

        /*
        In development
        mm.registerMechanic(RiftPortal.getInstance());
        mm.registerMechanic(Runes.getInstance());
         */

        mm.loadMechanics();

        CommandManager cm = new CommandManager();

        cm.registerCommand(new CommandGuild("guild", "/<command> [args]", "Opens the guild menu!"));
        cm.registerCommand(new CommandSpawn("spawn", "/<command> [args]", "Spawns a mob? idk chase"));
        cm.registerCommand(new CommandAdd("ad", "/<command> [args]", "Adds shit"));
        cm.registerCommand(new CommandLag("lag", "/<command> [args]", "Checks for lag."));
        cm.registerCommand(new CommandSet("set", "/<command> [args]", "SETS THE YEAH."));
        cm.registerCommand(new CommandList("list", "/<command> [args]", "THE LIST"));
        cm.registerCommand(new CommandRank("rank", "/<command> [args]", "The rank command!"));
        cm.registerCommand(new CommandEss("essentials", "/<command> [args]", "The essentials command."));
        cm.registerCommand(new CommandMail("mailbox", "/<command> [args]", "The mail command."));
        cm.registerCommand(new CommandAccept("accept", "/<command> [args]", "The accept command."));
        cm.registerCommand(new CommandInvoke("invoke", "/<command> [args]", "The invoke command."));

        cm.registerCommand(new CommandGlobalChat("gl", "/<command> [args]", "The invoke command."));

        cm.registerCommand(new CommandStats("stats", "/<command> [args]", "The stats command."));
        cm.registerCommand(new CommandStop("stop", "/<command> [args]", "The stop command."));
        cm.registerCommand(new CommandRoll("roll", "/<command> [args]", "The roll command."));
        cm.registerCommand(new CommandStuck("stuck", "/<command> [args]", "The stuck command."));
        cm.registerCommand(new CommandRedeem("redeem", "/<command> [args]", "The redeem command."));

        cm.registerCommand(new CommandPl("pinvite", "/<command> [args]", "Will invite a player to a party and create one!"));
        cm.registerCommand(new CommandPAccept("paccept", "/<command> [args]", "Accept a party invitation."));
        cm.registerCommand(new CommandPRemove("premove", "/<command> [args]", "Remove player from party."));
        cm.registerCommand(new CommandPLeave("pleave", "/<command> [args]", "Remove player from party."));
        cm.registerCommand(new CommandPChat("pchat", "/<command> [args]", "Talk in party chat."));
        cm.registerCommand(new CommandModeration("dr", "/<command> [args]", "the dr moderation command."));
        cm.registerCommand(new CommandLogout("logout", "/<command> [args]", "the Logout command."));

        try {
            FileUtils.deleteDirectory(new File("world" + File.separator + "playerdata"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{
        	Bukkit.broadcastMessage(ChatColor.RED + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.YELLOW + "A SCHEDULED RESET WILL TAKE PLACE IN 5 MINUTES");
            Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), ()->{
        	Bukkit.shutdown();
            }, 6000);
        }, 570000);
        // 576000 = 8 hours
        // 5 Minutes = 6000
        // 570000 == 7 hours and 55 minutes.

        Utils.log.info("DungeonRealms Registering Commands() ... FINISHED!");
        this.hasFinishedSetup = true;
        Utils.log.info("DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000l) / START_TIME) + "/s");
    }

    public void onDisable() {
        saveConfig();
        ShopMechanics.deleteAllShops();
        API.logoutAllPlayers();
        mm.stopInvocation();
        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
        Database.mongoClient.close();
        AsyncUtils.pool.shutdown();
    }

}
