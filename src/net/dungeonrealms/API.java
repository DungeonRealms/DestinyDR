package net.dungeonrealms;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.rmi.activation.UnknownObjectException;
import java.util.UUID;

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
     * Will check the players region
     * @param uuid
     * @param region
     * @return
     * @since 1.0
     */
    public static boolean isPlayerInRegion(UUID uuid, String region) {
        return API.getWorldGuard().getRegionManager(Bukkit.getPlayer(uuid).getWorld()).getApplicableRegions(Bukkit.getPlayer(uuid).getLocation()).getRegions().contains(region) ? true : false;
    }

}
