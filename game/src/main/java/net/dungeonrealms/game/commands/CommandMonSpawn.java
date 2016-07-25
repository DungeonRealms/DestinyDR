package net.dungeonrealms.game.commands;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.commands.BasicCommand;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entities.types.monsters.EnumMonster;
import net.dungeonrealms.game.world.entities.types.monsters.EnumNamedElite;
import net.dungeonrealms.game.world.entities.utils.EntityStats;
import net.dungeonrealms.game.world.spawning.SpawningMechanics;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

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
            BlockCommandSender bcs = (BlockCommandSender) s;
            Location location = bcs.getBlock().getLocation().add(0, 2, 0);
            World nmsWorld = ((CraftWorld) bcs.getBlock().getWorld()).getHandle();
            if (args.length != 5) return true;
            String monsterType = args[0];
            int tier = Integer.parseInt(args[1]);
            boolean elite = false;
            if (args[2].equalsIgnoreCase("true")) {
                elite = true;
            }
            String meta = args[3];
            if (meta.equals("null")) {
                meta = "";
            }
            String customName = args[4];
            if (customName.equalsIgnoreCase("null")) {
                customName = "";
            } else {
                customName = ChatColor.translateAlternateColorCodes('&', customName);
                customName = customName.replaceAll("_", " ");
                customName = customName.replaceAll("&0", ChatColor.BLACK.toString());
                customName = customName.replaceAll("&1", ChatColor.DARK_BLUE.toString());
                customName = customName.replaceAll("&2", ChatColor.DARK_GREEN.toString());
                customName = customName.replaceAll("&3", ChatColor.DARK_AQUA.toString());
                customName = customName.replaceAll("&4", ChatColor.DARK_RED.toString());
                customName = customName.replaceAll("&5", ChatColor.DARK_PURPLE.toString());
                customName = customName.replaceAll("&6", ChatColor.GOLD.toString());
                customName = customName.replaceAll("&7", ChatColor.GRAY.toString());
                customName = customName.replaceAll("&8", ChatColor.DARK_GRAY.toString());
                customName = customName.replaceAll("&9", ChatColor.BLUE.toString());
                customName = customName.replaceAll("&a", ChatColor.GREEN.toString());
                customName = customName.replaceAll("&b", ChatColor.AQUA.toString());
                customName = customName.replaceAll("&c", ChatColor.RED.toString());
                customName = customName.replaceAll("&d", ChatColor.LIGHT_PURPLE.toString());
                customName = customName.replaceAll("&e", ChatColor.YELLOW.toString());
                customName = customName.replaceAll("&f", ChatColor.WHITE.toString());

                customName = customName.replaceAll("&u", ChatColor.UNDERLINE.toString());
                customName = customName.replaceAll("&s", ChatColor.BOLD.toString());
                customName = customName.replaceAll("&i", ChatColor.ITALIC.toString());
                customName = customName.replaceAll("&m", ChatColor.MAGIC.toString());
                //This is autistic. Whoever placed the command blocks with these incorrect color codes should be banned.
            }
            EnumMonster enumMonster = EnumMonster.getMonsterByString(monsterType);
            if (enumMonster == null) {
                enumMonster = EnumMonster.Undead;
            }
            Entity entity = SpawningMechanics.getMob(nmsWorld, tier, enumMonster);
            if (entity == null) {
                return true;
            }
            int level = Utils.getRandomFromTier(tier, "high");
            if (elite) {
                entity.getBukkitEntity().setMetadata("elite", new FixedMetadataValue(DungeonRealms.getInstance(), "true"));
                EntityStats.setMonsterElite(entity, EnumNamedElite.NONE, tier, enumMonster, level, bcs.getBlock().getWorld().getName().contains("DUNGEON"));
            } else if (bcs.getBlock().getWorld().getName().contains("DUNGEON")) {
                entity.getBukkitEntity().setMetadata("dungeon", new FixedMetadataValue(DungeonRealms.getInstance(), true));
                EntityStats.createDungeonMob(entity, level, tier);
            } else {
                EntityStats.setMonsterRandomStats(entity, level, tier);
            }
            SpawningMechanics.rollElement(entity, enumMonster);
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            nmsWorld.addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
            if (!customName.equals("")) {
                entity.setCustomName(GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + customName.trim());
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + customName.trim()));
            } else {
                entity.setCustomName(GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + enumMonster.name.trim());
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + enumMonster.name.trim()));
            }
            return true;
        }

        //TODO: Player spawning.

        /*Player player = (Player) s;
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

                        entity.setCustomName(lvl + GameAPI.getTierColor(tier) + customName);

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
                        case InfernalAbyss:
                            entity = new InfernalAbyss(world, player.getLocation());
                            MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, 4, 100);
                            EntityStats.setBossRandomStats(entity, 100, 4);
                            break;
                        default:
                            entity = null;
                            break;
                    }
                    if (entity == null)
                        return false;

                    Location location = new Location(world.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    world.addEntity(entity, SpawnReason.CUSTOM);
                    entity.setLocation(location.getX(), location.getY(), location.getZ(), 1, 1);
                    break;
            }
        }*/
        return true;
    }
}
