package net.dungeonrealms.game.world.spawning;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumNamedElite;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.Item.ElementalAttribute;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base for a DR Mob Spawner.
 * 
 * @author Unknown
 */
public abstract class MobSpawner {

    @Getter //Where do we spawn them?
    private Location location;
    
    @Getter @Setter //What tier monster are we spawning?
    private int tier;
    
    @Getter //List of monsters we have spawned.
    private List<Entity> spawnedMonsters = new CopyOnWriteArrayList<>();

    @Getter @Setter //How many do we spawn
    private int spawnAmount;
    
    @Setter @Getter //The timer that attempts to spawn in entities.
    private int timerID = -1;
    
    @Getter //"high" or "low". How powerful this entity is.
    private String lvlRange;
    
    @Setter @Getter //Custom name
    private String customName;
    
    @Setter @Getter //Monster type
    private EnumMonster monsterType;
    
    @Setter @Getter // Should we ignore spawn conditions?
    private boolean firstSpawn = true;

    @Setter @Getter
    private int initialRespawnDelay;
    
    @Getter @Setter //Delay between spawns.
    private int respawnDelay;

    @Setter @Getter
    private int counter;

    @Setter @Getter
    private int minimumXZ;
    @Setter @Getter
    private int maximumXZ;

    @Getter @Setter
    private Hologram editHologram;

    @Getter @Setter
    private ItemType weaponType;

    @Getter @Setter
    private ElementalAttribute element;
   
    @Getter @Setter
    private double elementChance;

    @Getter
    private ArmorStand armorStand;
    
    public MobSpawner(Location location, EnumMonster type, String name, int tier, int spawnAmount, String lvlRange, int respawnDelay, int mininmumXZ, int maximumXZ) {
    	setCustomName(name);
    	setMonsterType(type);
    	setSpawnAmount(Math.min(spawnAmount, 8));

        this.lvlRange = lvlRange;
        this.initialRespawnDelay = respawnDelay;

        this.spawnAmount = spawnAmount;
        this.location = location;
        this.tier = tier;
        this.respawnDelay = respawnDelay;
        this.counter = 0;
        setMinimumXZ(mininmumXZ);
        setMaximumXZ(maximumXZ);
        spawnArmorStand();
    }
    
    private void spawnArmorStand() {
    	ArmorStand as = getLocation().getWorld().spawn(getLocation(), ArmorStand.class);
    	
    	// Remove old stands. (Shouldn't happen.)
    	List<org.bukkit.entity.Entity> list = as.getNearbyEntities(1, 1, 1);
    	list.stream().filter(entity -> entity instanceof ArmorStand).forEach(entity -> {
    		entity.remove();
    		if (as.getWorld().getBlockAt(getLocation()).getType() == Material.ARMOR_STAND)
    			as.getWorld().getBlockAt(getLocation()).setType(Material.AIR);
    	});
    	
    	as.setVisible(false);
    	as.setGravity(false);
    	as.setInvulnerable(true);
    }

    public void createEditInformation() {
        if(this.editHologram != null && !this.editHologram.isDeleted())
            this.editHologram.delete();
        
        Hologram holo = HologramsAPI.createHologram(DungeonRealms.getInstance(), getLocation().clone().add(.5, 3.5, .5));

        if (this instanceof EliteMobSpawner) {
            EliteMobSpawner eliteSpawner = (EliteMobSpawner) this;

            EnumNamedElite elite = eliteSpawner.getEliteType();
            if (elite == null && hasCustomName())
                elite = EnumNamedElite.getFromName(getCustomName());

            holo.appendTextLine(ChatColor.GREEN + "Elite Type: " + (elite != null ? elite.toString() : "N/A"));
        }
        
        holo.appendTextLine(ChatColor.GREEN + "Mob Type: " + (getMonsterType() != null ? getMonsterType().name() : "N/A"));

        holo.appendTextLine(ChatColor.GREEN + "Tier: " + this.getTier());
        if (hasCustomName())
            holo.appendTextLine(ChatColor.GREEN + "Custom Name: " + ChatColor.RESET + getCustomName());

        holo.appendTextLine(ChatColor.GREEN + "Level Range: " + this.getLvlRange());
        holo.appendTextLine(ChatColor.GREEN + "Respawn Delay: " + this.getInitialRespawnDelay());
        holo.appendTextLine(ChatColor.GREEN + "Spawn Amount: " + this.getSpawnAmount());
        holo.appendTextLine(ChatColor.GREEN + "Spawn Range: " + this.getMinimumXZ() + " - " + this.getMaximumXZ());

        if (this.weaponType != null)
            holo.appendTextLine(ChatColor.GREEN + "Weapon Type: " + this.weaponType);
        
        if (getElement() != null)
        	holo.appendTextLine(getElement().getColor() + "" + (getElementChance() != 0 ? getElementChance() : "100") + "% chance for " + getElement().getPrefix() + " damage");
        
        this.editHologram = holo;
    }
    
    public boolean hasCustomName() {
    	return this.getCustomName() != null;
    }

