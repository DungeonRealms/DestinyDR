package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.Burick;
import net.dungeonrealms.game.world.entity.type.monster.boss.InfernalAbyss;
import net.dungeonrealms.game.world.entity.type.monster.boss.Mayel;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by Kieran Quigley (Proxying) on 17-Jun-16.
 */
public class BossTeleport extends BaseCommand {
    public BossTeleport(String command, String usage, String description) {
        super(command, usage, description);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 3) return true;
        if (!(sender instanceof BlockCommandSender)) return true;

        BlockCommandSender bcs = (BlockCommandSender) sender;
        if (!bcs.getBlock().getWorld().getName().contains("DUNGEON")) return true;
        DungeonManager.DungeonObject dungeonObject = DungeonManager.getInstance().getDungeon(bcs.getBlock().getWorld());
        if (!dungeonObject.canSpawnBoss) {
            for (Player p : bcs.getBlock().getWorld().getPlayers()) {
                int percentToKill = (int) (dungeonObject.maxAlive * 0.80);
                int killed = dungeonObject.killed;
                p.sendMessage(ChatColor.RED + "You need to kill " + ChatColor.UNDERLINE + (percentToKill - killed) + ChatColor.RED + " monsters to spawn the boss.");
            }
            return true;
        }
        if (dungeonObject.hasBossSpawned || dungeonObject.beingRemoved) {
            return true;
        }

        double x = Double.parseDouble(args[0]);
        double y = Double.parseDouble(args[1]);
        double z = Double.parseDouble(args[2]);

        for (Player player : bcs.getBlock().getWorld().getPlayers()) {
            player.teleport(new Location(player.getWorld(), x, y + 2, z));
            player.setFallDistance(0.0F);
        }

        switch (dungeonObject.getType()) {
            case BANDIT_TROVE:
                Location toSpawn = new Location(bcs.getBlock().getWorld(), 529, 55, -313);
                Entity mayel = new Mayel(((CraftWorld) bcs.getBlock().getWorld()).getHandle(), toSpawn);
                MetadataUtils.registerEntityMetadata(mayel, EnumEntityType.HOSTILE_MOB, 1, 100);
                EntityStats.setBossRandomStats(mayel, 100, 1);
                mayel.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                ((CraftWorld) bcs.getBlock().getWorld()).getHandle().addEntity(mayel, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mayel.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                bcs.getBlock().setType(Material.AIR);
                toSpawn.getWorld().playSound(toSpawn, Sound.AMBIENT_CAVE, 1F, 1F);
                break;
            case VARENGLADE:
                Location varen = new Location(bcs.getBlock().getWorld(), -364, 60, -1.2);
                Entity burick = new Burick(((CraftWorld) bcs.getBlock().getWorld()).getHandle(), varen);
                MetadataUtils.registerEntityMetadata(burick, EnumEntityType.HOSTILE_MOB, 1, 100);
                EntityStats.setBossRandomStats(burick, 100, 3);
                burick.setLocation(varen.getX(), varen.getY(), varen.getZ(), 1, 1);
                ((CraftWorld) bcs.getBlock().getWorld()).getHandle().addEntity(burick, CreatureSpawnEvent.SpawnReason.CUSTOM);
                burick.setLocation(varen.getX(), varen.getY(), varen.getZ(), 1, 1);
                bcs.getBlock().setType(Material.AIR);
                varen.getWorld().playSound(varen, Sound.ENTITY_ENDERDRAGON_HURT, 4F, 0.5F);
                break;
            case THE_INFERNAL_ABYSS:
                Location abyss = new Location(bcs.getBlock().getWorld(), -54, 158, 646);
                Entity infernal = new InfernalAbyss(((CraftWorld) bcs.getBlock().getWorld()).getHandle(), abyss);
                MetadataUtils.registerEntityMetadata(infernal, EnumEntityType.HOSTILE_MOB, 1, 100);
                EntityStats.setBossRandomStats(infernal, 100, 4);
                infernal.setLocation(abyss.getX(), abyss.getY(), abyss.getZ(), 1, 1);
                ((CraftWorld) bcs.getBlock().getWorld()).getHandle().addEntity(infernal, CreatureSpawnEvent.SpawnReason.CUSTOM);
                infernal.setLocation(abyss.getX(), abyss.getY(), abyss.getZ(), 1, 1);
                bcs.getBlock().setType(Material.AIR);
                abyss.getWorld().playSound(abyss, Sound.ENTITY_LIGHTNING_THUNDER, 1F, 1F);
                break;
        }
        return false;
    }
}
