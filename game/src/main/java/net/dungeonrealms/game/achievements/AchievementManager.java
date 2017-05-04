package net.dungeonrealms.game.achievements;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.handler.KarmaHandler;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.world.teleportation.WorldRegion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 10/24/2015.
 */
public class AchievementManager implements GenericMechanic, Listener {

    static AchievementManager instance = null;

    public static AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }

    public static HashMap<UUID, String> REGION_TRACKER = new HashMap<>();


    @Override
    public EnumPriority startPriority() {
        return EnumPriority.POPE;
    }

    @Override
    public void startInitialization() {
        /**
         * every 4 seconds to check all players regions and fire proper event if
         * applicable.
         *
         * @since 1.0
         */
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> Bukkit.getOnlinePlayers().forEach(player -> {
            String region = GameAPI.getRegionName(player.getLocation());
            if (REGION_TRACKER.containsKey(player.getUniqueId()))
                if (REGION_TRACKER.get(player.getUniqueId()).equalsIgnoreCase(region))
                    return;
            KarmaHandler.getInstance().tellPlayerRegionInfo(player);
            Bukkit.getServer().getPluginManager().callEvent(new PlayerEnterRegionEvent(player, region));
            REGION_TRACKER.put(player.getUniqueId(), region);
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
    public void handleLogin(UUID uuid) {
        if (Bukkit.getPlayer(uuid) == null) return;
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if(wrapper == null) return;
        if (wrapper.getPetsUnlocked().size() > 0) {
            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.PET_COMPANION);
        }
        if (wrapper.getPetsUnlocked().size() >= 3) {
            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ANIMAL_TAMER);
        }
        int playerBankGems = wrapper.getGems();
        BankMechanics.getInstance().checkBankAchievements(uuid, playerBankGems);


        if (wrapper.getGuildID() != 0) {
            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.GUILD_MEMBER);
            //TODO: Check if they are Officer when type is implemented.
        }
        //TODO: Realm level/tier checks when they are implemented.

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String dev : DungeonRealms.getInstance().getDevelopers()) {
                if (player.getName().equalsIgnoreCase(dev)) {
                    Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.PLAY_WITH_DEV);
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
        WorldRegion region = WorldRegion.getByRegionName(event.getRegion());
        if(region != null)
        	region.giveAchievement(event.getPlayer());
    }
}
