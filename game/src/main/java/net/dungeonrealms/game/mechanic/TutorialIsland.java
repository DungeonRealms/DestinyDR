package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.*;

/**
 * Created by chase on 7/11/2016.
 */
public class TutorialIsland implements GenericMechanic, Listener {

    public static Map<UUID, List<String>> WELCOMES = new HashMap<>();

    public static HashMap<String, List<String>> completion_delay = new HashMap<>();
    // Player_name, List of NPC names who have a timer event to tell them they've completed running. (used for rewards)

    public static final String tutorialRegion = "tutorial_island";
    // Region name of tutorial island.
    
    private static TutorialIsland instance = null;

    public List<String> getWelcomes(UUID uuid) {
        List<String> welcomes;
        if (!WELCOMES.containsKey(uuid)) {
            welcomes = new ArrayList<>();
            WELCOMES.put(uuid, welcomes);
        } else welcomes = WELCOMES.get(uuid);
        return welcomes;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), this::hideVanishedPlayers, 100L, 1L);
    }


    public static TutorialIsland getInstance() {
        if (instance == null) {
            instance = new TutorialIsland();
        }
        return instance;
    }

    private void hideVanishedPlayers() {
        GameAPI._hiddenPlayers.stream().filter(player -> player != null).forEach(player -> {
            for (Player player1 : Bukkit.getOnlinePlayers()) {
                // GMs can see hidden players whereas non-GMs cannot.
                if (player1.getUniqueId().toString().equals(player.getUniqueId().toString()) || Rank.isTrialGM(player1)) {
                    player1.showPlayer(player);
                } else {
                    player1.hidePlayer(player);
                }
            }
        });
    }

    public static boolean onTutorialIsland(Location loc) {
        return loc != null ? GameAPI.getRegionName(loc).equalsIgnoreCase(tutorialRegion) : false;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player pl = e.getPlayer();
        if (onTutorialIsland(pl.getLocation())) {
            e.setCancelled(true);
            pl.updateInventory();
        }
    }


    @Override
    public void stopInvocation() {

    }
}