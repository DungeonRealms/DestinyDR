package net.dungeonrealms;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.rmi.activation.UnknownObjectException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Nick on 9/17/2015.
 */
public class API {

	/**
	 * Gets the WorldGuard plugin.
	 *
	 * @return
	 * @since 1.0
	 */
	private static WorldGuardPlugin getWorldGuard() {
		Plugin plugin = DungeonRealms.getInstance().getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			try {
			throw new UnknownObjectException("getWorldGuard() of API.class is RETURNING NULL!");
			} catch (UnknownObjectException e) {
			e.printStackTrace();
			}
		}
		return (WorldGuardPlugin) plugin;
	}
	/**
	 * @param uuid
	 * @since 1.0
	 * Checks if player is in a region that denys PVP
	 */
	public static boolean isInSafeRegion(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		ApplicableRegionSet region = getWorldGuard().getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation());
		if (region.getFlag(DefaultFlag.PVP) != null) {
			return !region.allows(DefaultFlag.PVP);
		} else {
			return false;
		}
	}

	/**
	 * Will check the players region
	 * 
	 * @param uuid
	 * @param region
	 * @return
	 * @since 1.0
	 */
	public static boolean isPlayerInRegion(UUID uuid, String region) {
		return getWorldGuard().getRegionManager(Bukkit.getPlayer(uuid).getWorld())
			.getApplicableRegions(Bukkit.getPlayer(uuid).getLocation()).getRegions().contains(region);
	}


	/**
	 * Gets the a list of nearby players from a location within a given radius
	 * @param location
	 * @param radius
	 * @since 1.0
	 */
	public static List<Player> getNearbyPlayers(Location location, int radius) {
		return location.getWorld().getPlayers().stream().filter(player -> location.distance(player.getLocation()) <= radius).collect(Collectors.toList());
	}

}
