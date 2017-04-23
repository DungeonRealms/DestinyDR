package net.dungeonrealms.game.world.entity.util;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.type.pet.CreeperPet;
import net.dungeonrealms.game.world.entity.type.pet.EnumPets;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EntitySlime;
import net.minecraft.server.v1_9_R2.PathEntity;
import net.minecraft.server.v1_9_R2.PathfinderGoal;
import net.minecraft.server.v1_9_R2.PathfinderGoalFloat;
import net.minecraft.server.v1_9_R2.PathfinderGoalSelector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * PetUtils - Player pet mechanics.
 * 
 * Redone on April 22nd, 2017.
 * @author Kneesnap
 */
public class PetUtils implements GenericMechanic{

	@Getter
    private static PetUtils instance = new PetUtils();

    private static Field gsa;
    private static Field goalSelector;
    private static Field targetSelector;
    
    @Getter
    private static Map<Player, Entity> pets = new HashMap<>();

    static {
        try {
            gsa = PathfinderGoalSelector.class.getDeclaredField("b");
            gsa.setAccessible(true);
            goalSelector = EntityInsentient.class.getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);
            targetSelector = EntityInsentient.class.getDeclaredField("targetSelector");
            targetSelector.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }

    @Override
	public void startInitialization() {
    	// Teleports pets to players if they're too far away.
    	Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), () -> {
    		Map<Player, Entity> temp = new HashMap<>(getPets());
    		for (Player p : temp.keySet()) {
    			if (!p.isOnline()) {
    				removePet(p);
    				continue;
    			}
    			
    			Entity pet = temp.get(p);
    			if (p.getLocation().distance(pet.getLocation()) > 20 && !p.isFlying())
    				Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> pet.teleport(p));
    			
    		}
    	}, 100L, 100L);
    	
    	Bukkit.getScheduler().runTaskTimerAsynchronously(DungeonRealms.getInstance(), this::creeperEffects, 40L, 100L);
    }
    
    private void creeperEffects() {
        FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.BLUE, Color.RED, Color.WHITE).withFade(Color.BLUE, Color.RED, Color.WHITE).with(FireworkEffect.Type.STAR).trail(true).build();
        
        for (Entity pet : getPets().values()) {
        	if (pet == null || pet.isDead() || !(pet instanceof CreeperPet))
        		continue;
        	
        	// Spawn firework
        	Bukkit.getScheduler().runTask(DungeonRealms.getInstance(), () -> {
        		Firework fw = (Firework) pet.getWorld().spawnEntity(pet.getLocation(), EntityType.FIREWORK);
            	FireworkMeta fwm = fw.getFireworkMeta();
            	fwm.addEffect(effect);
            	fwm.setPower(1); // 0.5 seconds
            	fw.setFireworkMeta(fwm);
        	});
        }
    }

    @Override
    public void stopInvocation() {

    }

    /**
     * Gives the entity an AI to follow the player.
     */
    public static void givePetAI(org.bukkit.entity.Entity entity, Player follow, EnumPets petType) {
    	EntityInsentient e = (EntityInsentient) ((CraftEntity)entity).getHandle();
        try {
            if (e instanceof EntityInsentient) {
            	// Register Pet follow AIs.
            	PathfinderGoalSelector goal = (PathfinderGoalSelector) goalSelector.get(e);
            	goal.a(0, new PathfinderGoalFloat(e));
                if (petType != EnumPets.SLIME && petType != EnumPets.MAGMA_CUBE) {
                    goal.a(1, new PathfinderGoalWalkToTile(e, follow, petType.getFollowSpeed()));
                } else {
                    goal.a(1, new PathfinderGoalSlimeFollowOwner(e, follow, petType.getFollowSpeed()));
                }
            } else {
                throw new IllegalArgumentException(e.getCustomName() + " is not an instance of an EntityInsentient.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static class PathfinderGoalWalkToTile extends PathfinderGoal {
        private EntityInsentient entity;
        private PathEntity path;
        private Player owner;
        private double speed;

        PathfinderGoalWalkToTile(EntityInsentient entity, Player owner, double speed) {
            this.entity = entity;
            this.owner = owner;
            this.speed = speed;
        }

        @Override
        public boolean a() {
            if (this.owner == null)
                return path != null;
            
            Location targetLocation = this.owner.getLocation();

            this.entity.getNavigation();
            this.path = this.entity.getNavigation().a(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ() + 1);
            this.entity.getNavigation();
            
            boolean walk = this.path != null && this.entity.getBukkitEntity().getLocation().distance(targetLocation) >= 6;
            if (walk)
            	this.c();
            
            return walk;
        }

        @Override
        public void c() {
            this.entity.getNavigation().a(this.path, speed);
        }
    }

    private static class PathfinderGoalSlimeFollowOwner extends PathfinderGoal {
        private EntityInsentient entity;
        private double speed;
        private static Method controllerRotate;
        private Player owner;

        static {
            try {
                controllerRotate = EntitySlime.class.getDeclaredClasses()[0].getDeclaredMethod("a", float.class, boolean.class);
                controllerRotate.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PathfinderGoalSlimeFollowOwner(EntityInsentient entity, Player owner, double speed) {
            this.entity = entity;
            this.speed = speed;
            this.owner = owner;
            this.a(3);
        }

        @Override
        public boolean a() {
            return (owner != null && !owner.isDead() && this.entity.getBukkitEntity().getLocation().distanceSquared(owner.getLocation()) >= 6);
        }

        @Override
        public void d() {
            o();
        }

        private boolean o() {
            return true;
        }

        @Override
        public void e() {
            EntityPlayer owner = ((CraftPlayer) this.owner).getHandle();
            this.entity.a(owner, 10.0F, 10.0F);
            if (this.entity.getBukkitEntity().getLocation().distanceSquared(this.owner.getLocation()) >= 6) {
                try {
                    controllerRotate.invoke(this.entity.getControllerMove(), this.entity.yaw, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.entity.getNavigation().a(owner, this.speed);
            }
        }
    }

    /**
     * Spawns a pet.
     */
    public static void spawnPet(Player player, EnumPets pet, String petName) {
    	if (!GameAPI.isMainWorld(player)) {
    		player.sendMessage(ChatColor.RED + "Your pet is at home, in Andalucia.");
    		return;
    	}
    	
    	// Apply color prefix.
    	petName = Rank.colorFromRank(Rank.getInstance().getRank(player.getUniqueId())) + petName;
    	
    	// Spawns the pet.
    	getPets().put(player, pet.create(player, petName).getBukkitEntity());
    }
    
    public static boolean hasActivePet(Player p) {
    	return getPets().containsKey(p);
    }
    
    public static void removePet(Player p) {
    	if (!hasActivePet(p))
    		return;
    	Entity pet = getPets().get(p);
    	pet.remove();
    	getPets().remove(p);
    }
}
