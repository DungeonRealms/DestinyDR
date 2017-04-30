package net.dungeonrealms.game.world.loot;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LootManager - Handles loot being spawned across the world.
 * 
 * TODO: Loot Chest particles.
 * 
 * Redone on April 29th, 2017.
 * @author Kneesnap
 */
public class LootManager implements GenericMechanic, Listener {
	
	@Getter private static List<LootSpawner> spawners = new ArrayList<>();
	@Getter private static Map<String, HashMap<ItemStack, Integer>> loot = new HashMap<>();
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.ARCHBISHOPS;
	}
	
	@Override
	public void startInitialization() {
		Utils.log.info("[ChestLoot] - Loading types and spawners...");
		
		//  LOAD LOOT CHOICES  //
		Arrays.stream(new File(GameAPI.getDataFolder() + "/loot/").list())
				.filter(name -> name.endsWith(".loot")).forEach(name -> loadLoot(name));
		
		for (String line : DungeonRealms.getInstance().getConfig().getStringList("loot")) {
			String[] cords = line.split("=")[0].split(",");
			int x,y,z;
			x = Integer.parseInt(cords[0]);
			y = Integer.parseInt(cords[1]);
			z = Integer.parseInt(cords[2]);
			Location loc = new Location(GameAPI.getMainWorld(), x, y, z);
			String lootData = line.split("=")[1].substring(1).split("@")[0];
			
			if (!getLoot().containsKey(lootData)) {
				Bukkit.getLogger().warning("Could not find loot data '" + lootData + "'.");
				continue;
			}
			
			int spawnDelay = 1200 + Integer.parseInt(line.substring(line.lastIndexOf("@") +  1), line.indexOf("#"));
			getSpawners().add(new LootSpawner(loc, spawnDelay, lootData));
		}
		
		Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> 
			getSpawners().stream().filter(s -> !s.isBroken()).forEach(LootSpawner::showParticles), 20L, 20L);
		
		Utils.log.info("[ChestLoot] - Spawns loaded.");
	}
	
	@Override
	public void stopInvocation() {
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerOpenChest(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		
		if (block == null || block.getType() != Material.CHEST)
			return;
		
		Player p = e.getPlayer();
		e.setCancelled(true);
		LootSpawner spawner = getSpawner(block.getLocation());
		if (!GameAPI.isMainWorld(block) || spawner == null) {
			p.sendMessage(ChatColor.GRAY + "The chest is locked.");
			return;
		}
		
		if (!GameAPI.getNearbyMonsters(block.getLocation(), 10).isEmpty()) {
			p.sendMessage(ChatColor.RED + "It is " + ChatColor.BOLD + "NOT" + ChatColor.RESET + ChatColor.RED + " safe to open that right now");
			p.sendMessage(ChatColor.GRAY + "Eliminate the monsters in the area first.");
			return;
		}
		
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			p.playSound(block.getLocation(), Sound.BLOCK_CHEST_OPEN, 1F, 1F);
			p.openInventory(spawner.getInventory());
		} else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			// Drop all items.
			for (ItemStack item : spawner.getInventory().getContents())
				if (item != null)
					block.getLocation().getWorld().dropItemNaturally(block.getLocation(), item);
			spawner.getInventory().clear();
		}
		
		Achievements.getInstance().giveAchievement(e.getPlayer().getUniqueId(), Achievements.EnumAchievements.OPEN_LOOT_CHEST);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLootClose(InventoryCloseEvent event) {
		if (!event.getInventory().getTitle().equals("Loot"))
			return;
		
		for (LootSpawner spawner : getSpawners())
			if (spawner.getInventory().equals(event.getInventory()))
				spawner.attemptBreak((Player) event.getPlayer());
	}
	
	private void loadLoot(String type) {
		File f = new File(GameAPI.getDataFolder() + "/loot/" + type);
		type = type.split(".")[0];
		
		//  READ FILE  //
		List<String> lines = new ArrayList<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String l = "";
			while ((l = reader.readLine()) != null)
				lines.add(l.split(" ")[0]); //Anything past space is ignored.
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HashMap<ItemStack, Integer> lootMap = new HashMap<>();
		for (String line : lines) {
			//amount = a static or ranged value that controls either the item amount or 
			//meta = a static value that represents data such as item tier.
			
			// Generator type + meta.
			String lootType = line.split("%")[0].split(":")[0];
			int meta = 0;
			if (lootType.contains(",")) {
				meta = Integer.parseInt(lootType.split(",")[1]);
				lootType = lootType.split(",")[0];
			}
			
			// Get amount.
			int amount = 1;
			if (line.contains(":")) {
				String amtStr = line.split(":")[1].split("%")[0];
				if (amtStr.contains("-")) {
					String[] split = amtStr.split("-");
					amount = Utils.randInt(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
				} else {
					amount = Integer.parseInt(amtStr);
				}
			}
			
			//  GENERATE LOOT DATA  //
			if (!LootType.isValid(lootType)) {
				Bukkit.getLogger().warning("Unknown loot generator '" + lootType + "'.");
				continue;
			}
			
			int spawnChance = Math.max(1, Integer.parseInt(line.split("%")[1]));
			lootMap.put(LootType.getGenerator(lootType).getLoot(amount, meta), spawnChance);
			
			//In brackets = optional.
			//Format: type[,amt][:minAmt[-maxAmt]]%chance
			//Ex: gems:1-10%50 <- Random gem value between 1 and 10, with a 50% chance of spawning.
		}
		
		getLoot().put(type, lootMap);
	}
	
	/**
	 * Calculates the delay multiplier for loot spawns based on playercount.
	 */
	public static double getDelayMultiplier() {
		return Math.max(0.2D, (150D - (Bukkit.getOnlinePlayers().size() * 1.75D)));
	}

	public static boolean checkLocationForLootSpawner(Location location) {
		return getSpawners().stream().filter(l -> l.getLocation().distanceSquared(location) <= 2).count() > 0;
	}

	public static LootSpawner getSpawner(Location location) {
		return getSpawners().stream().filter(l -> l.getLocation().equals(location)).findAny().get();
	}
}
