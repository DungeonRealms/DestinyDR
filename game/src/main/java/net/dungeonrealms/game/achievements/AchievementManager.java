package net.dungeonrealms.game.achievements;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.database.DatabaseAPI;
import net.dungeonrealms.game.database.type.EnumData;
import net.dungeonrealms.game.events.PlayerEnterRegionEvent;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.handlers.KarmaHandler;
import net.dungeonrealms.game.mechanics.generic.EnumPriority;
import net.dungeonrealms.game.mechanics.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * Checks the players online to see if they have earned my achievement.
     *
     * @param uuid
     * @since 1.0
     */
    public void handleLogin(UUID uuid) {
        if (Bukkit.getPlayer(uuid) == null) return;
        List<String> playerPets = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.PETS, uuid);
        if (playerPets.size() > 0) {
            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.PET_COMPANION);
        }
        if (playerPets.size() >= 3) {
            Achievements.getInstance().giveAchievement(uuid, Achievements.EnumAchievements.ANIMAL_TAMER);
        }
        int playerBankGems = (int) DatabaseAPI.getInstance().getData(EnumData.GEMS, uuid);
        BankMechanics.getInstance().checkBankAchievements(uuid, playerBankGems);
        if (!GuildDatabaseAPI.get().isGuildNull(uuid)) {
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
        Player pl = event.getPlayer();
        String region = event.getRegion().toLowerCase();
        switch (region) {
            case "villagesafe":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.HARRISONS_FIELD);
                break;
            case "plainsofcyrenne":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.PLAINS_OF_CYRENE);
                break;
            case "darkoakwild2":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.DARKOAK);
                break;
            case "infrontoftavern":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.JAGGED_ROCKS);
                break;
            case "goblincity":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.GOBLIN_CITY);
                break;
            case "trollcity1":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.TROLLINGOR);
                break;
            case "crystalpeakt":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CRYSTALPEAK_TOWER);
                break;
            case "transitional3":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.HELMCHEN);
                break;
            case "alsahra":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.AL_SAHRA);
                break;
            case "savannahsafezone":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.TRIPOLI);
                break;
            case "swampvillage_2":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.DREADWOOD);
                break;
            case "swamp_1":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.GLOOMY_HOLLOWS);
                break;
            case "crestguard":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CREST_GUARD);
                break;
            case "cstrip6":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.FROZEN_NORTH);
                break;
            case "underworld":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.UNDER_WORLD);
                break;
            case "cheifs":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CHIEF);
                break;
            case "deadpeaks":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.DEAD_PEAKS);
                break;
            case "mure":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.MURE);
                break;
            case "sebrata":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.SEBRATA);
                break;
            case "fireydungeon":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.FIERY_DUNGEON);
                break;
            case "tutorial_island":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.TUTORIAL_ISLAND);
                break;
            case "achievement_easteregg_portal_cakelie":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CAKE_IS_A_LIE);
                break;
            case "cityofcyrennica":
                Achievements.getInstance().giveAchievement(pl.getUniqueId(), Achievements.EnumAchievements.CYRENNICA);
                break;
        }
    }
}
