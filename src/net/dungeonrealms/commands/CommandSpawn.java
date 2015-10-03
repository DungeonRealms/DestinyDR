package net.dungeonrealms.commands;

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

import net.dungeonrealms.entities.types.monsters.EntityBandit;
import net.dungeonrealms.entities.types.monsters.EntityFireImp;
import net.dungeonrealms.entities.types.monsters.EntityGolem;
import net.dungeonrealms.entities.types.monsters.EntityPirate;
import net.dungeonrealms.entities.types.monsters.EntityRangedPirate;
import net.dungeonrealms.entities.utils.BuffUtils;
import net.dungeonrealms.entities.utils.EntityAPI;
import net.dungeonrealms.entities.utils.MountUtils;
import net.dungeonrealms.entities.utils.PetUtils;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.NBTUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandSpawn implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender)
            return false;
        Player player = (Player) s;
        if (args.length > 0) {
            switch (args[0]) {
                case "wolf":
                    Wolf w = (Wolf) Bukkit.getWorld(player.getWorld().getName()).spawnEntity(player.getLocation(),
                            EntityType.WOLF);
                    NBTUtils.nullifyAI(w);
                    break;
                case "buff":
                    BuffUtils.spawnBuff(player.getUniqueId());
                    Utils.log.info("Spawned Buff");
                    break;
                case "pet": {
                    if (!EntityAPI.hasPetOut(player.getUniqueId())) {
                        PetUtils.spawnPet(player.getUniqueId(), 9);
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
                case "monster": {
                    if (args.length >= 2) {
                        int tier = 1;
                        if (args.length == 3)
                            tier = Integer.parseInt(args[2]);
                        if (args[1].equalsIgnoreCase("pirate")) {
                            World world = ((CraftWorld) player.getWorld()).getHandle();
                            EntityPirate zombie = new EntityPirate(world, EnumEntityType.HOSTILE_MOB, tier);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                            world.addEntity(zombie, SpawnReason.CUSTOM);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                        } else if (args[1].equalsIgnoreCase("rangedpirate")) {
                            World world = ((CraftWorld) player.getWorld()).getHandle();
                            EntityRangedPirate zombie = new EntityRangedPirate(world, EnumEntityType.HOSTILE_MOB, tier);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                            world.addEntity(zombie, SpawnReason.CUSTOM);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                        } else if (args[1].equalsIgnoreCase("imp")) {
                            World world = ((CraftWorld) player.getWorld()).getHandle();
                            EntityFireImp zombie = new EntityFireImp(world, tier, EnumEntityType.HOSTILE_MOB);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                            world.addEntity(zombie, SpawnReason.CUSTOM);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                        } else if (args[1].equalsIgnoreCase("bandit")) {
                            World world = ((CraftWorld) player.getWorld()).getHandle();
                            EntityBandit zombie = new EntityBandit(world, tier, EnumEntityType.HOSTILE_MOB);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                            world.addEntity(zombie, SpawnReason.CUSTOM);
                            zombie.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                        } else if (args[1].equalsIgnoreCase("golem")) {
                            World world = ((CraftWorld) player.getWorld()).getHandle();
                            EntityGolem golem = new EntityGolem(world, tier, EnumEntityType.HOSTILE_MOB);
                            golem.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());
                            world.addEntity(golem, SpawnReason.CUSTOM);
                            golem.setPosition(player.getLocation().getX(), player.getLocation().getY(),
                                    player.getLocation().getZ());

                        }
                    }
                    break;
                }
                case "spawner":
               	 String[] monsters = args[1].split(",");
               	 int tier = 1;
               	 if(args.length ==3)
               		 tier = Integer.parseInt(args[2]);
                    MobSpawner spawner = new MobSpawner(player.getLocation(), monsters,tier);
                    SpawningMechanics.add(spawner);
                    break;
            }
        }
        return true;
    }
}
