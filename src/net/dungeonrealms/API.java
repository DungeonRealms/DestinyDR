package net.dungeonrealms;

import java.rmi.activation.UnknownObjectException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;

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
	public static WorldGuardPlugin getWorldGuard() {
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
	 * @param event
	 * @since 1.0
	 * Checks if player is in a region that denys PVP
	 */
	public static boolean isInSafeRegion(UUID uuid) {
		Player p = Bukkit.getPlayer(uuid);
		ApplicableRegionSet region = getWorldGuard().getRegionManager(p.getWorld())
			.getApplicableRegions(p.getLocation());
		if (region.getFlag(DefaultFlag.PVP) != null) {
			if (region.allows(DefaultFlag.PVP))
			return false;
			else
			return true;
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

}
