package net.dungeonrealms.game.mechanic.dungeons;

import com.google.common.collect.Lists;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.Rank;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.event.PlayerEnterRegionEvent;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DungeonListener - Listens for basic dungeon events.
 * <p>
 * Redone on April 28th, 2017.
 *
 * @author Kneesnap
 */
public class DungeonListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeaveDungeon(PlayerEnterRegionEvent evt) {
        if (!DungeonManager.isDungeon(evt.getPlayer()) || !evt.getNewRegion().toLowerCase().startsWith("exit_instance"))
            return;

        Player player = evt.getPlayer();
        player.teleport(TeleportLocation.CYRENNICA.getLocation());

        Affair.getParty(player).announce(player.getName() + " has left the dungeon.");
    }

    /**
     * Handles a player entering a dungeon.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerAttemptDungeonEnter(PlayerEnterRegionEvent event) {
        if (!GameAPI.isMainWorld(event.getPlayer()) || !event.getNewRegion().toLowerCase().startsWith("instance_"))
            return;
        Player player = event.getPlayer();
        PlayerWrapper hisWrapper = PlayerWrapper.getPlayerWrapper(player);
        List<Player> players = Lists.newArrayList();
        DungeonType type = DungeonType.getInternal(event.getNewRegion().split("_")[1]);
        if(type == null) return;
        if(type.isOnCooldown(hisWrapper)) {
            player.sendMessage(ChatColor.RED + "You can not join this dungeon because you have already recently completed it!");
            player.sendMessage(ChatColor.GRAY + "You must wait until " + type.getCooldownString(hisWrapper) + " before joining the dungeon again!");
            return;
        }
        if(Affair.isInParty(player)) {
            for(Player member : Affair.getParty(player).getAllMembers()) {
                if(!member.getWorld().equals(player.getLocation().getWorld())) continue;
                if(member.getLocation().distanceSquared(player.getLocation()) > 200) continue;
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(member);
                if(type.isOnCooldown(wrapper)) {
                    member.sendMessage(ChatColor.RED + "You can not join this dungeon because you have already recently completed it!");
                    member.sendMessage(ChatColor.GRAY + "You must wait until " + type.getCooldownString(wrapper) + " before joining the dungeon again!");
                    player.sendMessage(ChatColor.RED + player.getName() + " has already done this dungeon recently!");
                    return;
                }
                players.add(member);
            }
        } else {
            players.add(player);
        }

        DungeonManager.createDungeon(type, players);
    }

    /**
     * Handles testing for custom items.
     */
    @EventHandler
    public void onHopperPickup(InventoryPickupItemEvent evt) {
        if (!(evt.getInventory().getHolder() instanceof Hopper))
            return;

        Hopper h = (Hopper) evt.getInventory().getHolder();
        Matcher mName = Pattern.compile("Custom ID: <(\\w+)>").matcher(h.getInventory().getTitle());
        if (!mName.find())
            return;

        evt.setCancelled(true);
        if (mName.group(1).equalsIgnoreCase(GameAPI.getCustomID(evt.getItem().getItemStack()))) {
            evt.getItem().remove();
            h.getBlock().getLocation().subtract(0, 1, 0).getBlock().setType(Material.REDSTONE_BLOCK);
        } else {
            evt.getItem().setVelocity(new Vector(0, .25F, 0));
        }
    }

    @EventHandler
    public void onPlayerOpen(PlayerInteractEntityEvent event) {
        if (DungeonManager.isDungeon(event.getRightClicked().getWorld())) {
            if (event.getRightClicked() instanceof Villager) {
                event.setCancelled(true);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1.4F);
//                event.getPlayer().closeInventory();
            }
        }
    }

    /**
     * Prevents opening hoppers.
     */
    @EventHandler
    public void onHopperInteract(PlayerInteractEvent evt) {
        if (!evt.hasBlock() || evt.getClickedBlock().getType() != Material.HOPPER || !DungeonManager.isDungeon(evt.getClickedBlock().getLocation()))
            return;
        if (!Rank.isGM(evt.getPlayer()))
            evt.setCancelled(true);
    }
}
