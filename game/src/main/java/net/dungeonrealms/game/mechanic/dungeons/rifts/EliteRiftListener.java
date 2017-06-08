package net.dungeonrealms.game.mechanic.dungeons.rifts;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;

/**
 * Created by Rar349 on 6/8/2017.
 */
public class EliteRiftListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        if (!DungeonManager.isDungeon(evt.getPlayer().getWorld(), DungeonType.ELITE_RIFT)) return;
        EliteRift rift = (EliteRift) DungeonManager.getDungeon(evt.getPlayer().getWorld());
        if(rift.getBossType().equals(EliteBossType.CLEAR_FLOOR)) {
            if(evt.getTo() != evt.getFrom()) {
                rift.getLastMovements().put(evt.getPlayer(), System.currentTimeMillis());

                Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                    Block bl = evt.getFrom().clone().subtract(0,1,0).getBlock();
                    if(!bl.getType().equals(Material.AIR) && !bl.getType().equals(Material.STATIONARY_LAVA) && !bl.getType().equals(Material.LAVA)) {
                        Material type = bl.getType();
                        MaterialData data = new MaterialData(type, bl.getData());
                        rift.getBlockTypes().put(bl.getLocation(), new Tuple<>(data, System.currentTimeMillis()));
                        bl.setType(Material.STATIONARY_LAVA);
                    }
                },5);
            }

          int minYCoord = rift.getMap().getSpawnLocation().getBlockY();
            int hisY = evt.getPlayer().getLocation().getBlockY();
            if(hisY < minYCoord) {
                GameAPI.teleport(evt.getPlayer(), TeleportLocation.CYRENNICA.getLocation());
                evt.getPlayer().sendMessage(ChatColor.RED + "You fell into the unknown!");
            }
        }
    }


}
