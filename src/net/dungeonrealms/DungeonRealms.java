package net.dungeonrealms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.dungeonrealms.core.Core;
import net.dungeonrealms.game.commands.CommandAccept;
import net.dungeonrealms.game.commands.CommandAdd;
import net.dungeonrealms.game.commands.CommandCheck;
import net.dungeonrealms.game.commands.CommandEss;
import net.dungeonrealms.game.commands.CommandGlobalChat;
import net.dungeonrealms.game.commands.CommandGuild;
import net.dungeonrealms.game.commands.CommandInvoke;
import net.dungeonrealms.game.commands.CommandLag;
import net.dungeonrealms.game.commands.CommandList;
import net.dungeonrealms.game.commands.CommandLogout;
import net.dungeonrealms.game.commands.CommandMail;
import net.dungeonrealms.game.commands.CommandModeration;
import net.dungeonrealms.game.commands.CommandPAccept;
import net.dungeonrealms.game.commands.CommandPChat;
import net.dungeonrealms.game.commands.CommandPLeave;
import net.dungeonrealms.game.commands.CommandPRemove;
import net.dungeonrealms.game.commands.CommandPl;
import net.dungeonrealms.game.commands.CommandPurchase;
import net.dungeonrealms.game.commands.CommandRank;
import net.dungeonrealms.game.commands.CommandRoll;
import net.dungeonrealms.game.commands.CommandSet;
import net.dungeonrealms.game.commands.CommandShopClose;
import net.dungeonrealms.game.commands.CommandSkip;
import net.dungeonrealms.game.commands.CommandSpar;
import net.dungeonrealms.game.commands.CommandSpawn;
import net.dungeonrealms.game.commands.CommandStats;
import net.dungeonrealms.game.commands.CommandStop;
import net.dungeonrealms.game.commands.CommandStuck;
import net.dungeonrealms.game.commands.CommandTell;
import net.dungeonrealms.game.commands.CommandToggle;
import net.dungeonrealms.game.commands.generic.CommandManager;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.guild.Guild;
import net.dungeonrealms.game.handlers.EnergyHandler;
import net.dungeonrealms.game.handlers.HealthHandler;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.handlers.ScoreboardHandler;
import net.dungeonrealms.game.handlers.TutorialIslandHandler;
import net.dungeonrealms.game.listeners.AntiCheatListener;
import net.dungeonrealms.game.listeners.BankListener;
import net.dungeonrealms.game.listeners.BlockListener;
import net.dungeonrealms.game.listeners.DamageListener;
import net.dungeonrealms.game.listeners.EnergyListener;
import net.dungeonrealms.game.listeners.InventoryListener;
import net.dungeonrealms.game.listeners.ItemListener;
import net.dungeonrealms.game.listeners.MainListener;
import net.dungeonrealms.game.listeners.MainListenerInstance;
import net.dungeonrealms.game.listeners.ShopListener;
import net.dungeonrealms.game.mastery.AsyncUtils;
import net.dungeonrealms.game.mastery.RealmManager;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.MechanicManager;
import net.dungeonrealms.game.menus.HearthStone;
import net.dungeonrealms.game.menus.Profile;
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
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.realms.Instance;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spawning.BuffManager;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;

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
    public static String version = "3.0";
    
    // Menus
    
    private static HearthStone hs;
    private static Profile ps;
    
    // End of Menus
    
    // Shard Config
    
    public boolean isInstanceServer = false;
    public String bungeeName = "Lobby";
    public int realmnumber = -1;
    public int realmport = -1;
    public int realmmax = 0;
    public int realmpmax = 0;
    
    // End of Shard Config

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
        
        Utils.log.info("Reading shard config...");
        Ini ini = new Ini();
        try {
			ini.load(new FileReader("shardconfig.ini"));
			isInstanceServer = ini.get("Main", "instanced", Boolean.class);
			bungeeName = ini.get("Bungee", "name", String.class);
			realmnumber = ini.get("RealmInstance", "number", int.class);
			realmport = ini.get("RealmInstance", "port", int.class);
			realmmax = ini.get("RealmInstance", "maxrealms", int.class);
			realmpmax = ini.get("RealmInstance", "maxplayers", int.class);

		} catch (InvalidFileFormatException e1) {
			Utils.log.info("InvalidFileFormat in shard config!");
		} catch (FileNotFoundException e1) {
			Utils.log.info("Shard Config not found!");
		} catch (IOException e1) {
			Utils.log.info("IOException in shard config!");
		}
        Utils.log.info("Done reading shard config!");
        
        hs = new HearthStone();
        ps = new Profile();
        hs.onEnable();
        ps.onEnable();
        
        Database.getInstance().startInitialization();
        DatabaseAPI.getInstance().startInitialization();
        NetworkAPI.getInstance().startInitialization();
        AntiCheat.getInstance().startInitialization();
        //new Spar().startInitialization();

        PluginManager pm = Bukkit.getPluginManager();
        Utils.log.info("DungeonRealms Registering Events() ... STARTING ...");
        
        if(!isInstanceServer)
        {
            pm.registerEvents(new MainListener(), this);
            pm.registerEvents(new DamageListener(), this);
            pm.registerEvents(new ItemListener(), this);
            pm.registerEvents(new InventoryListener(), this);
            pm.registerEvents(new BlockListener(), this);
            pm.registerEvents(new BankListener(), this);
            pm.registerEvents(new EnergyListener(), this);
            pm.registerEvents(new AntiCheatListener(), this);
            pm.registerEvents(new ShopListener(), this);
            pm.registerEvents(new AchievementManager(), this);
        } else {
            pm.registerEvents(new MainListenerInstance(), this);
            pm.registerEvents(new DamageListener(), this);
            pm.registerEvents(new ItemListener(), this);
            pm.registerEvents(new InventoryListener(), this);
            pm.registerEvents(new BlockListener(), this);
            pm.registerEvents(new EnergyListener(), this);
            pm.registerEvents(new AntiCheatListener(), this);
            pm.registerEvents(new AchievementManager(), this);
        }
        pm.registerEvents(Instance.getInstance(), this);
        
        
        //pm.registerEvents(new MainListener(), this);
        //pm.registerEvents(new DamageListener(), this);
        //pm.registerEvents(new ItemListener(), this);
        //pm.registerEvents(new InventoryListener(), this);
        //pm.registerEvents(new BlockListener(), this);
        //pm.registerEvents(new BankListener(), this);
        //pm.registerEvents(new EnergyListener(), this);
        //pm.registerEvents(new AntiCheatListener(), this);
        //pm.registerEvents(new BossListener(), this);
        //pm.registerEvents(new ShopListener(), this);
        //pm.registerEvents(new AchievementManager(), this);
        //pm.registerEvents(Instance.getInstance(), this);
        /*
        In development
        pm.registerEvents(new RiftPortal(), this);
        pm.registerEvents(new Runes(), this);
         */
        Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");

        mm = new MechanicManager();
        if(!isInstanceServer)
        {
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
            //mm.registerMechanic(DungeonManager.getInstance());
            mm.registerMechanic(new Entities());
            mm.registerMechanic(ScoreboardHandler.getInstance());
            //mm.registerMechanic(RealmManager.getInstance());
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
        } else {
            mm.registerMechanic(Core.getInstance());
            mm.registerMechanic(PetUtils.getInstance());
            mm.registerMechanic(CombatLog.getInstance());
            mm.registerMechanic(EnergyHandler.getInstance());
            mm.registerMechanic(Rank.getInstance());
            mm.registerMechanic(DonationEffects.getInstance());
            mm.registerMechanic(HealthHandler.getInstance());
            mm.registerMechanic(KarmaHandler.getInstance());
            mm.registerMechanic(BankMechanics.getInstance());
            mm.registerMechanic(NetworkServer.getInstance());
            mm.registerMechanic(new Entities());
            mm.registerMechanic(ScoreboardHandler.getInstance());
            //mm.registerMechanic(RealmManager.getInstance());
            mm.registerMechanic(new ShopMechanics());
            mm.registerMechanic(Mining.getInstance());
            mm.registerMechanic(Instance.getInstance());
            mm.registerMechanic(AchievementManager.getInstance());
            mm.registerMechanic(new LootManager());
            mm.registerMechanic(Affair.getInstance());
            if(realmnumber >= 0)
            {
            	mm.registerMechanic(RealmManager.getInstance());
            }
        }

        /*
        In development
        mm.registerMechanic(RiftPortal.getInstance());
        mm.registerMechanic(Runes.getInstance());
         */

        mm.loadMechanics();

        CommandManager cm = new CommandManager();

        if(isInstanceServer)
        {
            
            cm.registerCommand(new CommandGuild("guild", "/<command> [args]", "Opens the guild menu!"));
            cm.registerCommand(new CommandLag("lag", "/<command> [args]", "Checks for lag."));
            cm.registerCommand(new CommandSet("set", "/<command> [args]", "SETS THE YEAH."));
            cm.registerCommand(new CommandEss("essentials", "/<command> [args]", "The essentials command."));
            cm.registerCommand(new CommandMail("mailbox", "/<command> [args]", "The mail command."));
            cm.registerCommand(new CommandAccept("accept", "/<command> [args]", "The accept command."));
            cm.registerCommand(new CommandInvoke("invoke", "/<command> [args]", "The invoke command."));
            cm.registerCommand(new CommandStats("stats", "/<command> [args]", "The stats command."));
            cm.registerCommand(new CommandStop("stop", "/<command> [args]", "The stop command."));
            cm.registerCommand(new CommandRoll("roll", "/<command> [args]", "The roll command."));
            cm.registerCommand(new CommandPRemove("premove", "/<command> [args]", "Remove player from party."));
            cm.registerCommand(new CommandPLeave("pleave", "/<command> [args]", "Remove player from party."));
            cm.registerCommand(new CommandPChat("pchat", "/<command> [args]", "Talk in party chat."));
            cm.registerCommand(new CommandModeration("dr", "/<command> [args]", "The dr moderation command."));
            cm.registerCommand(new CommandLogout("logout", "/<command> [args]", "The Logout command."));
            cm.registerCommand(new CommandToggle("toggles", "/<command> [args]", "The Toggle command."));
            cm.registerCommand(new CommandCheck("check", "/<command> [args]", "Check epoch time of item."));
            cm.registerCommand(new CommandTell("tell", "/<command> [args]", "tell a player something."));
            cm.registerCommand(new CommandTell("isay", "/<command> [args]", "Prints message to players in dungeon world from command block."));
        } else {
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
        }
        if(!isInstanceServer)
        {
    		AbstractCommand gotesting = new TestingCommand("gotesting", "/<command> [args]", "This is a test command");
    		gotesting.register();	
        }

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
    	ps.onDisable();
    	hs.onDisable();
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
