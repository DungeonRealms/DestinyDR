package net.dungeonrealms.game.world.entity.type.pet;

import java.lang.reflect.Constructor;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import lombok.Getter;

/**
 * EnumPets - A registry of pets.
 * 
 * Redone April 22nd, 2017.
 * @author Kneesnap
 */
@Getter
public enum EnumPets {
    //Snowman - Christmas
    //Killer Rabbit - Easter
    //Green Baby Sheep - St.Patrick's
    //Primed Creeper - 4th of July
    //Cave Spider - Halloween
    //Blue Baby Sheep - St.Andrew's
    //Magma Cube - Guy Fawkes/Bonfire Night
    //Pink Baby Sheep - Cancer Awareness (Maybe different for Breast/Lung etc based on the ribbons)
    //Pig (In Love) - Valentines Day
    //Adult Chicken - Thanksgiving
    WOLF(WolfPet.class, 95, "Wolf", Sound.ENTITY_WOLF_AMBIENT, 1.1D),
    ENDERMITE(EndermitePet.class, 67, "Endermite", Sound.ENTITY_ENDERMITE_AMBIENT, 1.5D),
    CAVE_SPIDER(SpiderPet.class, 59, "Cave Spider", Sound.ENTITY_SPIDER_AMBIENT, 1.3D),
    BABY_ZOMBIE(ZombiePet.class, 54, "Baby Zombie", Sound.ENTITY_ZOMBIE_AMBIENT, 1D),
    BABY_PIGZOMBIE(ZombiePigPet.class, 57, "Baby Pig Zombie", Sound.ENTITY_ZOMBIE_PIG_AMBIENT, 1D),
    OCELOT(OcelotPet.class, 98, "Ocelot", Sound.ENTITY_CAT_AMBIENT, 1D),
    RABBIT(RabbitPet.class, 101, "Rabbit", Sound.ENTITY_RABBIT_AMBIENT, 0.9D),
    CHICKEN(ChickenPet.class, 93, "Chicken", Sound.ENTITY_CHICKEN_AMBIENT, 1.1D),
    BAT(BatPet.class, 65, "Bat", Sound.ENTITY_BAT_TAKEOFF, -1D),
    SLIME(SlimePet.class, 55, "Slime", Sound.ENTITY_SLIME_SQUISH, 1.25D),
    MAGMA_CUBE(MagmaPet.class, 62, "Magma Cube", Sound.ENTITY_MAGMACUBE_SQUISH, 1.25D),
    
    // Event Pets:
    SILVERFISH(SilverfishPet.class, 60, "Silverfish", Sound.ENTITY_SILVERFISH_AMBIENT, 1.5D, true),
    SNOWMAN(SnowmanPet.class, 56, "Snowman", Sound.ENTITY_SNOWMAN_AMBIENT, 1.8D, true),
    CREEPER_OF_INDEPENDENCE(CreeperPet.class, 50, "Independence Creeper", Sound.ENTITY_CREEPER_PRIMED, 1.25D, true),
    STORAGE_MULE(null, 64, "Baby Horse", null, 1.8D, true);

    private Class<? extends EntityInsentient> clazz;
    private int eggShortData;
    private String displayName;
    private Sound sound;
    private double followSpeed;
    private boolean special;
    
    EnumPets(Class<? extends EntityInsentient> cls, int eggData, String display, Sound sound, double followSpeed) {
    	this(cls, eggData, display, sound, followSpeed, false);
    }

    EnumPets(Class<? extends EntityInsentient> cls, int eggShortData, String displayName, Sound sound, double followSpeed, boolean event) {
    	this.clazz = cls;
        this.eggShortData = eggShortData;
        this.displayName = displayName;
        this.sound = sound;
        this.followSpeed = followSpeed;
        this.special = event;
    }
    
    public String getName() {
    	return name().replaceAll("_", "");
    }
    
    public int getId() {
    	return ordinal();
    }
    
    @SuppressWarnings("unchecked")
	public Class<? extends EntityInsentient> getNMSSuperClass() {
    	return (Class<? extends EntityInsentient>) getClazz().getSuperclass();
    }
    
    /**
     * Does this represent a bukkit entity with no custom class?
     * Example: STORAGE_MULE
     * @return
     */
    public boolean isFrame() {
    	return getClazz() == null;
    }
    
    /**
     * Create an instance of this pet.
     * Please use PetUtils#spawnPet instead.
     */
    public Entity create(Player player, String petName) {
    	assert !isFrame();
    	player.closeInventory();
    	try {
    		World world = ((CraftWorld)player.getWorld()).getHandle();
    		Entity pet;
    		
    		// Construct entity.
    		Constructor<? extends Entity> constructor = getClazz().getDeclaredConstructor(World.class, Player.class);
    		if (constructor == null) {
    			pet = getClazz().getDeclaredConstructor(World.class).newInstance(world);
    		} else {
    			pet = constructor.newInstance(world, player);
    		}
    		
    		if (pet instanceof EntityInsentient) {
    			EntityInsentient ie = (EntityInsentient) pet;
    			ie.cM(); //Sets this entity as persistent. Equivalent to doing this.persistent = true; in the entity constructor.
    			ie.l(false); //Sets this entity as unable to pickup items. Equivalent to doing this.canPickUpLoot in the constructor.
    		}
    		
    		// Set entity data.
    		org.bukkit.entity.Entity e = pet.getBukkitEntity();
    		e.setCustomName(petName);
    		e.setCustomNameVisible(true);
    		MetadataUtils.registerEntityMetadata(e, EnumEntityType.PET);
    		
    		// Spawn into world.
    		Location l = player.getLocation();
    		pet.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);
    		world.addEntity(pet, SpawnReason.CUSTOM);
    		pet.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);
    		
    		// Give correct AI and add to pet list.
    		player.playSound(player.getLocation(), getSound(), 1F, 1F);
    		if (getFollowSpeed() >= 0)
    			PetUtils.givePetAI(e, player, this);
    		return pet;
    	} catch (Exception e) {
    		e.printStackTrace();
    		Bukkit.getLogger().warning("Failed to create Pet " + name() + ".");
    		player.sendMessage(ChatColor.RED + "There was an error loading your pet.");
    		GameAPI.sendNetworkMessage("DEVMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Failed to load " + player.getName() + "'s pet " + name() + ".");
    	}
    	return null;
    }

    public static EnumPets getById(int id) {
        for (EnumPets ep : values())
            if (ep.getId() == id)
                return ep;
        return null;
    }

    public static EnumPets getByName(String rawName) {
    	rawName = rawName.replaceAll("_", "");
        for (EnumPets ep : values())
            if (ep.getName().equalsIgnoreCase(rawName))
                return ep;
        return null;
    }
}
