package net.dungeonrealms.game.world.loot;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.command.moderation.CommandLootChest;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.loot.LootTable.PossibleLoot;

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
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * LootManager - Handles loot being spawned across the world.
 * 
 * Redone on April 29th, 2017.
 * @author Kneesnap
 */
public class LootManager implements GenericMechanic, Listener {
	
	@Getter private static List<LootSpawner> spawners = new ArrayList<>();
	@Getter private static Map<String, LootTable> loot = new HashMap<>();
	
	@Override
	public EnumPriority startPriority() {
		return EnumPriority.ARCHBISHOPS;
	}
	
	@Override
	public void startInitialization() {
		Utils.log.info("[Loot] - Loading types and spawners...");
		
		//  LOAD LOOT CHOICES  //
		Arrays.stream(new File(GameAPI.getDataFolder() + "/loot/").listFiles())
				.filter(f -> f.getName().endsWith(".loot")).forEach(this::loadLoot);
		
		for (String line : DungeonRealms.getInstance().getConfig().getStringList("loot")) {
			String[] cords = line.split("=")[0].split(",");
			int x,y,z;
			x = Integer.parseInt(cords[0]);
			y = Integer.parseInt(cords[1]);
			z = Integer.parseInt(cords[2]);
			Location loc = new Location(GameAPI.getMainWorld(), x, y, z);
			String lootData = line.split("=")[1].split("@")[0];
			
			if (!getLoot().containsKey(lootData)) {
				Bukkit.getLogger().warning("Could not find loot data '" + lootData + "'.");
				continue;
			}
			
			getSpawners().add(new LootSpawner(loc, Integer.parseInt(line.split("@")[1].split("#")[0]), lootData));
		}
		
		Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
		
		Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> 
			getSpawners().stream().filter(s -> !s.isBroken()).forEach(LootSpawner::showParticles), 20L, 20L);
		
		Utils.log.info("[Loot] - Spawns loaded.");
	}
	
	@Override
	public void stopInvocation() {
		
	}
	
	/**
	 * Register a LootSpawner.
	 * @param s
	 */
	public static void addSpawner(LootSpawner s) {
		getSpawners().add(s);
		updateConfig();
	}
	
	/**
	 * Remove a lootspawner.
	 * @param s
	 */
	public static void removeSpawner(LootSpawner s) {
		CommandLootChest.removeHologram(s.getLocation());
		getSpawners().remove(s);
		updateConfig();
	}
	
	private static void updateConfig() {
		List<String> config = new ArrayList<>();
		for (LootSpawner s : getSpawners())
			config.add(s.toString());
		DungeonRealms.getInstance().getConfig().set("loot", config);
		DungeonRealms.getInstance().saveConfig();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // Ignore the event if it was already cancelled. (Shop or such.)
	public void playerOpenChest(PlayerInteractEvent e) {
		Block block = e.getClickedBlock();
		
		if (block == null || block.getType() != Material.CHEST)
			return;
		
		Player p = e.getPlayer();
		e.setCancelled(true);
		if(Mining.treasureChests.add(p.getLocation()))return;
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
			spawner.attemptBreak(p);
		}
		
		Achievements.giveAchievement(e.getPlayer(), EnumAchievements.OPEN_LOOT_CHEST);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLootClose(InventoryCloseEvent event) {
		if (!event.getInventory().getTitle().equals("Loot"))
			return;
		
		for (LootSpawner spawner : getSpawners())
			if (spawner.getInventory().equals(event.getInventory()))
				spawner.attemptBreak((Player) event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLootChestClick(InventoryClickEvent event) {
		if (!event.getInventory().getTitle().equals("Loot"))
			return;

		if(!(event.getWhoClicked() instanceof Player)) return;

		if(event.isCancelled()) return;

		if(event.getClick() != ClickType.LEFT && event.getClick() != ClickType.SHIFT_LEFT) {
			event.setCancelled(true);
			return;
		}

		if(event.getClick() == ClickType.LEFT) {
			if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) return;
			if (event.getRawSlot() < event.getInventory().getSize()) {
				//click is in chest
				event.setCancelled(true);
			}
		} else if(event.getClick() == ClickType.SHIFT_LEFT) {
			if (event.getRawSlot() > event.getInventory().getSize()) {
				//click is in chest
				event.setCancelled(true);
			}
		} else {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLootChestDrag(InventoryDragEvent event) {
		if (!event.getInventory().getTitle().equals("Loot"))
			return;

		if(!(event.getWhoClicked() instanceof Player)) return;

		if(event.isCancelled()) return;

		event.setCancelled(true);
	}

	private void loadLoot(File f) {
		String type = f.getName().split("\\.")[0];
		
		//  READ FILE  //
		List<String> lines = new ArrayList<>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			String l = "";
			while ((l = reader.readLine()) != null) {
				String s = l.split(" ")[0].split("#")[0]; //These lines are commented out in the config.
				if (s.length() > 0)
					lines.add(s);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		LootTable table = new LootTable();
		
		for (String line : lines) {
			//amount = a static or ranged value that controls either the item amount or 
			//meta = a static value that represents data such as item tier.
			try {
				// Generator type + meta.
				String lootType = line.split("%")[0].split(":")[0];
				int meta = 1;
				if (lootType.contains(",")) {
					meta = Integer.parseInt(lootType.split(",")[1]);
					lootType = lootType.split(",")[0];
				}
				
				// Get amount.
				int minAmt = 0;
				int maxAmt = -1;
				if (line.contains(":")) {
					String amtStr = line.split(":")[1].split("%")[0];
					if (amtStr.contains("-")) {
						String[] split = amtStr.split("-");
						minAmt = Integer.parseInt(split[0]);
						maxAmt = Integer.parseInt(split[1]);
					} else {
						minAmt = Integer.parseInt(amtStr);
					}
				}
				if (maxAmt == -1)
					maxAmt = minAmt;
				
				//  GENERATE LOOT DATA  //
				if (!LootType.isValid(lootType)) {
					Bukkit.getLogger().warning("Unknown loot generator '" + lootType + "'.");
					continue;
				}
				
				double spawnChance = Math.max(1D, Double.parseDouble(line.split("%")[1]) * 10D);
				
				table.addLoot(new PossibleLoot(LootType.getGenerator(lootType), spawnChance, minAmt, maxAmt, meta));
			} catch (Exception e) {
				e.printStackTrace();
				Bukkit.getLogger().warning("Lootable '" + type + "' has an invalid line \"" + line + "\".");
			}
			//In brackets = optional.
			//Format: type[,amt][:minAmt[-maxAmt]]%chance
			//Ex: gems:1-10%50 <- Random gem value between 1 and 10, with a 50% chance of spawning.
		}
		
		getLoot().put(type, table);
	}
	
	/**
	 * Calculates the delay multiplier for loot spawns based on playercount.
	 */
	public static double getDelayMultiplier() {
		return Math.max(0.2D, (150D - (Bukkit.getOnlinePlayers().size() * 1.75D)));
	}

	/**
	 * Is there a lootspawner within 2 blocks of this location?
	 * @param location
	 * @return
	 */
	public static boolean checkLocationForLootSpawner(Location location) {
		return getSpawners().stream().filter(l -> l.getLocation().distanceSquared(location) <= 2).count() > 0;
	}

	/**
	 * Gets the spawner at a given location.
	 * @param location
	 * @return
	 */
	public static LootSpawner getSpawner(Location location) {
		return getSpawners().stream().filter(l -> l.getLocation().equals(location)).findAny().orElse(null);
	}
}