    public String getSerializedString() {
        StringBuilder builder = new StringBuilder();
        Location loc = getLocation();
        
        builder.append(loc.getX()).append(",")
        		.append(loc.getY()).append(",")
        		.append(loc.getZ()).append("=");
        
        builder.append(getMonsterType().getIdName());

        if (hasCustomName())
        	builder.append("(").append(getCustomName()).append(")");

        builder.append(":");
        
        // Tier
        builder.append(getTier()).append(";")
        	.append(getSpawnAmount()).append(getLvlRange().equals("high") ? "+" : "-")
        	.append("@");
        
        builder.append(this.initialRespawnDelay).append("#")
        	.append(getMinimumXZ()).append("-").append(getMaximumXZ()).append("$");

        if (this.weaponType != null)
            builder.append("@WEP@").append(weaponType.name()).append("@WEP@");

        // Set element.
        if (getElement() != null) {
        	builder.append("@ELEM@").append(getElement().name());
        	
        	if (getElementChance() > 0 && getElementChance() <= 100)
        		builder.append("%").append(getElementChance());
        	
        	builder.append("@ELEM@");
        }
        
        return builder.toString();
    }

    public void spawnIn() {
    	getSpawnedMonsters().stream().filter(Entity::isDead).forEach(getSpawnedMonsters()::remove);
    	
    	if (getSpawnedMonsters().size() >= getSpawnAmount() || !canSpawnMobs())
    		return;
    	
    	createMobs();
    	setFirstSpawn(false);
    	setCounter(0);
    }
    
    protected void createMobs() {
    	spawn();
    }
    
    protected boolean canSpawnMobs() {
    	setCounter(getCounter() + 1);
    	return isFirstSpawn() || getCounter() >= getRespawnDelay();
    }

    public abstract void init();

    public void kill() {
    	getSpawnedMonsters().forEach(Entity::remove);
    	getSpawnedMonsters().clear();
    	
    	getArmorStand().remove();
    }

    public void remove() {
        kill();
        Bukkit.getScheduler().cancelTask(getTimerID());

        for (String spawner : Lists.newArrayList(SpawningMechanics.SPAWNER_CONFIG)) {
            if(doesLineMatchLocation(spawner)){
                //Remove that whole line..
                SpawningMechanics.SPAWNER_CONFIG.remove(spawner);
                DungeonRealms.getInstance().getConfig().set("spawners", SpawningMechanics.SPAWNER_CONFIG);
                DungeonRealms.getInstance().saveConfig();
                Bukkit.getLogger().info("Removing spawner line: " + spawner);
                break;
            }
        }
    }
    
    protected Entity spawn() {
    	Location spawn = spray();
    	
    	if (GameAPI.isInSafeRegion(spawn))
    		return null;
    	
    	if (getMonsterType() == null) {
    		Utils.log.info("Could not spawn non-existant monster-type.");
    		return null;
    	}
    	
    	Entity entity;
    	int level = Utils.getRandomFromTier(getTier(), getLvlRange());
    	if (this instanceof EliteMobSpawner) {
    		entity = EntityAPI.spawnElite(getLocation(), ((EliteMobSpawner)this).getEliteType(), getMonsterType(), getTier(), level, getCustomName(), false);
    	} else {
    		entity = EntityAPI.spawnCustomMonster(getLocation(), getMonsterType(), level, getTier(), getWeaponType(), getCustomName());
    	}
    	getSpawnedMonsters().add(entity);
    	return entity;
    }
    
    protected void setTimer(Runnable r, int delay) {
    	Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> {
    		if (getTimerID() == -1)
    			setTimerID(Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), r, 0L, (long) delay).getTaskId());
    	}, 0L, 40L);
    }

    private boolean doesLineMatchLocation(String line){
        if(!line.contains("="))return false;
        String[] coords = line.split("=")[0].split(",");

        double x = Double.parseDouble(coords[0]);
        double y = Double.parseDouble(coords[1]);
        double z = Double.parseDouble(coords[2]);

        return getLocation().getX() == x && getLocation().getY() == y && getLocation().getZ() == z;
    }
    
    public Location spray() {
    	Location loc = getLocation();
    	double bound = getMinimumXZ() + getMaximumXZ();
    	double xMin = loc.getX() - bound;
    	double xMax = loc.getX() + bound;
    	double zMin = loc.getZ() - bound;
    	double zMax = loc.getZ() + bound;
    	
    	// Add 0.5 so they spawn in the middle of the block.
        double x = xMin + (int) (Math.random() * (xMax - xMin + 1)) + 0.5D;
        double y = loc.getY() + 3D;
        double z = zMin + (int) (Math.random() * (zMax - zMin + 1)) + 0.5D;

        // Raise mobs out of ground if they spawn in it.
        Location ret = new Location(loc.getWorld(), x, y, z);
        while (ret.getBlock().getType() != Material.AIR && ret.getY() < 250)
        	ret.add(0, 1, 0);
        
        return ret;
    }
}
