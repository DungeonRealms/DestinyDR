package net.dungeonrealms;

import com.esotericsoftware.minlog.Log;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import lombok.Getter;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.common.game.command.CommandManager;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.DatabaseInstance;
import net.dungeonrealms.common.game.database.data.EnumOperators;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.game.updater.UpdateTask;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.anticheat.AntiDuplication;
import net.dungeonrealms.game.command.*;
import net.dungeonrealms.game.command.dungeon.*;
import net.dungeonrealms.game.command.friend.AcceptCommand;
import net.dungeonrealms.game.command.friend.AddCommand;
import net.dungeonrealms.game.command.friend.FriendsCommand;
import net.dungeonrealms.game.command.friend.RemoveCommand;
import net.dungeonrealms.game.command.guild.*;
import net.dungeonrealms.game.command.menu.*;
import net.dungeonrealms.game.command.moderation.*;
import net.dungeonrealms.game.command.party.*;
import net.dungeonrealms.game.command.punish.*;
import net.dungeonrealms.game.command.support.CommandSupport;
import net.dungeonrealms.game.command.test.*;
import net.dungeonrealms.game.command.toggle.*;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.handler.*;
import net.dungeonrealms.game.listener.MainListener;
import net.dungeonrealms.game.listener.TabCompleteCommands;
import net.dungeonrealms.game.listener.combat.DamageListener;
import net.dungeonrealms.game.listener.combat.PvEListener;
import net.dungeonrealms.game.listener.combat.PvPListener;
import net.dungeonrealms.game.listener.inventory.AntiCheatListener;
import net.dungeonrealms.game.listener.inventory.InventoryListener;
import net.dungeonrealms.game.listener.inventory.ItemListener;
import net.dungeonrealms.game.listener.inventory.ShopListener;
import net.dungeonrealms.game.listener.mechanic.BankListener;
import net.dungeonrealms.game.listener.mechanic.BossListener;
import net.dungeonrealms.game.listener.mechanic.EnergyListener;
import net.dungeonrealms.game.listener.mechanic.RestrictionListener;
import net.dungeonrealms.game.listener.network.BungeeChannelListener;
import net.dungeonrealms.game.listener.network.NetworkClientListener;
import net.dungeonrealms.game.listener.world.BlockListener;
import net.dungeonrealms.game.listener.world.DungeonListener;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.CrashDetector;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.mechanic.TutorialIsland;
import net.dungeonrealms.game.mechanic.generic.MechanicManager;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.chat.TabbedChatListener;
import net.dungeonrealms.game.player.combat.ForceField;
import net.dungeonrealms.game.player.combat.updated.CombatAPI;
import net.dungeonrealms.game.player.menu.HearthStone;
import net.dungeonrealms.game.player.menu.Profile;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.tab.TabMechanics;
import net.dungeonrealms.game.title.TitleAPI;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.powermove.PowerMove;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import net.dungeonrealms.game.world.loot.LootManager;
import net.dungeonrealms.game.world.realms.Realms;
import net.dungeonrealms.game.world.realms.instance.RealmInstance;
import net.dungeonrealms.game.world.shops.ShopMechanics;
import net.dungeonrealms.game.world.spawning.BuffManager;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.dungeonrealms.game.world.teleportation.Teleportation;
import net.dungeonrealms.network.GameClient;
import net.dungeonrealms.network.packet.type.ServerListPacket;
import net.dungeonrealms.tool.PatchTools;
import org.apache.commons.io.FileUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class DungeonRealms extends JavaPlugin {

    private static long SERVER_START_TIME, REBOOT_TIME;

    @Getter
    private static ShardInfo shard;

    @Getter
    private static GameClient client;

    private static DungeonRealms instance = null;
    private static HearthStone hs;
    private static Profile ps;
    private static TabCompleteCommands tcc;

    // Shard Config
    public MechanicManager mm = null;
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

    private volatile boolean acceptPlayers = false;

    public boolean isDrStopAll;

    @Getter
    private Set<UUID> loggingIn = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());
    @Getter
    private List<String> loggingOut = new ArrayList<>();

    public static DungeonRealms getInstance() {
        return instance;
    }

    public static long getServerStart() {
        return SERVER_START_TIME;
    }


    public void onLoad() {
        Utils.log.info("DungeonRealms onLoad() ... STARTING UP");
        instance = this;
    }

    public static int rebooterID;

    public List<String> getDevelopers() {
        return Arrays.asList(Constants.DEVELOPERS);
    }

    public boolean canAcceptPlayers() {
        return acceptPlayers;
    }

    public void setAcceptPlayers(boolean bool) {
        acceptPlayers = bool;
    }

    public void onEnable() {
        AsyncUtils.threadCount = Runtime.getRuntime().availableProcessors();
        AsyncUtils.pool = Executors.newFixedThreadPool(AsyncUtils.threadCount);

        Constants.build();
//        new DupedItemsRemover().startInitialization();
        SERVER_START_TIME = System.currentTimeMillis();

        Utils.log.info("DungeonRealms onEnable() ... STARTING UP");
        saveDefaultConfig();

        // RANDOMIZE REBOOT TIME //
        Random random = new Random();
        long min = Constants.MIN_GAME_TIME + SERVER_START_TIME;
        long max = Constants.MAX_GAME_TIME + SERVER_START_TIME;

        REBOOT_TIME += min + (long) (random.nextDouble() * (max - min));

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
            isSubscriberShard = ini.get("Settings", "subscriber_shard", Boolean.class);
            isSupportShard = ini.get("Settings", "support_shard", Boolean.class);
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

        shard = ShardInfo.getByShardID(shardid);
        BungeeUtils.setPlugin(this);

        DatabaseInstance.getInstance().startInitialization(true);
        DatabaseAPI.getInstance().startInitialization(bungeeName);
        AntiDuplication.getInstance().startInitialization();
        DungeonManager.getInstance().startInitialization();
        TipHandler.getInstance().startInitialization();
        ItemGenerator.loadModifiers();
        PowerMove.registerPowerMoves();
        //new Spar().startInitialization();

        ItemGenerator.loadModifiers();

        Utils.log.info("Connecting to DungeonRealms master server...");
        client = new GameClient();

        try {
            client.connect();
            Log.set(Log.LEVEL_INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }


        mm = new MechanicManager();
        if (!isInstanceServer) {
            mm.registerMechanic(PetUtils.getInstance());
            mm.registerMechanic(Teleportation.getInstance());
            mm.registerMechanic(EnergyHandler.getInstance());
            mm.registerMechanic(DonationEffects.getInstance());
            mm.registerMechanic(HealthHandler.getInstance());
            mm.registerMechanic(KarmaHandler.getInstance());
            mm.registerMechanic(BankMechanics.getInstance());
            mm.registerMechanic(BungeeChannelListener.getInstance());
            mm.registerMechanic(NetworkClientListener.getInstance());
            mm.registerMechanic(new ForceField());
            mm.registerMechanic(CrashDetector.getInstance());
            mm.registerMechanic(new EntityMechanics());
            mm.registerMechanic(ScoreboardHandler.getInstance());
            mm.registerMechanic(new ShopMechanics());
            mm.registerMechanic(Mining.getInstance());
            mm.registerMechanic(RealmInstance.getInstance());
            mm.registerMechanic(Fishing.getInstance());
            mm.registerMechanic(SpawningMechanics.getInstance());
            mm.registerMechanic(AchievementManager.getInstance());
            mm.registerMechanic(TabMechanics.getInstance());
            mm.registerMechanic(BuffManager.getInstance());
            mm.registerMechanic(new LootManager());
            mm.registerMechanic(Affair.getInstance());
            mm.registerMechanic(PatchTools.getInstance());
            mm.registerMechanic(TutorialIsland.getInstance());
        } else {
            mm.registerMechanic(PetUtils.getInstance());
            mm.registerMechanic(EnergyHandler.getInstance());
            mm.registerMechanic(DonationEffects.getInstance());
            mm.registerMechanic(HealthHandler.getInstance());
            mm.registerMechanic(KarmaHandler.getInstance());
            mm.registerMechanic(BankMechanics.getInstance());
            mm.registerMechanic(new EntityMechanics());
            mm.registerMechanic(BungeeChannelListener.getInstance());
            mm.registerMechanic(NetworkClientListener.getInstance());
            mm.registerMechanic(ScoreboardHandler.getInstance());
            mm.registerMechanic(new ShopMechanics());
            mm.registerMechanic(PatchTools.getInstance());
            mm.registerMechanic(Mining.getInstance());
            mm.registerMechanic(RealmInstance.getInstance());
            mm.registerMechanic(AchievementManager.getInstance());
            mm.registerMechanic(new LootManager());
            mm.registerMechanic(Affair.getInstance());


            if (realmnumber >= 0) mm.registerMechanic(Realms.getInstance());
        }

        mm.loadMechanics();

        // START UPDATER TASK //
        new UpdateTask(this);

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
            pm.registerEvents(new TitleAPI(), this);
            pm.registerEvents(new AntiCheatListener(), this);
            pm.registerEvents(TutorialIsland.getInstance(), this);
            //TODO: Fix.
            pm.registerEvents(new ShopListener(), this);
            pm.registerEvents(new AchievementManager(), this);
            hs = new HearthStone();
            ps = new Profile();
            tcc = new TabCompleteCommands();
            hs.onEnable();
            ps.onEnable();
            tcc.onEnable();
            pm.registerEvents(new TabbedChatListener(), this);
            pm.registerEvents(new DungeonListener(), this);
            pm.registerEvents(new BossListener(), this);
            pm.registerEvents(new RestrictionListener(), this);
            pm.registerEvents(new PvPListener(), this);
            pm.registerEvents(new PvEListener(), this);
        } else {
            pm.registerEvents(new DamageListener(), this);
            pm.registerEvents(new ItemListener(), this);
            pm.registerEvents(new InventoryListener(), this);
            pm.registerEvents(new BlockListener(), this);
            pm.registerEvents(new EnergyListener(), this);
            pm.registerEvents(new AntiCheatListener(), this);
            pm.registerEvents(new AchievementManager(), this);
            pm.registerEvents(new TabbedChatListener(), this);
            pm.registerEvents(new RestrictionListener(), this);
            pm.registerEvents(new DungeonListener(), this);
            pm.registerEvents(new BossListener(), this);
            pm.registerEvents(new PvPListener(), this);
            pm.registerEvents(new PvEListener(), this);
        }


        Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");

        CommandManager cm = new CommandManager();

        // Commands always registered regardless of server.
        cm.registerCommand(new CommandDevDebug("devdebug", "/<command> [args]", "Toggle on and off debug."));

        cm.registerCommand(new CommandInvsee("invsee", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Collections.singletonList("mis")));
        cm.registerCommand(new CommandBanksee("banksee", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Collections.singletonList("mbs")));
        cm.registerCommand(new CommandArmorsee("armorsee", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Collections.singletonList("mas")));
        cm.registerCommand(new CommandGemsee("gemsee", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Collections.singletonList("mgs")));
        cm.registerCommand(new CommandBinsee("binsee", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Collections.singletonList("mbns")));

        cm.registerCommand(new CommandLag("lag", "/<command> [args]", "Checks for lag."));
        cm.registerCommand(new CommandSet("set", "/<command> [args]", "Development command for modifying account variables."));
        cm.registerCommand(new CommandEss("dr", "/<command> [args]", "Developer command with the core essentials."));
        cm.registerCommand(new CommandVote("vote", "/<command> [args]", "Gives the link to vote for rewards."));
        cm.registerCommand(new CommandInterface("interface", "/<command> [args]", "Development command for accessing interfaces."));
        cm.registerCommand(new CommandTell("tell", "/<command> [args]", "Send a private message to a player."));
        cm.registerCommand(new CommandISay("isay", "/<command> [args]", "Prints message to players in dungeon world from command block."));
        cm.registerCommand(new CommandModeration("moderation", "/<command> [args]", "Moderation command for Dungeon Realms staff.", Collections.singletonList("mod")));
        cm.registerCommand(new CommandStaffChat("staffchat", "/<command> [args]", "Send a message to the staff chat.", Arrays.asList("sc", "s")));
        cm.registerCommand(new CommandBroadcast("broadcast", "/<command> [args]", "Send a formatted broadcast to all shards..", Collections.singletonList("sayall")));
        cm.registerCommand(new CommandGm("gm", "/<command> [args]", "Displays the Game Master toggles."));
        cm.registerCommand(new CommandPlayerFix("playerfix", "/<command> [args]", "Sets a player's state to offline so he can login.", Collections.singletonList("pfix")));
        cm.registerCommand(new CommandPlayerRetention("pretention", "/<command> [args]", "Player retention command."));
        cm.registerCommand(new CommandSudo("sudo", "/<command> [args]", "Sudo command."));
        cm.registerCommand(new CommandSudoChat("sudochat", "/<command> [args]", "Sudo Chat command."));

        cm.registerCommand(new CommandPAccept("paccept", "/<command> [args]", "Accept a party invitation."));
        cm.registerCommand(new CommandPRemove("premove", "/<command> [args]", "Remove player from party.", Collections.singletonList("pkick")));
        cm.registerCommand(new CommandPLeave("pleave", "/<command> [args]", "Remove player from party.", Collections.singletonList("pquit")));
        cm.registerCommand(new CommandPChat("pchat", "/<command> [args]", "Talk in party chat.", Collections.singletonList("p")));
        cm.registerCommand(new CommandPl("pinvite", "/<command> [args]", "Will invite a player to a party, creating one if it doesn't exist."));
        cm.registerCommand(new CommandPDecline("pdecline", "/<command> [args]", "Decline a party invitation."));

        cm.registerCommand(new CommandTestDupe("testdupe", "/<command> [args]", "Command test dupe."));
        cm.registerCommand(new CommandAlbranir("albranir", "/<command> [args]", "Command to spawn albranir."));
        cm.registerCommand(new CommandClearChat("clearchat", "/<command> [args]", "Command clear chat."));


        cm.registerCommand(new CommandLogout("logout", "/<command> [args]", "Safely logout of Dungeon Realms."));
        cm.registerCommand(new CommandRoll("roll", "/<command> [args]", "Rolls a random number between 1 and the supplied argument."));
        cm.registerCommand(new CommandShard("shard", "/<command> [args]", "This command will allow the user to change shards.", Collections.singletonList("connect")));

        cm.registerCommand(new CommandToggle("toggles", "/<command> [args]", "View and manage your profile toggles.", Collections.singletonList("toggle")));
        cm.registerCommand(new CommandToggleDebug("toggledebug", "/<command> [args]", "Toggles displaying combat debug messages.", Collections.singletonList("debug")));
        cm.registerCommand(new CommandToggleChaos("togglechaos", "/<command> [args]", "Toggles killing blows on lawful players (anti-chaotic)."));
        cm.registerCommand(new CommandToggleGlobalChat("toggleglobalchat", "/<command> [args]", "Toggles talking only in global chat."));
        cm.registerCommand(new CommandTogglePvp("togglepvp", "/<command> [args]", "Toggles all outgoing PvP damage (anti-neutral)."));
        cm.registerCommand(new CommandToggleSoundtrack("togglesoundtrack", "/<command> [args]", "Toggles the DungeonRealms soundtrack."));
        cm.registerCommand(new CommandToggleTells("toggletells", "/<command> [args]", "Toggles receiving NON-BUD /tell.", Collections.singletonList("dnd")));
        cm.registerCommand(new CommandToggleTrade("toggletrade", "/<command> [args]", "Toggles trading requests."));
        cm.registerCommand(new CommandToggleTradeChat("toggletradechat", "/<command> [args]", "Toggles receiving <T>rade chat."));
        cm.registerCommand(new CommandToggleDuel("toggleduel", "/<command> [args]", "Toggles dueling requests."));
        cm.registerCommand(new CommandToggleTips("toggletips", "/<command> [args]", "Toggles tip messages."));

        cm.registerCommand(new CommandCheck("check", "/<command> [args]", "Checks the identity of a Dungeon Realms signed item."));
        cm.registerCommand(new CommandStats("stat", "/<command> [args]", "Allows you to view and manage your stat points.", Collections.singletonList("stats")));
        cm.registerCommand(new CommandStop("shutdown", "/<command> [args]", "This will stop Dungeon Realms safely following safe shutdown procedures.", Collections.singletonList("drstop")));

        cm.registerCommand(new DungeonSpawn("dspawn", "/<command> [args]", "Spawn dungeon monsters."));
        cm.registerCommand(new CommandMonSpawn("monspawn", "/<command> [args]", "Spawn monsters"));
        cm.registerCommand(new ReplaceNear("drreplacenear", "/<command> [args]", "Replaces nearby blocks"));
        cm.registerCommand(new BossTeleport("bosstp", "/<command> [args]", "Dungeon Boss Teleporation"));
        cm.registerCommand(new BossSpawn("bspawn", "/<command> [args]", "Spawn a dungeon boss."));
        cm.registerCommand(new CommandTips("tips", "/<command>", "Tips command"));
        cm.registerCommand(new DungeonJoin("djoin", "/<command>", "Dungeon Join command"));
        cm.registerCommand(new DRLightning("drlightning", "/<command>", "Spawns lightning at an area"));
        cm.registerCommand(new DebuffCrystal("debuffcrystal", "/<command>", "Spawns a debuff crystal"));
        cm.registerCommand(new CommandMessage("message", "/<command>", "Messages a player", Arrays.asList("msg", "tell", "t", "whisper", "w", "m")));
        cm.registerCommand(new CommandReply("reply", "/<command>", "Messages a player", Collections.singletonList("r")));
        cm.registerCommand(new CommandPlayed("played", "/<command>", "Checks your playtime"));
        // Commands only registered for an instance server (including the always registered commands).
        if (isInstanceServer) {
            // cm.registerCommand(new CommandGuild("guild", "/<command> [args]", "Opens the guild menus!"));
        }
        // Commands only registered for live servers (including always registered).
        else {

            //GUILD STUFF
            cm.registerCommand(new CommandGInfo("ginfo", "/<command>", "Guild info command."));
            cm.registerCommand(new CommandG("g", "/<command> [msg]", "Guild chat command."));
            cm.registerCommand(new CommandGQuit("gquit", "/<command>", "Guild quit command.", Arrays.asList("gleave", "gdisband")));
            cm.registerCommand(new CommandGAccept("gaccept", "/<command>", "Guild accept invitation command."));
            cm.registerCommand(new CommandGKick("gkick", "/<command> [args]", "Guild kick command."));
            cm.registerCommand(new CommandGInvite("ginvite", "/<command> [args]", "Guild invitation command."));
            cm.registerCommand(new CommandGPromote("gpromote", "/<command> [args]", "Guild promote command."));
            cm.registerCommand(new CommandGDemote("gdemote", "/<command> [args]", "Guild demote command."));
            cm.registerCommand(new CommandGMotd("gmotd", "/<command> [args]", "Guild motd command."));
            cm.registerCommand(new CommandGDeny("gdecline", "/<command>", "Guild decline invitation command.", Collections.singletonList("gdeny")));

            cm.registerCommand(new CommandSpawn("spawn", "/<command> [args]", "This will teleport a Game Master to their spawn point."));
            cm.registerCommand(new CommandTeleport("teleport", "/<command> [args]", "This will allow a Gamer Master to teleport across the lands of Andalucia."));
            cm.registerCommand(new CommandAdd("ad", "/<command> [args]", "This will spawn a Dungeon Realms item.", Collections.singletonList("ad")));
            cm.registerCommand(new CommandList("list", "/<command> [args]", "Displays a list of online players."));
            cm.registerCommand(new CommandSetRank("setrank", "/<command> [args]", "Sets the rank of a player."));
            cm.registerCommand(new CommandArmorSee("armorsee", "/<command> [args]", "Shows the armor of a player or entity."));
            cm.registerCommand(new CommandWhois("whois", "/<command> [args]", "Get which shard a player is playing on if any."));
            cm.registerCommand(new CommandMail("mailbox", "/<command> [args]", "Manage your received mail and send your own mail."));
            cm.registerCommand(new CommandReboot("reboot", "/<command>", "Displays the time until the shard will next reboot."));
            cm.registerCommand(new CommandInvoke("invoke", "/<command> [args]", "The invoke command."));
            cm.registerCommand(new CommandHead("head", "/<command> [args]", "Spawn a player's Minecraft head."));
            cm.registerCommand(new CommandStore("drstore", "/<command> [args]", "This command will issue store items to users."));

            cm.registerCommand(new CommandGlobalChat("gl", "/<command> [args]", "Sends a message to global chat."));
            cm.registerCommand(new CommandLocalChat("l", "/<command> [args]", "Sendsa message to local chat."));

            cm.registerCommand(new CommandAsk("ask", "/<command> [args]", "Ask command", Collections.singletonList("help")));
            //cm.registerCommand(new CommandWelcome("welcome", "/<command> [args]", "Welcome command for ecash"));
            cm.registerCommand(new CommandAnswer("answer", "/<command> [args]", "Answer command"));
            cm.registerCommand(new CommandStuck("stuck", "/<command> [args]", "Will help remove you if you're stuck in a block."));
            cm.registerCommand(new CommandSuicide("suicide", "/<command>", "Kills your player.", Collections.singletonList("drsuicide")));

            cm.registerCommand(new CommandRealm("realm", "/<command> [args]", "Realm command"));
            cm.registerCommand(new CommandResetRealm("resetrealm", "/<command>", "Realm reset command"));
            cm.registerCommand(new CommandRealmFix("realmfix", "/<command> [args]", "Realm fix command"));
            cm.registerCommand(new CommandRealmWipe("realmwipe", "/<command> [args]", "Realm wipe command"));

            cm.registerCommand(new CommandBan("ban", "/ban <player> <duration | eg. 15m / 5d> [reason ...]", "Ban command", "drban"));
            cm.registerCommand(new CommandUnban("unban", "/<command> [args]", "Unban command", "drunban", "drpardon"));
            cm.registerCommand(new CommandMute("mute", "/mute <player> <duration | eg. 15m / 5d> [reason ...]", "Mute command", "drmute"));
            cm.registerCommand(new CommandUnmute("unmute", "/<command> [args]", "Unmute command", "drunmute"));
            cm.registerCommand(new CommandJail("jail", "/<command> [args]", "Jail command"));
            cm.registerCommand(new CommandUnjail("unjail", "/<command> [args]", "Unjail command"));

            cm.registerCommand(new CommandSkip("skip", "/<command> [args]", "Skips the tutorial island."));
            cm.registerCommand(new CommandPurchase("purchase", "/<command> [args]", "Will announce a purchase messages."));

            cm.registerCommand(new CommandMount("mount", "/<command> [args]", "Opens the player mounts menu.", Collections.singletonList("mounts")));
            cm.registerCommand(new CommandPet("pet", "/<command> [args]", "Opens the player pets menu.", Collections.singletonList("pets")));
            cm.registerCommand(new CommandTrail("trail", "/<command> [args]", "Opens the player trails menu.", Arrays.asList("trails", "effect", "effects")));
            cm.registerCommand(new CommandAchievements("achievements", "/<command> [args]", "Opens the player achievements menu.", Collections.singletonList("achievement")));
            cm.registerCommand(new CommandProfile("profile", "/<command> [args]", "Opens the player profile menu."));
            cm.registerCommand(new CommandEcash("ecash", "/<command> [args]", "Opens the E-Cash vendor menu.", Arrays.asList("shop", "buy")));
            cm.registerCommand(new CommandPatchNotes("patchnotes", "/<command>", "Shows patch for current build", Collections.singletonList("patch")));

            cm.registerCommand(new CommandTestRank("testrank", "/<command> [args]", "This is a test command."));
            cm.registerCommand(new CommandTestingHall("testhall", "/<command> [args]", "This is a test command.", Collections.singletonList("testinghall")));

            cm.registerCommand(new StarterCommand("givestarter", "/<command> [args]", "Provides a player with the starter kit."));
            cm.registerCommand(new KickAllCommand("kickall", "/<command> [args]", "Kicks all players from the server."));

            //FRIENDS
            cm.registerCommand(new AddCommand("add", "/<command> [args]", "Send Friend request!", Collections.singletonList("friend")));
            cm.registerCommand(new RemoveCommand("unfriend", "/<command> [args]", "Remove friend from list!", Collections.singletonList("rem")));
            cm.registerCommand(new AcceptCommand("accept", "/<command> [args]", "Accept Friend request!", Collections.singletonList("draccept")));
            cm.registerCommand(new AcceptCommand("deny", "/<command> [args]", "Deny Friend request!", Collections.singletonList("drdeny")));

            cm.registerCommand(new FriendsCommand("friends", "/<command> [args]", "Open friends list!", Arrays.asList("buddy", "buddys")));
            cm.registerCommand(new CommandPlayed("played", "/<command>", "Checks your playtime"));
        }

        CombatAPI.getInstance().start();

        // Commands exclusive to support agents on their special server.
        if (isMasterShard || isSupportShard) {
            cm.registerCommand(new CommandSupport("support", "/<command> [args]", "The main command for accessing all support features and tools."));
        }

        try {
            FileUtils.deleteDirectory(new File("world" + File.separator + "playerdata"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getServer().setWhitelist(false);

        rebooterID = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            if (System.currentTimeMillis() >= (REBOOT_TIME - 300000L)) {
                scheduleRestartTask();
                Bukkit.getScheduler().cancelTask(rebooterID);
            }
        }, 0, 100);

        // FIX PLAYERS //
        UpdateResult playerFixResult = DatabaseInstance.playerData.updateMany(Filters.eq("info.current", shard.getPseudoName()),
                new Document(EnumOperators.$SET.getUO(), new Document("info.isPlaying", false)));

        if (playerFixResult.wasAcknowledged())
            Constants.log.info("Set " + playerFixResult.getModifiedCount() + " players' " +
                    "statuses to offline from " +
                    "shard " + shard);
        else Constants.log.info("Operation failed: database error.");

        Utils.log.info("DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000L) / SERVER_START_TIME) + "/s");

        try {
            Constants.log.info("Successfully discovered process's pid - " + Utils.getPid());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            Constants.log.info("Server now accepting players.");

            this.acceptPlayers = true;
            Bukkit.getServer().setWhitelist(false);
        }, 240L);


        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                GameAPI.GAMEPLAYERS.values().forEach(gp -> gp.getPlayerStatistics().setTimePlayed(gp.getPlayerStatistics().getTimePlayed() + 1));
            }
        }, 0L, 1000);

        // SEND SERVER INFO TO MASTER SERVER REPEATEDLY //
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ServerListPacket packet = new ServerListPacket();

                final Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);

                packet.target = shard;
                packet.tokens = new PlayerToken[onlinePlayers.length];


                for (int i = 0; i < onlinePlayers.length; i++) {
                    Player player = onlinePlayers[i];
                    packet.tokens[i] = new PlayerToken(player.getUniqueId().toString(), player.getName());
                }

                getClient().sendTCP(packet);
            }
        }, 0L, 3000);

        // run backup every ten minutes
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, GameAPI::backupDatabase, 0L, 12000L);
    }

    public long getRebootTime() {
        return REBOOT_TIME;
    }

    private void scheduleRestartTask() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> Bukkit.getOnlinePlayers().forEach(player -> TitleAPI.sendTitle(player, 1, 60, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 5 MINUTES")));

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                setAcceptPlayers(false);
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(),
                        () -> Bukkit.getOnlinePlayers().forEach(player -> TitleAPI.sendTitle(player, 1, 60, 1, "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "WARNING: " + ChatColor.RED + "A SCHEDULED  " + ChatColor.BOLD + "REBOOT" + ChatColor.RED + " WILL TAKE PLACE IN 1 MINUTE")));
            }
        }, 240000L);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), GameAPI::stopGame);
            }
        }, 300000L);
    }

    public void onDisable() {
        ps.onDisable();
        hs.onDisable();
        tcc.onDisable();
        if (!mm.isShutdown())
            mm.stopInvocation();

        DatabaseAPI.getInstance().stopInvocation();

        Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
    }

}
