package net.dungeonrealms.game.command;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.DungeonManager;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.type.monster.type.melee.MeleeEnderman;
import net.dungeonrealms.game.world.entity.util.EntityStats;
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
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandMonSpawn extends BaseCommand {

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
            if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("elite")) {
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

            if(entity instanceof MeleeEnderman && DungeonManager.getInstance().getDungeon(nmsWorld.getWorld()) != null){
                ((MeleeEnderman)entity).setTeleport(false);
            }
            if (!customName.equals("")) {
                entity.setCustomName(GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + customName.trim());
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + customName.trim()));
            } else {
                entity.setCustomName(GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + enumMonster.name.trim());
                entity.getBukkitEntity().setMetadata("customname", new FixedMetadataValue(DungeonRealms.getInstance(), GameAPI.getTierColor(tier) + ChatColor.BOLD.toString() + enumMonster.name.trim()));
            }
            DungeonManager.DungeonObject object = DungeonManager.getInstance().getDungeon(bcs.getBlock().getWorld());
            if(object != null){
                object.aliveMonsters.add(entity);
                DungeonManager.getInstance().TRACKED_SPAWNS.put(entity.getUniqueID(), location);
            }
            return true;
        } else if (s instanceof Player) {
            Player player = (Player) s;
            if (Rank.isGM(player)) {
                Location location = player.getLocation();
                World nmsWorld = ((CraftWorld) player.getWorld()).getHandle();
                if (args.length > 0) {
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
                        EntityStats.setMonsterElite(entity, EnumNamedElite.NONE, tier, enumMonster, level, player.getWorld().getName().contains("DUNGEON"));
                    } else if (player.getWorld().getName().contains("DUNGEON")) {
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
            }
        }
        return false;
    }
}
