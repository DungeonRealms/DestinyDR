package net.dungeonrealms.commands;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.metadata.FixedMetadataValue;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.commands.generic.BasicCommand;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumBoss;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.types.monsters.boss.Burick;
import net.dungeonrealms.entities.types.monsters.boss.InfernalAbyss;
import net.dungeonrealms.entities.types.monsters.boss.Mayel;
import net.dungeonrealms.entities.types.monsters.boss.subboss.Pyromancer;
import net.dungeonrealms.entities.utils.BuffUtils;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.NBTUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.spawning.MobSpawner;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.World;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandSpawn extends BasicCommand {

    public CommandSpawn(String command, String usage, String description) {
        super(command, usage, description);
    }

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
                        EnumMonster monsEnum = EnumMonster.getMonsterByString(args[1]);
                        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
                        Entity entity = SpawningMechanics.getMob(((CraftWorld)player.getWorld()).getHandle(), tier, monsEnum);
                        
                    	int level = entity.getBukkitEntity().getMetadata("level").get(0).asInt();
                        String lvl = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "+ChatColor.RESET;
                        String healthName = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt()+ChatColor.RED.toString() + "❤";
                        String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                        ArmorStand stand = entity.getBukkitEntity().getLocation().getWorld().spawn(entity.getBukkitEntity().getLocation(), ArmorStand.class);
                        stand.setRemoveWhenFarAway(false);
                        stand.setVisible(false);
                        stand.setSmall(true);
                        stand.setBasePlate(false);
                        stand.setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "nametag"));
                        stand.setGravity(false);
                        stand.setArms(false);
                        stand.setCustomNameVisible(true);
                        stand.setCustomName(lvl + customName + healthName);
                        LivingEntity ent = stand;
                        ent.setRemoveWhenFarAway(false);
                        entity.getBukkitEntity().setPassenger(stand);

                        
                        
                        World world = ((CraftWorld) player.getWorld()).getHandle();
                        if(elite){
                          EntityStats.setMonsterElite(entity, level, tier);
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
                    MobSpawner spawner = new MobSpawner(player.getLocation(), monster, tier, 4, SpawningMechanics.getSpawners().size());
    				String text = (player.getLocation().getX() + "," + player.getLocation().getY() + ","
    				        + player.getLocation().getZ() + "=" + args[1] + ":" + tier);
    				SpawningMechanics.SPAWNER_CONFIG.add(text);
    				DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                    SpawningMechanics.add(spawner);
                    break;
                case "boss":
                	String bossName = args[1];
                	Entity entity = null;
                    World world = ((CraftWorld) player.getWorld()).getHandle();
                    EnumBoss boss = EnumBoss.getByID(bossName);
                	switch(boss){
                		case Mayel :
                			entity = new Mayel(world, player.getLocation());
                			break;
                		case Burick:
                			entity = new Burick(world, player.getLocation());
                			break;
                		case Pyromancer:
                			entity = new Pyromancer(world, player.getLocation());
                			break;
                		case InfernalAbyss:
                			entity = new InfernalAbyss(world,player.getLocation());
                			break;
//                		case LordsGuard:
//                			entity = new InfernalLordsGuard(world, player.getLocation());
//                			break;
                		default: 
                			entity = null;
                	}
                	if(entity == null)
                		return false;
                	
                	int level = entity.getBukkitEntity().getMetadata("level").get(0).asInt();
                    String lvl = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] "+ChatColor.RESET;
                    String healthName = entity.getBukkitEntity().getMetadata("currentHP").get(0).asInt()+ChatColor.RED.toString() + "❤";
                    String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                    ArmorStand stand = entity.getBukkitEntity().getLocation().getWorld().spawn(entity.getBukkitEntity().getLocation(), ArmorStand.class);
                    stand.setRemoveWhenFarAway(false);
                    stand.setVisible(false);
                    stand.setSmall(true);
                    stand.setBasePlate(false);
                    stand.setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "nametag"));
                    stand.setGravity(false);
                    stand.setArms(false);
                    stand.setCustomNameVisible(true);
                    stand.setCustomName(lvl + customName + healthName);
                    LivingEntity ent = stand;
                    ent.setRemoveWhenFarAway(false);
                    entity.getBukkitEntity().setPassenger(stand);

                	
                    Location location = new Location(world.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    world.addEntity(entity, SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    break;
            }
        }
        return true;
    }
}
