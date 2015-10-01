package net.dungeonrealms;

import net.dungeonrealms.banks.BankMechanics;
import net.dungeonrealms.combat.CombatLog;
import net.dungeonrealms.commands.*;
import net.dungeonrealms.energy.EnergyHandler;
import net.dungeonrealms.entities.Entities;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.items.DRBow;
import net.dungeonrealms.items.ItemRegistry;
import net.dungeonrealms.items.enchanting.EnchantmentAPI;
import net.dungeonrealms.listeners.*;
import net.dungeonrealms.mastery.FTPUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.WebAPI;
import net.dungeonrealms.mongo.Database;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.party.PartyMechanics;
import net.dungeonrealms.rank.Rank;
import net.dungeonrealms.rank.Subscription;
import net.dungeonrealms.shops.ShopMechanics;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.dungeonrealms.teleportation.Teleportation;
import net.minecraft.server.v1_8_R3.Item;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by Nick on 9/17/2015.
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
		pm.registerEvents(new EnergyListener(), this);
		Utils.log.info("DungeonRealms Registering Events() ... FINISHED!");

		WebAPI.fetchPrerequisites();

		PetUtils.getInstance().startInitialization();
		Teleportation.getInstance().startInitialization();
		CombatLog.getInstance().startInitialization();
		PartyMechanics.getInstance().startInitialization();
		EnergyHandler.getInstance().startInitialization();
		EnchantmentAPI.getInstance().startInitialization();
		Subscription.getInstance().startInitialization();
		Rank.getInstance().startInitialization();

		Utils.log.info("DungeonRealms Registering Monsters() ... STARTING ...");
		Entities.getInstance().startInitialization();
		Utils.log.info("DungeonRealms Registering Monsters() ... FINISHED!");

		Utils.log.info("DungeonRealms Registering Commands() ... STARTING ...");
		// If the command doesn't return true; the command will print in chat.
		getCommand("spawn").setExecutor(new CommandSpawn());
		getCommand("add").setExecutor(new CommandAdd());
		getCommand("analyze").setExecutor(new CommandAnalyze());
		getCommand("lag").setExecutor(new CommandLag());
		getCommand("party").setExecutor(new CommandParty());
		getCommand("set").setExecutor(new CommandSet());
		getCommand("list").setExecutor(new CommandList());
		getCommand("profile").setExecutor(new CommandProfile());
		getCommand("rank").setExecutor(new CommandRank());
		Utils.log.info("DungeonRealms Registering Commands() ... FINISHED!");
		Utils.log.info("DungeonRealms Registering FTP() ... STARTING ...");
		FTPUtils.startInitialization();
		Utils.log.info("DungeonRealms Finished Registering FTP() ... FINISHED!");

		Item itemBow = new DRBow();
		ItemRegistry itemRegistry = new ItemRegistry();
		itemRegistry.register("minecraft:bow", itemBow);
		SpawningMechanics.loadSpawners();
		Utils.log.info(
			"DungeonRealms STARTUP FINISHED in ... " + ((System.currentTimeMillis() / 1000l) / START_TIME) + "/s");

	}

	public void onDisable() {
		Collection<? extends Player> list = Bukkit.getOnlinePlayers();
		ShopMechanics.deleteAllShops();
		for (int i = 0; i < list.size(); i++) {
			Player current = (Player) list.toArray()[i];
			BankMechanics.handleLogout(current.getUniqueId());
			Utils.log.info(current.getName() + " has been logged out.");
			current.kickPlayer("Server Restarting");
		}
		Utils.log.info("DungeonRealms onDisable() ... SHUTTING DOWN");
		Database.mongoClient.close();
	}

}
