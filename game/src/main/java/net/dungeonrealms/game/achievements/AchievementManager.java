package net.dungeonrealms.game.achievements;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerGameStats;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.teleportation.WorldRegion;

import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * AchievementManager - Manages Achievements.
 *
 * Redone on May 7th, 2017.
 * @author Kneesnap
 */
public class AchievementManager implements GenericMechanic, Listener {

    private static HashMap<Player, Location> regionMap = new HashMap<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
    	Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    	// Calls when a player moves into a new region... But why is this in AchivementManager?
    	// TODO: Find a better place for this.
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            if (regionMap.containsKey(player) && GameAPI.getRegionName(regionMap.get(player)).equals(GameAPI.getRegionName(player.getLocation())))
            	return;
            Bukkit.getServer().getPluginManager().callEvent(new PlayerEnterRegionEvent(player, regionMap.get(player), player.getLocation()));
            regionMap.put(player, player.getLocation());
        }), 0, 60L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Checks the players online to see if they have earned an achievement.
     *
     * @param uuid
     * @since 1.0
     */
	public static void handleLogin(Player player) {

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if(wrapper == null)
        	return;

        if (wrapper.getPetsUnlocked().size() > 0)
            Achievements.giveAchievement(player, EnumAchievements.PET_COMPANION);

        if (wrapper.getPetsUnlocked().size() >= 3)
        	Achievements.giveAchievement(player, EnumAchievements.ANIMAL_TAMER);

        if (wrapper.getPlayerGameStats().getStat(PlayerGameStats.StatColumn.PLAYER_KILLS) > 1)
            Achievements.giveAchievement(player, EnumAchievements.MAN_HUNTER_I);
        if (wrapper.getPlayerGameStats().getStat(PlayerGameStats.StatColumn.PLAYER_KILLS) > 3)
            Achievements.giveAchievement(player, EnumAchievements.MAN_HUNTER_II);
        if (wrapper.getPlayerGameStats().getStat(PlayerGameStats.StatColumn.PLAYER_KILLS) > 5)
            Achievements.giveAchievement(player, EnumAchievements.MAN_HUNTER_III);
        if (wrapper.getPlayerGameStats().getStat(PlayerGameStats.StatColumn.PLAYER_KILLS) > 10)
            Achievements.giveAchievement(player, EnumAchievements.MAN_HUNTER_IV);
        if (wrapper.getPlayerGameStats().getStat(PlayerGameStats.StatColumn.PLAYER_KILLS) > 20)
            Achievements.giveAchievement(player, EnumAchievements.MAN_HUNTER_VI);

        BankMechanics.checkBankAchievements(player);

        if (wrapper.isInGuild())
            Achievements.giveAchievement(player, EnumAchievements.GUILD_MEMBER);

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (String dev : DungeonRealms.getInstance().getDevelopers()) {
                if (p.getName().equalsIgnoreCase(dev)) {
                    Achievements.giveAchievement(player, EnumAchievements.PLAY_WITH_DEV);
                    break;
                }
            }
        }
    }

    /**
     * This event is fired every 4 seconds for all players to check their
     * current region and apply achievements if necessary. @see
     * monitorRegionEnters
     *
     * @param event
     * @since 1.9.1
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onRegionEnter(PlayerEnterRegionEvent event) {
        WorldRegion region = WorldRegion.getByRegionName(event.getNewRegion());
        if(region != null)
        	region.giveAchievement(event.getPlayer());
    }
}
