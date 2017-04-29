package net.dungeonrealms.game.world.entity.util;

import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mastery.ItemSerialization;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.dungeonrealms.game.mechanic.data.MuleTier;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMountSkins;
import net.dungeonrealms.game.world.entity.type.mounts.EnumMounts;
import net.dungeonrealms.game.world.entity.type.mounts.JumpingMount;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.minecraft.server.v1_9_R2.EntityHuman;
import net.minecraft.server.v1_9_R2.EntityLiving;
import net.minecraft.server.v1_9_R2.GenericAttributes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Horse.Color;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MountUtils - Handles basic mount utilities
 * 
 * Created April 22nd, 2017.
 * @author Kneesnap
 */
public class MountUtils {

    public static ConcurrentHashMap<UUID, Inventory> inventories = new ConcurrentHashMap<>();
    
    @Getter
    private static Map<Player, Entity> mounts = new HashMap<>();

    public static boolean hasMountPrerequisites(EnumMounts mountType, List<String> playerMounts) {
    	if (mountType == EnumMounts.TIER1_HORSE || !mountType.name().contains("HORSE"))
    		return true;
    	
    	return !playerMounts.isEmpty() && playerMounts.contains(EnumMounts.getById(mountType.getId() - 1).name());
    }

    public static void spawnMount(Player player, EnumMounts mount, EnumMountSkins skin) {
    	if (!GameAPI.isMainWorld(player.getLocation())) {
    		player.sendMessage(ChatColor.RED + "Your mount is too scared by foreign land to step foot here.");
    		return;
    	}
    	
    	if (mount == EnumMounts.MULE) {
    		// Create Entity
    		Horse mule = player.getWorld().spawn(player.getLocation(), Horse.class);
    		mule.setAdult();
    		mule.setAgeLock(true);
    		mule.setVariant(Variant.MULE);
    		mule.setCarryingChest(true);
    		mule.setTamed(true);
    		mule.setOwner(player);
    		mule.setColor(Color.BROWN);
    		MetadataUtils.registerEntityMetadata(mule, EnumEntityType.MULE);
    		
    		// Create Inventory.
    		String inv = (String) DatabaseAPI.getInstance().getData(EnumData.INVENTORY_MULE, player.getUniqueId());
    		int muleLevel = Math.min((int) DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, player.getUniqueId()), MuleTier.values().length);
    		MuleTier tier = MuleTier.getByTier(muleLevel);
    		
    		mule.setCustomName(tier.getColor() + player.getName() + "'s " + tier.getName());
    		mule.setCustomNameVisible(true);
    		player.closeInventory();
    		player.playSound(player.getLocation(), Sound.ENTITY_DONKEY_AMBIENT, 1F, 1F);
    		PetUtils.givePetAI(mule, player, EnumPets.STORAGE_MULE);
    		getMounts().put(player, mule);
    		
    		if (!inventories.containsKey(player.getUniqueId())) {
    			Inventory inventory = Bukkit.createInventory(player, tier.getSize(), "Mule Storage");
    			if (inv != null && !inv.equals("") && !inv.equals("empty") && !inv.equals("null"))
    				inventory = ItemSerialization.fromString(inv, tier.getSize());
    			inventories.put(player.getUniqueId(), inventory);
    		}
    		
    		return;
    	}
    	
    	Horse.Color color = Rank.isDev(player) ? Horse.Color.WHITE : Horse.Color.BROWN;
    	
    	Entity mountEnt = mount.create(player, skin);
    	
    	if (mountEnt instanceof Horse) {
    		Horse horse = (Horse) mountEnt;
        	horse.setColor(color);
        	HorseInventory horseInventory = horse.getInventory();
        	HorseTier tier = HorseTier.getByMount(mount);
        	if (tier.getTier() > 1)
        		horse.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, tier.getTier() - 1));
        	horseInventory.setSaddle(new ItemStack(Material.SADDLE));
        	horseInventory.setArmor(new ItemStack(tier.getArmor()));
        	player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1F, 1F);
    	}
    	
    	if (mountEnt != null) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
            getMounts().put(player, mountEnt);
            player.closeInventory();
        }
    	
    	return;
    }
    
    /**
     * Handles custom mount tick logic.
     */
    public static boolean handleMountLogic(EntityLiving mount, Player owner) {
    	if (!mount.isVehicle() || mount.passengers.isEmpty() || !(mount.passengers.get(0) instanceof EntityHuman)) {
    		removeMount(owner);
    		return false;
    	}
    	
    	// Sync where the mount is looking with where the player is looking.
    	EntityHuman rider = (EntityHuman) mount.passengers.get(0);
    	mount.lastYaw = mount.yaw = rider.yaw;
    	mount.pitch = rider.pitch * 0.5F;
    	mount.yaw = mount.yaw % 360;
    	mount.pitch = mount.pitch % 360;
    	mount.aP = mount.aN = mount.yaw; // Set head rotation yaw.
    	
    	
    	float sideMotion = rider.be * 0.5F; // Set the side motion to be the player's strafe motion.
    	float forwardMotion = rider.bf; // Set forward motion to be the player's move forward motion.
    	if (forwardMotion < 0.0)
    		forwardMotion *= 0.25F;
    	
    	// Handle jumping.
        try {
            Field jump = EntityLiving.class.getDeclaredField("bd");
            
            if (jump != null && mount.onGround) { //Is someone jumping and still on the ground?
            	jump.setAccessible(true);
            	
            	if (jump.getBoolean(mount) && mount instanceof JumpingMount)
            		((JumpingMount)mount).customJump(); //Entity has jumped.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        mount.P = 1; // Set step height.
        mount.aR = mount.cl() * 0.1F; //Set jump movement factor to the land movement factor / 10
        
        mount.l((float) mount.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue()); // Set the land movement factor to the movement speed.
        
        // Handle vanilla movement.
        // We have to call the superclass method otherwise we'll deadlock.
        // Since there isn't anything like mount.super.g(sideMotion, forwardMotion);
        // We gotta use reflection or an interface all mounts implement. this sadly is cleaner.
        try {
        	Method m = mount.getClass().getSuperclass().getDeclaredMethod("g", Float.class, Float.class);
        	m.setAccessible(true);
        	m.invoke(mount, sideMotion, forwardMotion);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return true;
    }
    
    public static boolean hasActiveMount(Player p) {
    	return getMounts().containsKey(p);
    }
    
    public static void removeMount(Player p) {
    	if (!hasActiveMount(p))
    		return;
    	Entity mount = getMounts().get(p);
    	if (mount.getPassenger() == p)
    		p.eject();
    	mount.remove();
    	//TODO: Save?
    	getMounts().remove(p);
    }
}
