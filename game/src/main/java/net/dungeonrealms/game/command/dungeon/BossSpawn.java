package net.dungeonrealms.game.command.dungeon;

import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.Burick;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.InfernalAbyss;
import net.dungeonrealms.game.world.entity.type.monster.boss.type.Mayel;
import net.dungeonrealms.game.world.entity.util.EntityStats;
import net.minecraft.server.v1_9_R2.Entity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by Alan on 8/2/2016.
 */
public class BossSpawn extends BaseCommand {
    public BossSpawn(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 3) return true;
        if (!(sender instanceof Player)) return true;
        final Player player = (Player) sender;
        if (!(Rank.isGM(player))) return true;

        Location toSpawn = player.getLocation();

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Syntax: /bspawn mayel|burick|infernal");
        }
        switch (args[0].toLowerCase()) {
            case "mayel":
                Entity mayel = new Mayel(((CraftWorld) player.getWorld()).getHandle(), toSpawn);
                MetadataUtils.registerEntityMetadata(mayel, EnumEntityType.HOSTILE_MOB, 1, 100);
                EntityStats.setBossRandomStats(mayel, 100, 1);
                mayel.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                ((CraftWorld) player.getWorld()).getHandle().addEntity(mayel, CreatureSpawnEvent.SpawnReason.CUSTOM);
                mayel.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                toSpawn.getWorld().playSound(toSpawn, Sound.AMBIENT_CAVE, 1F, 1F);
                break;
            case "burick":
                Entity burick = new Burick(((CraftWorld) player.getWorld()).getHandle(), toSpawn);
                MetadataUtils.registerEntityMetadata(burick, EnumEntityType.HOSTILE_MOB, 1, 100);
                EntityStats.setBossRandomStats(burick, 100, 3);
                burick.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                ((CraftWorld) player.getWorld()).getHandle().addEntity(burick, CreatureSpawnEvent.SpawnReason.CUSTOM);
                burick.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                toSpawn.getWorld().playSound(toSpawn, Sound.ENTITY_ENDERDRAGON_HURT, 4F, 0.5F);
                break;
            case "infernal":
            case "infernalabyss":
                Entity infernal = new InfernalAbyss(((CraftWorld) player.getWorld()).getHandle(), toSpawn);
                MetadataUtils.registerEntityMetadata(infernal, EnumEntityType.HOSTILE_MOB, 1, 100);
                EntityStats.setBossRandomStats(infernal, 100, 4);
                infernal.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                ((CraftWorld) player.getWorld()).getHandle().addEntity(infernal, CreatureSpawnEvent.SpawnReason.CUSTOM);
                infernal.setLocation(toSpawn.getX(), toSpawn.getY(), toSpawn.getZ(), 1, 1);
                toSpawn.getWorld().playSound(toSpawn, Sound.ENTITY_LIGHTNING_THUNDER, 1F, 1F);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Syntax: /bspawn mayel|burick|infernal");
                break;
        }
        return true;
    }
}
