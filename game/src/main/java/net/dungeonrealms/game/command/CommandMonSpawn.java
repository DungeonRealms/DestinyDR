package net.dungeonrealms.game.command;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.command.BaseCommand;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.mechanic.dungeons.DungeonType;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 9/17/2015.
 */
public class CommandMonSpawn extends BaseCommand {

    public CommandMonSpawn() {
        super("monspawn", "/<command> [args]", "Spawn monsters");
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String string, String[] args) {
        if (s instanceof ConsoleCommandSender)
            return false;
        
        if (args.length < 5) {
        	s.sendMessage(ChatColor.RED + "Syntax: /monspawn <monster> <tier> <elite> <> <display_name>");
        	return true;
        }
        
        //args[0] = monsterType
        //args[1] = tier
        //args[2] = true || elite -> elite.
        //args[3] = Ignored, kept so old command blocks don't break.
        //args[4] = DisplayName
        
        Location spawn = s instanceof BlockCommandSender ? ((BlockCommandSender)s).getBlock().getLocation() : null;
        if (s instanceof Player)
        	spawn = ((Player)s).getLocation();
        spawn = spawn.clone().add(0, 2, 0);
        
        boolean elite = args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("elite");
        
        String customName = args[4].equalsIgnoreCase("null") ? "" : args[4];
        customName = ChatColor.translateAlternateColorCodes('&', customName);
        customName = customName.replaceAll("_", " ");
        customName = customName.replaceAll("&u", ChatColor.UNDERLINE.toString()); //TODO: Convert existing command blocks to use proper color codes.
        customName = customName.replaceAll("&s", ChatColor.BOLD.toString());
        customName = customName.replaceAll("&i", ChatColor.ITALIC.toString());
        customName = customName.replaceAll("&m", ChatColor.MAGIC.toString());
        
        EnumMonster type = EnumMonster.getMonsterByString(args[0]);
        
        if (type == null) {
        	s.sendMessage(ChatColor.RED + "Unknown monster '" + args[0] + "'.");
        	if (s instanceof BlockCommandSender)
        		GameAPI.sendDevMessage("Command block at " + ((BlockCommandSender)s).getBlock().getLocation().toString() + " tried to spawn an unknown monster.");
        	return true;
        }
        
        //TODO: Just change the command blocks instead of using this junk.
        if (type == EnumMonster.Enderman && DungeonManager.isDungeon(spawn.getWorld(), DungeonType.THE_INFERNAL_ABYSS))
        	if (customName.contains("The Devastator") || customName.contains("The Annihilator"))
        		type = EnumMonster.InfernalEndermen;
       
        int tier = Integer.parseInt(args[1]);
        int level = Utils.getRandomFromTier(tier, "high");
        
        if (elite) {
        	EntityAPI.spawnElite(spawn, null, type, tier, level, customName);
        } else {
        	EntityAPI.spawnCustomMonster(spawn, type, level, tier, null, customName);
        }
        
        return true;
    }
}
