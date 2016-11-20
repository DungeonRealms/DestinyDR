package io.vawke.skelframe.bootstrap;

import net.dungeonrealms.common.frontend.command.CommandManager;
import net.dungeonrealms.common.old.game.database.DatabaseAPI;
import net.dungeonrealms.common.old.game.database.DatabaseInstance;
import net.dungeonrealms.old.game.command.*;
import net.dungeonrealms.vgame.Game;
import org.bukkit.plugin.PluginManager;

/**
 * Created by Giovanni on 10-11-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class IOBootstrap
{
    public void perform(String bungeeId)
    {
        Game.getGame().getInstanceLogger().sendMessage("SKELETON BOOTSTRAP FOR: " + bungeeId);

        PluginManager pluginManager = Game.getGame().getServer().getPluginManager();

        DatabaseInstance.getInstance().startInitialization(true);
        DatabaseAPI.getInstance().startInitialization(bungeeId);

        new CommandManager().registerCommand(new CommandLag("lag", "/<command> [args]", "Checks for lag."));

        // OLD DUNGEON REALMS BOOTSTRAP
        /*
        AntiDuplication.getInstance().startInitialization();
        DungeonManager.getInstance().startInitialization();
        TipHandler.getInstance().startInitialization();
        PowerMove.registerPowerMoves();
        ItemGenerator.loadModifiers();
        MechanicManager mechanicManager = new MechanicManager();
        mechanicManager.registerMechanic(PetUtils.getInstance());
        mechanicManager.registerMechanic(Teleportation.getInstance());
        mechanicManager.registerMechanic(CombatLog.getInstance());
        mechanicManager.registerMechanic(EnergyHandler.getInstance());
        mechanicManager.registerMechanic(DonationEffects.getInstance());
        mechanicManager.registerMechanic(HealthHandler.getInstance());
        mechanicManager.registerMechanic(KarmaHandler.getInstance());
        mechanicManager.registerMechanic(BankMechanics.getInstance());
        mechanicManager.registerMechanic(BungeeChannelListener.getInstance());
        mechanicManager.registerMechanic(NetworkClientListener.getInstance());
        mechanicManager.registerMechanic(new ForceField());
        mechanicManager.registerMechanic(CrashDetector.getInstance());
        mechanicManager.registerMechanic(new EntityMechanics());
        mechanicManager.registerMechanic(ScoreboardHandler.getInstance());
        mechanicManager.registerMechanic(new ShopMechanics());
        mechanicManager.registerMechanic(Soundtrack.getInstance());
        mechanicManager.registerMechanic(Mining.getInstance());
        mechanicManager.registerMechanic(RealmInstance.getInstance());
        mechanicManager.registerMechanic(Fishing.getInstance());
        mechanicManager.registerMechanic(SpawningMechanics.getInstance());
        mechanicManager.registerMechanic(AchievementManager.getInstance());
        mechanicManager.registerMechanic(TabMechanics.getInstance());
        mechanicManager.registerMechanic(BuffManager.getInstance());
        mechanicManager.registerMechanic(new LootManager());
        mechanicManager.registerMechanic(PartyMechanics.getInstance());
        mechanicManager.registerMechanic(PatchTools.getInstance());
        mechanicManager.registerMechanic(TutorialIsland.getInstance());

        pluginManager.registerEvents(new MainListener(), Game.getGame());
        pluginManager.registerEvents(new DamageListener(), Game.getGame());
        pluginManager.registerEvents(new ItemListener(), Game.getGame());
        pluginManager.registerEvents(new InventoryListener(), Game.getGame());
        pluginManager.registerEvents(new BlockListener(), Game.getGame());
        pluginManager.registerEvents(new BankListener(), Game.getGame());
        pluginManager.registerEvents(new EnergyListener(), Game.getGame());
        pluginManager.registerEvents(new TitleAPI(), Game.getGame());
        pluginManager.registerEvents(new AntiCheatListener(), Game.getGame());
        pluginManager.registerEvents(TutorialIsland.getInstance(), Game.getGame());
        //TODO: Fix.
        pluginManager.registerEvents(new ShopListener(), Game.getGame());
        pluginManager.registerEvents(new AchievementManager(), Game.getGame());
        new HearthStone().onEnable();
        new Profile().onEnable();
        new TabCompleteCommands().onEnable();
        pluginManager.registerEvents(new TabbedChatListener(), Game.getGame());
        pluginManager.registerEvents(new DungeonListener(), Game.getGame());
        pluginManager.registerEvents(new BossListener(), Game.getGame());
        pluginManager.registerEvents(new RestrictionListener(), Game.getGame());
        pluginManager.registerEvents(new PvPListener(), Game.getGame());
        pluginManager.registerEvents(new PvEListener(), Game.getGame());

        // Commands
        CommandManager cm = new CommandManager();
        cm.registerCommand(new CommandDevDebug("devdebug", "/<command> [args]", "Toggle on and off debug."));

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

        cm.registerCommand(new CommandTestPlayer("testplayer", "/<command> [args]", "Command to skelframe dr soundtrack."));
        cm.registerCommand(new CommandTestDupe("testdupe", "/<command> [args]", "Command skelframe dupe."));
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

        cm.registerCommand(new CommandGlobalChat("gl", "/<command> [args]", "Sends a message to global chat."));
        cm.registerCommand(new CommandLocalChat("l", "/<command> [args]", "Sendsa message to local chat."));

        cm.registerCommand(new CommandAsk("ask", "/<command> [args]", "Ask command", Collections.singletonList("help")));
        cm.registerCommand(new CommandWelcome("welcome", "/<command> [args]", "Welcome command for ecash"));
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

        cm.registerCommand(new CommandTestRank("testrank", "/<command> [args]", "This is a skelframe command."));
        cm.registerCommand(new CommandTestingHall("testhall", "/<command> [args]", "This is a skelframe command.", Collections.singletonList("testinghall")));

        cm.registerCommand(new StarterCommand("givestarter", "/<command> [args]", "Provides a player with the starter kit."));
        cm.registerCommand(new KickAllCommand("kickall", "/<command> [args]", "Kicks all players from the server."));

        //FRIENDS
        cm.registerCommand(new AddCommand("add", "/<command> [args]", "Send Friend request!", Collections.singletonList("friend")));
        cm.registerCommand(new RemoveCommand("unfriend", "/<command> [args]", "Remove friend from list!", Collections.singletonList("rem")));
        cm.registerCommand(new AcceptCommand("accept", "/<command> [args]", "Accept Friend request!", Collections.singletonList("draccept")));
        cm.registerCommand(new AcceptCommand("deny", "/<command> [args]", "Deny Friend request!", Collections.singletonList("drdeny")));

        cm.registerCommand(new CommandCloseShop("closeshop", "/<command> [args]", "Close Shop on all Shards!", Collections.singletonList("shopclose")));

        cm.registerCommand(new FriendsCommand("friend", "/<command> [args]", "Open friend list!", Arrays.asList("buddy", "buddys")));
        cm.registerCommand(new CommandPlayed("played", "/<command>", "Checks your playtime"));
        **/
    }
}
