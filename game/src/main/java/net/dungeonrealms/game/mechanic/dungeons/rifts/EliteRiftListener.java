package net.dungeonrealms.game.mechanic.dungeons.rifts;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.world.entity.type.monster.boss.RiftEliteBoss;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.TileEntityEndGateway;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_9_R2.block.CraftEndGateway;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.MaterialData;

/**
 * Created by Rar349 on 6/8/2017.
 */
public class EliteRiftListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent evt) {
        if (!DungeonManager.isDungeon(evt.getPlayer().getWorld(), DungeonType.ELITE_RIFT)) return;
        EliteRift rift = (EliteRift) DungeonManager.getDungeon(evt.getPlayer().getWorld());
            RiftEliteBoss boss = (RiftEliteBoss) rift.getBoss();
            if (boss == null || !boss.isAlive() || rift.isFinished()) return;
            if(boss.getStage().equals(RiftEliteBoss.BossStage.LAVA_TRAIL)) {
                if (!evt.getTo().equals(evt.getFrom()))
                    rift.getLastMovements().put(evt.getPlayer(), System.currentTimeMillis());

                    Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
                        Block bl = evt.getFrom().clone().subtract(0, 1, 0).getBlock();
                        if (!bl.getType().equals(Material.AIR) && !bl.getType().equals(Material.STATIONARY_LAVA) && !bl.getType().equals(Material.LAVA)) {
                            Material type = bl.getType();
                            MaterialData data = new MaterialData(type, bl.getData());
                            rift.getBlockTypes().put(bl.getLocation(), new Tuple<>(data, System.currentTimeMillis()));
                            bl.setTypeIdAndData(Material.STATIONARY_LAVA.getId(), (byte) 0, false);
                        }
                    }, 5);
            }
                int minYCoord = rift.getMap().getSpawnLocation().getBlockY();
                int hisY = evt.getPlayer().getLocation().getBlockY();
                if (hisY < minYCoord) {
                    GameAPI.teleport(evt.getPlayer(), TeleportLocation.CYRENNICA.getLocation());
                    evt.getPlayer().sendMessage(ChatColor.RED + "You fell into the unknown!");
                }
    }

    @EventHandler
    public void onSandBlockFall(EntityChangeBlockEvent evt) {
        if(evt.getEntityType() != EntityType.FALLING_BLOCK) return;
        if (!DungeonManager.isDungeon(evt.getEntity().getWorld(), DungeonType.ELITE_RIFT)) return;
        EliteRift rift = (EliteRift) DungeonManager.getDungeon(evt.getEntity().getWorld());
        FallingBlock block = (FallingBlock)evt.getEntity();
        if(block.getMaterial().equals(Material.COAL_BLOCK)) {
            evt.setCancelled(true);
            evt.getEntity().remove();
            evt.getBlock().setType(Material.END_GATEWAY);
            TileEntityEndGateway gateway = ((CraftEndGateway) evt.getBlock().getState()).getTileEntity();
            gateway.exactTeleport = true;
            //Send end teleport location.
            gateway.exitPortal = new BlockPosition(evt.getBlock().getLocation().getX(), evt.getBlock().getLocation().getY(), evt.getBlock().getLocation().getZ());
            rift.getBlackHoles().add(evt.getBlock());
            return;
        }
        evt.getEntity().remove();
        evt.setCancelled(true);
    }

    @EventHandler
    public void onBlackHole(PlayerTeleportEvent evt) {
        if(evt.getCause() == PlayerTeleportEvent.TeleportCause.END_GATEWAY) {
            evt.setCancelled(true);
            GameAPI.teleport(evt.getPlayer(), TeleportLocation.CYRENNICA.getLocation());
            evt.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "You were sucked into a black hole!");
        }
    }


}
