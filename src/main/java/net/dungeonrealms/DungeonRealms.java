package net.dungeonrealms;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.game.commands.*;
import net.dungeonrealms.game.commands.generic.CommandManager;
import net.dungeonrealms.game.commands.guild.CommandGInfo;
import net.dungeonrealms.game.commands.guild.CommandGQuit;
import net.dungeonrealms.game.commands.menualias.CommandMount;
import net.dungeonrealms.game.commands.menualias.CommandPet;
import net.dungeonrealms.game.commands.menualias.CommandProfile;
import net.dungeonrealms.game.commands.menualias.CommandTrail;
import net.dungeonrealms.game.commands.newcommands.*;
import net.dungeonrealms.game.commands.support.*;
import net.dungeonrealms.game.donate.DonationEffects;
import net.dungeonrealms.game.handlers.*;
import net.dungeonrealms.game.listeners.*;
import net.dungeonrealms.game.mastery.RealmManager;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.generic.MechanicManager;
import net.dungeonrealms.game.menus.HearthStone;
import net.dungeonrealms.game.menus.Profile;
import net.dungeonrealms.game.mongo.Database;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.achievements.AchievementManager;
import net.dungeonrealms.game.network.NetworkAPI;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.TabbedChatListener;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.anticheat.AntiCheat;
import net.dungeonrealms.game.world.entities.Entities;
import net.dungeonrealms.game.world.entities.utils.PetUtils;
import net.dungeonrealms.game.world.items.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.party.Affair;
import net.dungeonrealms.game.world.realms.Instance;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spawning.BuffManager;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    private static TabCompleteCommands tcc;

    // Shard Config

    public boolean isInstanceServer = false;
    public String bungeeName = "Lobby";
    public int realmnumber = -1;
    public int realmport = -1;
    public int realmmax = 0;
    public int realmpmax = 0;
    public String shardid = "US-666";
    public boolean isMasterShard = false; // Master shard (US-0) - handles rollout / editable / etc.
    public boolean isSubscriberShard = false; // Subscriber shard - only allow subsribers.
    public boolean isSupportShard = false; // Custom support shard - should we enable support commands?
    public boolean isYouTubeShard = false; // YouTuber shard - only YTers / staff allowed.
    public boolean isBrazilianShard = false; // Brazilian shard - eventually create DR localization, etc.
    public boolean isRoleplayShard = false; // Role playing shard - prompt user its a RP shard.
    public boolean isBetaShard = false; // Beta shard - enable extended capabilities / alert user about bugs.

    // End of Shard Config

    public List<String> getDEVS() {
        return DEVS;
    }

    private List<String> DEVS = Arrays.asList("Proxying", "Atlas__", "iFamasssxD", "APOLLO_IO", "Bradez1571", "EtheralTemplar");

    public boolean hasFinishedSetup() {
        return hasFinishedSetup;
    }

    public void setFinishedSetup(boolean bool) {
        hasFinishedSetup = bool;
    }

    public static long getServerStart() {
        return serverStart;
    }

    private static final long serverStart = System.currentTimeMillis();

    public void onEnable() {
        long START_TIME = System.currentTimeMillis() / 1000L;
        Utils.log.info("DungeonRealms onEnable() ... STARTING UP");
        saveDefaultConfig();

        Utils.log.info("Reading shard config...");
        Ini ini = new Ini();
        try {
            ini.load(new FileReader("shardconfig.ini"));
            // Main
            isInstanceServer = ini.get("Main", "instanced", Boolean.class);
            shardid = ini.get("Main", "shardid", String.class);
            bungeeName = ini.get("Bungee", "name", String.class);

            realmnumber = ini.get("RealmInstance", "number", int.class);
            realmport = ini.get("RealmInstance", "port", int.class);
            realmmax = ini.get("RealmInstance", "maxrealms", int.class);
            realmpmax = ini.get("RealmInstance", "maxplayers", int.class);
            // Shard Settings
            isMasterShard = ini.get("Settings", "master_shard", Boolean.class);
            isSubscriberShard = ini.get("Settings", "support_shard", Boolean.class);
            isSupportShard = ini.get("Settings", "subscriber_shard", Boolean.class);
            isYouTubeShard = ini.get("Settings", "youtube_shard", Boolean.class);
            isBrazilianShard = ini.get("Settings", "brazilian_shard", Boolean.class);
            isRoleplayShard = ini.get("Settings", "roleplay_shard", Boolean.class);
            isBetaShard = ini.get("Settings", "beta_shard", Boolean.class);
        } catch (InvalidFileFormatException e1) {
            Utils.log.info("InvalidFileFormat in shard config!");
        } catch (FileNotFoundException e1) {
            Utils.log.info("Shard Config not found!");
        } catch (IOException e1) {
            Utils.log.info("IOException in shard config!");
        }
        Utils.log.info("Done reading shard config!");

        Database.getInstance().startInitialization();
        DatabaseAPI.getInstance().startInitialization();
        NetworkAPI.getInstance().startInitialization();
        AntiCheat.getInstance().startInitialization();
        ItemGenerator.loadModifiers();

        //new Spar().startInitialization();

        ItemGenerator.loadModifiers();

        mm = new MechanicManager();
        if (!isInstanceServer) {
            mm.registerMechanic(PetUtils.getInstance());
            mm.registerMechanic(Teleportation.getInstance());
            mm.registerMechanic(CombatLog.getInstance());
            mm.registerMechanic(EnergyHandler.getInstance());
            mm.registerMechanic(Rank.getInstance());
            mm.registerMechanic(DonationEffects.getInstance());
            mm.registerMechanic(HealthHandler.getInstance());
            mm.registerMechanic(KarmaHandler.getInstance());
            mm.registerMechanic(BankMechanics.getInstance());
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
            mm.registerMechanic(PetUtils.getInstance());
            mm.registerMechanic(CombatLog.getInstance());
            mm.registerMechanic(EnergyHandler.getInstance());
            mm.registerMechanic(Rank.getInstance());
            mm.registerMechanic(DonationEffects.getInstance());
            mm.registerMechanic(HealthHandler.getInstance());
            mm.registerMechanic(KarmaHandler.getInstance());
            mm.registerMechanic(BankMechanics.getInstance());
            mm.registerMechanic(new Entities());
            mm.registerMechanic(ScoreboardHandler.getInstance());
            //mm.registerMechanic(RealmManager.getInstance());
            mm.registerMechanic(new ShopMechanics());
            mm.registerMechanic(Mining.getInstance());
            mm.registerMechanic(Instance.getInstance());
            mm.registerMechanic(AchievementManager.getInstance());
            mm.registerMechanic(new LootManager());
            mm.registerMechanic(Affair.getInstance());
            if (realmnumber >= 0) {
                mm.registerMechanic(RealmManager.getInstance());
            }
        }

        mm.loadMechanics();

        PluginManager pm = Bukkit.getPluginManager();
        Utils.log.info("DungeonRealms Registering Events() ... STARTING ...");

        if (!isInstanceServer) {
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
            hs = new HearthStone();
            ps = new Profile();
            tcc = new TabCompleteCommands();
            hs.onEnable();
            ps.onEnable();
            tcc.onEnable();
            pm.registerEvents(new TabbedChatListener(), this);
        } else {
            pm.registerEvents(new MainListenerInstance(), this);
            pm.registerEvents(new DamageListener(), this);
            pm.registerEvents(new ItemListener(), this);
            pm.registerEvents(new InventoryListener(), this);
            pm.registerEvents(new BlockListener(), this);
            pm.registerEvents(new EnergyListener(), this);
            pm.registerEvents(new AntiCheatListener(), this);
            pm.registerEvents(new AchievementManager(), this);
            pm.registerEvents(new TabbedChatListener(), this);
        }

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
        Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");

        CommandManager cm = new CommandManager();

        // Commands always registered regardless of server.
        cm.registerCommand(new CommandLag("lag", "/<command> [args]", "Checks for lag."));
        cm.registerCommand(new CommandSet("set", "/<command> [args]", "Development command for modifying account variables."));
        cm.registerCommand(new CommandEss("dr", "/<command> [args]", "The essentials command."));
        cm.registerCommand(new CommandTell("tell", "/<command> [args]", "Send a private message to a player."));
        cm.registerCommand(new CommandISay("isay", "/<command> [args]", "Prints message to players in dungeon world from command block."));
        cm.registerCommand(new CommandModeration("moderation", "/<command> [args]", "Moderation command for Dungeon Realms staff."));

        cm.registerCommand(new CommandPAccept("paccept", "/<command> [args]", "Accept a party invitation."));
        cm.registerCommand(new CommandPRemove("premove", "/<command> [args]", "Remove player from party."));
        cm.registerCommand(new CommandPLeave("pleave", "/<command> [args]", "Remove player from party."));
        cm.registerCommand(new CommandPChat("pchat", "/<command> [args]", "Talk in party chat."));

        cm.registerCommand(new CommandLogout("logout", "/<command> [args]", "The Logout command."));
        cm.registerCommand(new CommandToggle("toggles", "/<command> [args]", "The Toggle command."));
        cm.registerCommand(new CommandRoll("roll", "/<command> [args]", "The roll command."));
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "This command will allow the user to change shards."));

        cm.registerCommand(new CommandCheck("check", "/<command> [args]", "Check epoch time of item."));
        cm.registerCommand(new CommandStats("stat", "/<command> [args]", "The stats command.", Collections.singletonList("stats")));
        cm.registerCommand(new CommandStop("shutdown", "/<command> [args]", "The stop command.", Collections.singletonList("drstop")));
        cm.registerCommand(new CommandAccept("accept", "/<command> [args]", "The accept command."));

        // Commands only registered for an instance server (including the always registered commands).
        if (isInstanceServer) {
            // cm.registerCommand(new CommandGuild("guild", "/<command> [args]", "Opens the guild menu!"));
        }
        // Commands only registered for live servers (including always registered).
        else {

            //GUILD STUFF
            cm.registerCommand(new CommandGInfo("ginfo", "/<command>", "Guild info command."));
            cm.registerCommand(new CommandGQuit("gquit", "/<command>", "Guild quit command.", Arrays.asList("gleave", "gdisband")));

            cm.registerCommand(new CommandSpawn("spawn", "/<command> [args]", "Spawns a mob? idk chase"));
            cm.registerCommand(new CommandAdd("ad", "/<command> [args]", "Adds shit"));
            cm.registerCommand(new CommandList("list", "/<command> [args]", "List online players."));
            cm.registerCommand(new CommandSetRank("setrank", "/<command> [args]", "Manage a players rank!"));
            cm.registerCommand(new CommandMail("mailbox", "/<command> [args]", "The mail command."));
            cm.registerCommand(new CommandReboot("reboot", "/<command>", "Check reboot time."));
            cm.registerCommand(new CommandInvoke("invoke", "/<command> [args]", "The invoke command."));

            cm.registerCommand(new CommandGlobalChat("gl", "/<command> [args]", "Use global chat."));
            cm.registerCommand(new CommandLocalChat("l", "/<command> [args]", "Use local chat."));

            cm.registerCommand(new CommandStuck("stuck", "/<command> [args]", "The stuck command."));

            cm.registerCommand(new CommandSkip("skip", "/<command> [args]", "Skip the tutorial island."));
            cm.registerCommand(new CommandShopClose("closeshop", "/<command>", "Close Shop on all shards."));
            cm.registerCommand(new CommandPurchase("purchase", "/<command> [args]", "Purchase broadcast command."));

            cm.registerCommand(new CommandPl("pinvite", "/<command> [args]", "Will invite a player to a party and create one!"));

            cm.registerCommand(new CommandMount("mount", "/<command> [args]", "The mount command.", Collections.singletonList("mounts")));
            cm.registerCommand(new CommandPet("pet", "/<command> [args]", "The pet command.", Collections.singletonList("pets")));
            cm.registerCommand(new CommandTrail("trail", "/<command> [args]", "The trails command.", Collections.singletonList("trails")));
            cm.registerCommand(new CommandProfile("profile", "/<command> [args]", "The profile command."));

            cm.registerCommand(new TestingCommand("gotesting", "/<command> [args]", "This is a test command."));
            cm.registerCommand(new StarterCommand("givestarter", "/<command> [args]", "Gives a starter kit to someone"));
            cm.registerCommand(new RealmTestCommand("realmtest", "/<command> [args]", "Puts you in your realm"));
            cm.registerCommand(new KickAllCommand("kickall", "/<command> [args]", "Kicks all players from the server"));
            cm.registerCommand(new GlobalBroadcastCommand("glbroadcast", "/<command> [args]", "Broadcast Global message across all shards!"));
        }

        // Commands exclusive to support agents on their special server.
        if (isMasterShard || isSupportShard) {
            cm.registerCommand(new CommandSupport("support", "/<command> [args]", "The main command for accessing all support features and tools."));
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
                ShopMechanics.deleteAllShops(true);
                API.logoutAllPlayers(true);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    DungeonRealms.getInstance().mm.stopInvocation();
                    Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
                    Database.mongoClient.close();
                }, 200L);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> Bukkit.getOnlinePlayers().stream().forEach(player -> BountifulAPI.sendTitle(player, 1, 20 * 3, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 1 MINUTE")), (20 * 60) * 4);

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Bukkit::shutdown, 1200L);
            }, 6000L);
        }, 288000L);
        Utils.log.info("DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000L) / START_TIME) + "/s");
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> this.hasFinishedSetup = true, 240L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> DatabaseAPI.getInstance().PLAYER_TIME.entrySet().stream().forEach(e -> DatabaseAPI.getInstance().PLAYER_TIME.put(e.getKey(), (e.getValue() + 1))), 0L, 20L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> Database.getInstance().backupDatabase(), 18000L, 18000L);
    }

    public void onDisable() {
        API.logoutAllPlayers(false);
        ShopMechanics.deleteAllShops(true);
        ps.onDisable();
        hs.onDisable();
        tcc.onDisable();
        saveConfig();
        mm.stopInvocation();
        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), Database.mongoClient::close, 20L);
    }

}
