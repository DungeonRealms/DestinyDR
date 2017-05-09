package net.dungeonrealms.game.world.entity.type.mounts;

import lombok.Getter;
import net.dungeonrealms.game.mastery.MetadataUtils.Metadata;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.mechanic.data.HorseTier;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * EnumMounts - A registry of all mounts.
 * 
 * Please keep horses as the first values.
 */
@Getter
public enum EnumMounts {
    TIER1_HORSE(HorseMount.class, EntityType.HORSE, new ItemStack(Material.SADDLE, 1), 0, "Old Horse"),
    TIER2_HORSE(HorseMount.class, EntityType.HORSE, new ItemStack(Material.IRON_BARDING, 1), 0, "Squire's Horse"),
    TIER3_HORSE(HorseMount.class, EntityType.HORSE, new ItemStack(Material.DIAMOND_BARDING, 1), 0, "Traveler's Horse"),
    TIER4_HORSE(HorseMount.class, EntityType.HORSE, new ItemStack(Material.GOLD_BARDING, 1), 0, "Knight's Horse"),
    MULE(null, EntityType.HORSE, new ItemStack(Material.CHEST, 1), 0, "Mule"), //This is a bukkit entity, not a custom one. It has a special case :( in MountUtils

    SPIDER(SpiderMount.class, EntityType.SPIDER, new ItemStack(Material.STRING, 1), 0, "Spider Mount",
    		new MountData("Spider", ChatColor.LIGHT_PURPLE, 0.31F, 190), 5),
    
    WOLF(WolfMount.class, EntityType.WOLF, new ItemStack(Material.BONE, 1), 0, "Wolf Mount",
            new MountData("Wolf", ChatColor.WHITE, 0.245F, 140, "A ferocious beast, said to have", "slept at the side of Mayel The Cruel."), 5),
                            
    SLIME(SlimeMount.class, EntityType.SLIME, new ItemStack(Material.SLIME_BALL, 1), 0, "Slime Mount",
    		new MountData("Slime", ChatColor.GREEN, 0.35F, 170, "A quick slime found deep", "in the Varenglade Ruins"), 3);
                    
    private Class<? extends EntityInsentient> clazz;
    private ItemStack selectionItem;
    private int entityId;
    private short shortID;
    private String displayName;
    private int chance;
    private MountData mountData;
    
    @SuppressWarnings("deprecation")
	EnumMounts(Class<? extends EntityInsentient> cls, EntityType t, ItemStack selectionItem, int shortID, String displayName) {
    	this.clazz = cls;
        this.selectionItem = selectionItem;
        this.shortID = (short) shortID;
        this.displayName = displayName;
        this.entityId = t.getTypeId();
        
    }

    EnumMounts(Class<? extends EntityInsentient> e, EntityType t, ItemStack selectionItem, int shortID, String displayName, MountData data, int chance) {
	    this(e, t, selectionItem, shortID, displayName);
        this.mountData = data;
        this.chance = chance;
    }

    public static EnumMounts getById(int id) {
        return EnumMounts.values()[id];
    }

    public static EnumMounts getByName(String rawName) {
        for (EnumMounts em : values())
            if (em.name().equalsIgnoreCase(rawName))
                return em;
        return null;
    }
    
    public int getId() {
    	return ordinal();
    }
    
    @SuppressWarnings("unchecked")
	public Class<? extends EntityInsentient> getNMSSuperClass() {
    	return (Class<? extends EntityInsentient>) getClazz().getSuperclass();
    }
    
    public boolean isFrame() {
    	return this.getClazz() == null;
    }
    
    public boolean shouldRegister() {
    	for (int i = ordinal() + 1; i < values().length; i++)
    		if (values()[i].getClazz() == getClazz())
    			return false;
    	return !isFrame();
    }
    
    public float getSpeed() {
    	return getMountData() != null ? getMountData().getSpeed() : HorseTier.getByMount(this).getRawSpeed();
    }
    
    public org.bukkit.entity.Entity create(Player player) {
    	return create(player, null);
    }
    
    public org.bukkit.entity.Entity create(Player player, EnumMountSkins horseSkin) {
    	assert !isFrame();
    	World world = ((CraftWorld)player.getWorld()).getHandle();
    	
    	try {
    		EntityInsentient e = getClazz().getDeclaredConstructor(world.getClass(), Player.class, EnumMounts.class).newInstance(world, player, this);
    		e.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(getSpeed());
    		
    		Location l = player.getLocation();
    		e.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);
    		world.addEntity(e, CreatureSpawnEvent.SpawnReason.CUSTOM);
    		e.setLocation(l.getX(), l.getY(), l.getZ(), 0, 0);
    		
    		e.getBukkitEntity().setPassenger(player);
            e.setCustomName(player.getName());
            e.setCustomNameVisible(true);
            
            if (e instanceof HorseMount && horseSkin != null)
            	((HorseMount)e).setSkin(horseSkin);
            
            org.bukkit.entity.Entity entity = e.getBukkitEntity();
            
            Metadata.ENTITY_TYPE.set(entity, EnumEntityType.MOUNT);
            Metadata.MOUNT.set(entity, this);
            Metadata.OWNER.set(entity, player.getUniqueId().toString());
            
    		return entity;
    	} catch(Exception e) {
    		e.printStackTrace();
    		Bukkit.getLogger().warning("Failed to construct mount " + name() + "!");
    	}
    	return null;
    }
}
