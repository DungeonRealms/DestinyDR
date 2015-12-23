package net.dungeonrealms;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.core.Core;
import net.dungeonrealms.game.commands.*;
import net.dungeonrealms.game.commands.generic.CommandManager;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.guild.Guild;
import net.dungeonrealms.game.handlers.*;
import net.dungeonrealms.game.listeners.*;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.generic.MechanicManager;
import net.dungeonrealms.game.mongo.Database;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.achievements.AchievementManager;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.network.NetworkServer;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.PetUtils;
import net.dungeonrealms.game.world.items.NamedItems;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.realms.Instance;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spar.Spar;
import net.dungeonrealms.game.world.spawning.BuffManager;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

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
 * Written by Casey Keeling  (Atlas_) <casey@amrillocollege.com>, October 2015 -
 * Previous Developers -
 *  Written by Gay Tux Boy. ("Naughty, Naughty, Naughty")
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
    public static String version = "2.9_dd67e00";

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
        AntiCheat.getInstance().startInitialization();
        new Spar().startInitialization();

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

        mm.registerMechanic(Core.getInstance());
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
        mm.registerMechanic(new NamedItems());
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
        mm.registerMechanic(TutorialIslandHandler.getInstance());

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

        cm.registerCommand(new CommandPl("pinvite", "/<command> [args]", "Will invite a player to a party and create one!"));
        cm.registerCommand(new CommandPAccept("paccept", "/<command> [args]", "Accept a party invitation."));
        cm.registerCommand(new CommandPRemove("premove", "/<command> [args]", "Remove player from party."));
        cm.registerCommand(new CommandPLeave("pleave", "/<command> [args]", "Remove player from party."));
        cm.registerCommand(new CommandPChat("pchat", "/<command> [args]", "Talk in party chat."));
        cm.registerCommand(new CommandModeration("dr", "/<command> [args]", "The dr moderation command."));
        cm.registerCommand(new CommandLogout("logout", "/<command> [args]", "The Logout command."));
        cm.registerCommand(new CommandToggle("toggles", "/<command> [args]", "The Toggle command."));
        cm.registerCommand(new CommandSkip("skip", "/<command> [args]", "Skip the tutorial island."));
        cm.registerCommand(new CommandShopClose("closeshop", "/<command>", "Close Shop on all shards."));
        cm.registerCommand(new CommandPurchase("purchase", "/<command> [args]", "Purchase broadcast command."));

        cm.registerCommand(new CommandCheck("check", "/<command> [args]", "Check epoch time of item."));
        cm.registerCommand(new CommandTell("tell", "/<command> [args]", "tell a player something."));
        cm.registerCommand(new CommandTell("isay", "/<command> [args]", "Prints message to players in dungeon world from command block."));
        cm.registerCommand(new CommandSpar("spar", "/<command> [args]", "The spar command, basically duels recoded."));

        try {
            FileUtils.deleteDirectory(new File("world" + File.separator + "playerdata"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().scheduleAsyncDelayedTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getOnlinePlayers().stream().forEach(player -> BountifulAPI.sendTitle(player, 1, 20 * 3, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 5 MINUTES"));
            Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                DungeonRealms.getInstance().setFinishedSetup(false);
                ShopMechanics.deleteAllShops();
                API.logoutAllPlayers();
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    DungeonRealms.getInstance().mm.stopInvocation();
                    Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
                    AsyncUtils.pool.shutdown();
                    Database.mongoClient.close();
                }, 200);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    Bukkit.getOnlinePlayers().stream().forEach(player -> BountifulAPI.sendTitle(player, 1, 20 * 3, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 1 MINUTE"));
                }, (20 * 60) * 4);

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Bukkit::shutdown, 1200);
            }, 6000);
        }, 288000);
        Utils.log.info("DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000l) / START_TIME) + "/s");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> this.hasFinishedSetup = true, 240L);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            DatabaseAPI.getInstance().PLAYER_TIME.entrySet().stream().forEach(e -> {
                DatabaseAPI.getInstance().PLAYER_TIME.put(e.getKey(), (e.getValue() + 1));
            });
        }, 0, 20);

    }

    public void onDisable() {
        saveConfig();
        Guild.getInstance().saveAllGuilds();
        ShopMechanics.deleteAllShops();
        API.logoutAllPlayers();
        mm.stopInvocation();
        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
        AsyncUtils.pool.shutdown();
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Database.mongoClient::close, 20);
    }

}
