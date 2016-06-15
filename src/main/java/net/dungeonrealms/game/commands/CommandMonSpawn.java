package net.dungeonrealms.game.commands;

import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.commands.generic.BasicCommand;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.NBTUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanics.DungeonManager;
import net.dungeonrealms.game.mechanics.DungeonManager.DungeonObject;
import net.dungeonrealms.game.player.rank.Rank;
import net.dungeonrealms.game.world.entities.EnumEntityType;
import net.dungeonrealms.game.world.entities.types.monsters.EnumBoss;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumNamedElite;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Burick;
import net.dungeonrealms.game.world.entities.types.monsters.boss.InfernalAbyss;
import net.dungeonrealms.game.world.entities.types.monsters.boss.Mayel;
import net.dungeonrealms.game.world.entities.types.monsters.boss.subboss.Pyromancer;
import net.dungeonrealms.game.world.entities.utils.BuffUtils;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.spawning.BaseMobSpawner;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Random;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandMonSpawn extends BasicCommand {

    public CommandMonSpawn(String command, String usage, String description) {
        super(command, usage, description);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender) {
            return false;
        }

        if (s instanceof BlockCommandSender) {
            if (args.length > 0)
                switch (args[0]) {
                    case "boss":
                        if (args.length < 5) {
                            s.sendMessage("/monspawn boss (monster name) (x) (y) (z)");
                            return false;
                        }
                        String bossName = args[1];
                        int x = Integer.parseInt(args[2]);
                        int y = Integer.parseInt(args[3]);
                        int z = Integer.parseInt(args[4]);
                        Entity entity = null;
                        BlockCommandSender block = (BlockCommandSender) s;
                        World world = ((CraftWorld) block.getBlock().getWorld()).getHandle();
                        Location loc = new Location(block.getBlock().getWorld(), x, y, z, 1, 1);
                        EnumBoss boss = EnumBoss.getByID(bossName);
                        switch (boss) {
                        case Mayel:
                            entity = new Mayel(world, loc);
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 1, 100);
            				EntityStats.setBossRandomStats(entity, 100, 1);
                            break;
                        case Burick:
                            entity = new Burick(world, loc);
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 3, 100);
            				EntityStats.setBossRandomStats(entity, 100, 3);
                            break;
                        case Pyromancer:
                            entity = new Pyromancer(world);
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 1, 50);
            				EntityStats.setBossRandomStats(entity, 50, 1);
                            break;
                        case InfernalAbyss:
                            entity = new InfernalAbyss(world, loc);
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 4, 100);
            				EntityStats.setBossRandomStats(entity, 100, 4);
                            break;
//                		case LordsGuard:
//                			entity = new InfernalLordsGuard(world, player.getLocation());
//                			break;
                        default:
                            entity = null;
                    }
                    if (entity == null)
                        return false;
                    if(DungeonManager.getInstance().getDungeon(loc.getWorld()) != null){
                    	DungeonObject d = DungeonManager.getInstance().getDungeon(loc.getWorld());
                    	if(!d.canSpawnBoss){
                    		for(Player p : block.getBlock().getWorld().getPlayers()){
                    			int NinetyPercent = (int) (d.maxAlive - (d.maxAlive * 1.9));
                    			p.sendMessage(ChatColor.RED + "You need to kill " + ChatColor.UNDERLINE + (d.aliveMonsters.size() - NinetyPercent) + ChatColor.RED + " monsters to spawn the boss.");
                    		}
                    		return false;
                    	}
                    }
                        entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), 1, 1);
                    world.addEntity(entity, SpawnReason.CUSTOM);
                    entity.setLocation(loc.getX(), loc.getY(), loc.getZ(), 1, 1);
                    ((BlockCommandSender) s).getBlock().setType(Material.AIR);
                }
            return false;
        }

        Player player = (Player) s;
        if (!Rank.isGM(player)) {
            return false;
        }
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
                        String lvlRange = "low";
                        if (args.length == 4) {
                            if (args[3].equalsIgnoreCase("*")) {
                                elite = true;
                            } else if (args[3].equalsIgnoreCase("+")) {
                                lvlRange = "high";
                            } else if (args[3].equalsIgnoreCase("-")) {
                                lvlRange = "low";
                            }
                        }
                        EnumMonster monsEnum = EnumMonster.getMonsterByString(args[1]);
                        EnumEntityType type = EnumEntityType.HOSTILE_MOB;
                        Entity entity = SpawningMechanics.getMob(((CraftWorld) player.getWorld()).getHandle(), tier, monsEnum);

                        int level = Utils.getRandomFromTier(tier, lvlRange);
                        MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, tier, level);
                        EntityStats.setMonsterRandomStats(entity, level, tier);

                        World world = ((CraftWorld) player.getWorld()).getHandle();
                        if (elite) {
                            EntityStats.setMonsterElite(entity, EnumNamedElite.NONE, tier, monsEnum);
                        }
                        
                        String lvl = ChatColor.LIGHT_PURPLE.toString() + "[" + level + "] " + ChatColor.RESET;
                        String customName = entity.getBukkitEntity().getMetadata("customname").get(0).asString();
                        
                        entity.setCustomName(lvl + API.getTierColor(tier) + customName);
                        
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
                    int spawnDelay = 20;
                    if (args.length == 4)
                        spawnDelay = Integer.parseInt(args[3]);
                    BaseMobSpawner spawner = new BaseMobSpawner(player.getLocation(), monster, tier, 4, SpawningMechanics.getALLSPAWNERS().size(), "high", spawnDelay, 1, 2);
                    String text = (player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ() + "=" + monster + ":" + tier + "@" + spawnDelay + "#");
                    SpawningMechanics.SPAWNER_CONFIG.add(text);
                    DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                    SpawningMechanics.getALLSPAWNERS().add(spawner);
                    break;
                case "boss":
                    String bossName = args[1];
                    Entity entity = null;
                    World world = ((CraftWorld) player.getWorld()).getHandle();
                    EnumBoss boss = EnumBoss.getByID(bossName);
                    switch (boss) {
                        case Mayel:
                            entity = new Mayel(world, player.getLocation());
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 1, 100);
            				EntityStats.setBossRandomStats(entity, 100, 1);
                            break;
                        case Burick:
                            entity = new Burick(world, player.getLocation());
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 3, 100);
            				EntityStats.setBossRandomStats(entity, 100, 3);
                            break;
                        case Pyromancer:
                            entity = new Pyromancer(world);
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 1, 50);
            				EntityStats.setBossRandomStats(entity, 50, 1);
                            break;
                        case InfernalAbyss:
                            entity = new InfernalAbyss(world, player.getLocation());
            				MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 4, 100);
            				EntityStats.setBossRandomStats(entity, 100, 4);
                            break;
//                		case LordsGuard:
//                			entity = new InfernalLordsGuard(world, player.getLocation());
//                			break;
                        default:
                            entity = null;
                    }
                    if (entity == null)
                        return false;

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
