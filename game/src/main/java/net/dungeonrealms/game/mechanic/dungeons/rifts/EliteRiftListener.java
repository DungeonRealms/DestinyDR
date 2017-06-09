package net.dungeonrealms.game.mechanic.dungeons.rifts;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.util.ChatColor;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.world.entity.type.monster.boss.RiftEliteBoss;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
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
        if (rift.getBossType().equals(EliteBossType.CLEAR_FLOOR)) {
            RiftEliteBoss boss = (RiftEliteBoss) rift.getBoss();
            if (boss == null || !boss.isAlive() || rift.isFinished()) return;
            if (!evt.getTo().equals(evt.getFrom())) rift.getLastMovements().put(evt.getPlayer(), System.currentTimeMillis());
                if (evt.getPlayer().equals(boss.getTarget())) {

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
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent evt) {
        if (!DungeonManager.isDungeon(evt.getEntity().getWorld(), DungeonType.ELITE_RIFT)) return;
        EliteRift rift = (EliteRift) DungeonManager.getDungeon(evt.getEntity().getWorld());
        if (rift.getBossType().equals(EliteBossType.CLEAR_FLOOR)) {
            RiftEliteBoss boss = (RiftEliteBoss) rift.getBoss();
            if (boss.getBukkit() != evt.getEntity()) return;
            if (!(evt.getTarget() instanceof Player)) return;
            Player target = (Player) evt.getTarget();
            boss.say(ChatColor.YELLOW + ChatColor.BOLD.toString() + target.getName() + ChatColor.RED + " better run!");
        }
    }


}
