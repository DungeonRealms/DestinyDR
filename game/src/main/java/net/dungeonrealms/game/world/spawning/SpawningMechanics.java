package net.dungeonrealms.game.world.spawning;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.command.CommandSpawner;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;

/**
 * SpawningMechanics - Handles spawner mechanics.
 * 
 * Redone April 21st, 2017.
 * @author Kneesnap
 */
public class SpawningMechanics implements GenericMechanic {
	
	@Getter private static List<String> spawnerConfig = new ArrayList<>();
    @Getter private static List<MobSpawner> spawners = new ArrayList<>();

    private static void initAllSpawners() {
    	getSpawners().forEach(MobSpawner::init);
    }

    private static void killAll() {
    	getSpawners().forEach(MobSpawner::kill);
    	
    	GameAPI.getMainWorld().getEntities().forEach(entity -> {
            ((CraftWorld) entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
    	
        GameAPI.getMainWorld().getLivingEntities().forEach(entity -> {
            ((CraftWorld) entity.getWorld()).getHandle().removeEntity(((CraftEntity) entity).getHandle());
            entity.remove();
        });
    }
    
    public static void saveConfig() {
    	List<String> config = new ArrayList<String>();
    	for (MobSpawner s : getSpawners())
    		config.add(s.getSerializedString());
    	DungeonRealms.getInstance().getConfig().set("spawners", config);
    	DungeonRealms.getInstance().saveConfig();
    }

    private static void loadSpawners() {
    	Utils.log.info("DungeonRealms - Loading spawners...");
    	spawnerConfig = DungeonRealms.getInstance().getConfig().getStringList("spawners");
    	
    	// Load normal mobs from config.
    	for (String line : spawnerConfig)
    		if (line != null && line.length() > 0)
    			getSpawners().add(loadSpawner(line));
    	
    	// Load named elites.
    	for (EnumNamedElite elite : EnumNamedElite.values())
    		getSpawners().add(new EliteMobSpawner(elite));
    	
    	initAllSpawners();
    }
    
    public static MobSpawner loadSpawner(String line) {
    	// Load Coords
    	String[] coords = line.split("=")[0].split(",");
    	double x = Double.parseDouble(coords[0]);
    	double y = Double.parseDouble(coords[1]);
    	double z = Double.parseDouble(coords[2]);
    	
    	Location location = new Location(GameAPI.getMainWorld(), x, y, z);
    	
    	// Load general info.
    	boolean elite = line.contains("*");
    	if (elite)
    		line = line.replace("*", "");
    	
    	int tier = Integer.parseInt(line.split(":")[1].split(";")[0]);
    	int amount = Integer.parseInt(line.split(";")[1].substring(0, 1));
    	
    	// Load mob type.
    	String monsterType = line.split("=")[1].split(":")[0].split("\\(")[0];
    	EnumMonster monster = EnumMonster.getByName(monsterType);

    	// Spawn data.
    	int spawnDelay = Integer.parseInt(line.split("@")[1].split("#")[0]);
    	
    	if (monster == null) {
    		Bukkit.getLogger().warning("Failed to create monster '" + monsterType + "'.");
    		return null;
    	}
    	
    	// Calculate location ranges.
    	String[] locationRange = line.split("#")[1].split("\\$")[0].split("-");
    	
    	int minXZ = Integer.parseInt(locationRange[0]);
    	int maxXZ = Integer.parseInt(locationRange[1]);
    	
    	String mobPower = String.valueOf(line.charAt(line.indexOf("@") - 1)).equals("+") ? "high" : "low";
    	
    	String name = "";
    	if (line.contains("(") && line.contains(")")) {
			name = line.split("\\(")[1].split("\\)")[0].replaceAll("_", " ");
		}
    	// Create spawner.
    	MobSpawner spawner;
    	if (elite) {
    		spawner = new EliteMobSpawner(location, name, monster, tier, spawnDelay, minXZ);
    	} else {
    		spawner = new BaseMobSpawner(location, monster, name, tier, amount, mobPower, spawnDelay, minXZ, maxXZ);
    	}
    	
    	if (line.endsWith("$"))
    		return spawner;
    	
    	// There's more data.
    	
    	// Load weapon.
    	if (line.contains("@WEP@")) {
    		String weaponData = line.split("@WEP@")[1];
    		
    		ItemType type = ItemType.getByName(weaponData);
    		if (type == null) {
    			Bukkit.getLogger().warning("Invalid weapon type: " + weaponData);
    		} else {
    			spawner.setWeaponType(type);
    		}
    	}
    	
    	// Load Element
    	if (line.contains("@ELEM@")) {
    		String elemental = line.split("@ELEM@")[1];
    		
    		double chance = 100;
    		
    		if (elemental.contains("%")) {
    			String[] args = elemental.split("%");
    			chance = Double.parseDouble(args[1]);
    			elemental = args[0];
    		}
    		
    		ElementalAttribute element = ElementalAttribute.getByName(elemental);
            if (element == null) {
                Bukkit.getLogger().info("Invalid element type: " + elemental);
            } else {
                spawner.setElement(element);
                spawner.setElementChance(chance);
            }
    	}
    	return spawner;
    }

    public static void remove(MobSpawner mobSpawner) {
    	getSpawners().remove(mobSpawner);
    	mobSpawner.remove();

        if (mobSpawner.getEditHologram() != null)
            mobSpawner.getEditHologram().delete();

        CommandSpawner.shownMobSpawners.remove(mobSpawner.getLocation());
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
    public void startInitialization() {
        loadSpawners();
    }

    @Override
    public void stopInvocation() {
        killAll();
    }
}
