package net.dungeonrealms.mongo.achievements;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.events.PlayerEnterRegionEvent;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
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
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            Bukkit.getOnlinePlayers().stream().forEach(player -> {
                String region = API.getRegionName(player.getLocation());
                if (REGION_TRACKER.containsKey(player.getUniqueId()))
                    if (REGION_TRACKER.get(player.getUniqueId()).equalsIgnoreCase(region))
                        return;
                Bukkit.getServer().getPluginManager().callEvent(new PlayerEnterRegionEvent(player, region));
                REGION_TRACKER.put(player.getUniqueId(), region);
            });
        }, 0, 20 * 4l);
    }

    @Override
    public void stopInvocation() {

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
        Player pl = event.getPlayer();
        String region = event.getRegion();
        switch (region) {
            case "villagesafe":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.VILLAGE_SAFE);
                break;
            case "plainsofcyrenne":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.PLAINS_OF_CYRENE);
                break;
            case "darkoakwild2":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.DARK_OAK_WILD2);
                break;
            case "infrontoftavern":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.INFRONT_OF_TAVERN);
                break;
            case "goblincity":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.GOBLIN_CITY);
                break;
            case "trollcity1":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.TROLL_CITY1);
                break;
            case "crystalpeakt":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CRYSTALPEAKT);
                break;
            case "transitional3":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.TRANSITIONAL_13);
                break;
            case "alsahra":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.ALSAHRA);
                break;
            case "savannahsafezone":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.SAVANNAH_SAFEZONE);
                break;
            case "swampvillage_2":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.SWAMP_VILLAGE2);
                break;
            case "swamp_1":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.SWAMP1);
                break;
            case "crestguard":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CREST_GUARD);
                break;
            case "cstrip6":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CS_TRIP_6);
                break;
            case "underworld":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.UNDER_WORLD);
                break;
            case "Cheifs":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CHIEF);
                break;
            case "Dead_Peaks":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.DEAD_PEAKS);
                break;
            case "Mure":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.MURE);
                break;
            case "Sebrata":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.SEBRATA);
                break;
            case "fireydungeon":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.FIREY_DUNGEON);
                break;
            case "tutorial_island":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.TUTORAL_ISLAND);
                break;
        }
    }
}
