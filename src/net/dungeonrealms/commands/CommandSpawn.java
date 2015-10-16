package net.dungeonrealms.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.monsters.*;
import net.dungeonrealms.entities.utils.BuffUtils;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.enums.EnumMonster;
import net.dungeonrealms.mastery.NBTUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Random;

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
                    break;
                case "monster": {
                    if (args.length >= 2) {
                        int tier = 1;
                        if (args.length == 3) {
                            tier = Integer.parseInt(args[2]);
                        }
                        boolean elite = false;
                        if (args.length == 4) {
                            if (args[3].equalsIgnoreCase("*"))
                                elite = true;
                        }
                        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
                        Entity entity;
                        World world = ((CraftWorld) player.getWorld()).getHandle();
                        switch (args[1]) {
                            case "bandit":
                                entity = new EntityBandit(world, tier, type);
                                break;
                            case "rangedpirate":
                                entity = new EntityRangedPirate(world, type, tier);
                                break;
                            case "pirate":
                                entity = new EntityPirate(world, type, tier);
                                break;
                            case "imp":
                                entity = new EntityFireImp(world, tier, type);
                                break;
                            case "troll":
                                entity = new BasicMeleeMonster(world, EnumMonster.Troll, tier);
                                break;
                            case "goblin":
                                entity = new BasicMeleeMonster(world, EnumMonster.Goblin, tier);
                                break;
                            case "mage":
                                entity = new BasicMageMonster(world, EnumMonster.Mage, tier);
                                break;
                            case "spider":
                                entity = new EntitySpider(world, EnumMonster.Spider, tier);
                                break;
                            case "golem":
                                entity = new EntityGolem(world, tier, type);
                                break;
                            default:
                                entity = new EntityBandit(world, tier, type);
                                break;
                        }
                        if(elite){
                          int lvl = Utils.getRandomFromTier(tier);
                          EntityStats.setMonsterElite(entity, lvl, tier);
                        }
                        Location location = new Location(world.getWorld(), player.getLocation().getX() + new Random().nextInt(3), player.getLocation().getY(), player.getLocation().getZ() + new Random().nextInt(3));
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                        world.addEntity(entity, SpawnReason.CUSTOM);
                        entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    }
                    break;
                }
                case "spawner":
                    String monster = args[1];
                    int tier = 1;
                    if (args.length == 3)
                        tier = Integer.parseInt(args[2]);
                    MobSpawner spawner = new MobSpawner(player.getLocation(), monster, tier);
    				String text = (player.getLocation().getX() + "," + player.getLocation().getY() + ","
    				        + player.getLocation().getZ() + "=" + args[1] + ":" + tier);
    				SpawningMechanics.SPANWER_CONFIG.add(text);
    				DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPANWER_CONFIG);
                    SpawningMechanics.add(spawner);
                    break;
            }
        }
        return true;
    }
}
