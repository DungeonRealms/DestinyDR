package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.AchievementManager;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.profession.Fishing;
import net.dungeonrealms.game.profession.Mining;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by chase on 7/11/2016.
 */
public class TutorialIsland implements GenericMechanic, Listener {

    public static Map<UUID, List<String>> WELCOMES = new HashMap<>();

    public static HashMap<String, List<String>> completion_delay = new HashMap<>();
    // Player_name, List of NPC names who have a timer event to tell them they've completed running. (used for rewards)

    public static final String tutorialRegion = "tutorial";
    // Region name of tutorial island.

    List<String> got_exp = new ArrayList<>();
    // Already got exp.

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


    public boolean onTutorialIsland(UUID uuid) {
        return AchievementManager.REGION_TRACKER.get(uuid).equalsIgnoreCase("tutorial");
    }

    public static boolean onTutorialIsland(Location loc) {
        if (loc == null) {
            return false;
        }
        
        return GameAPI.getRegionName(loc).equalsIgnoreCase(tutorialRegion);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player pl = e.getPlayer();
        if (onTutorialIsland(pl.getLocation())) {
            e.setCancelled(true);
            pl.updateInventory();
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() == EntityType.ITEM_FRAME) {
            ItemFrame is = (ItemFrame) event.getEntity();
            is.setItem(is.getItem());
            is.setRotation(Rotation.NONE);
            event.setCancelled(true);
            if (event.getDamager() instanceof Player) {
                if (is.getItem().getType() != Material.MAP) return;
                Player plr = (Player) event.getDamager();
                if (plr.getInventory().contains(is.getItem())) {
                    return;
                }
                plr.getInventory().addItem(is.getItem());
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if(Rank.isGM(event.getPlayer()))return;
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
            event.setCancelled(true);
            ItemFrame is = (ItemFrame) event.getRightClicked();
            is.setItem(is.getItem());
            is.setRotation(Rotation.NONE);
            return;
        }
    }


    @Override
    public void stopInvocation() {

    }
}