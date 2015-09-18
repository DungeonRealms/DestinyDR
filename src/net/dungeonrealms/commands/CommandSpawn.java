package net.dungeonrealms.commands;

import net.dungeonrealms.entities.types.EntityPirate;
import net.dungeonrealms.entities.utils.BuffUtils;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.entities.utils.MountUtils;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.NBTUtils;
import net.dungeonrealms.mastery.Utils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandSpawn implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "wolf":
                    Wolf w = (Wolf) Bukkit.getWorld(player.getWorld().getName()).spawnEntity(player.getLocation(), EntityType.WOLF);
                    NBTUtils.nullifyAI(w);
                    break;
                case "pirate":
                    World world = ((CraftWorld) player.getWorld()).getHandle();
                    EntityPirate zombie = new EntityPirate(world, EnumEntityType.HOSTILE_MOB, 1);
                    zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                    world.addEntity(zombie, SpawnReason.CUSTOM);
                    zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                    Utils.log.info("Spawned");
                    break;
                case "buff":
                    BuffUtils.spawnBuff(player.getUniqueId());
                    Utils.log.info("Spawned Buff");
                    break;
                case "pet": {
                    if (!EntityAPI.hasPetOut(player.getUniqueId())) {
                        PetUtils.spawnPet(player.getUniqueId(), 3);
                        Utils.log.info("Spawned Pet");
                    } else {
                        player.sendMessage("You already have a pet summoned");
                    }
                    break;
                }
                case "mount": {
                    if (!EntityAPI.hasMountOut(player.getUniqueId())) {
                        if (EntityAPI.hasPetOut(player.getUniqueId())) {
                            Entity entity = EntityAPI.getPlayerPet(player.getUniqueId());
                            if (entity.isAlive()) {
                                entity.getBukkitEntity().remove();
                            }
                            EntityAPI.removePlayerPetList(player.getUniqueId());
                            player.sendMessage("Your pet has returned home as you have summoned your mount");
                        }
                        MountUtils.spawnMount(player.getUniqueId(), 5);
                        Utils.log.info("Spawned Mount");
                    } else {
                        player.sendMessage("You already have a mount summoned");
                    }
                    break;
                }
                default:
            }
        }
        return true;
    }
}
