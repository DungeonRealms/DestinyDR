package net.dungeonrealms.game.world.entity.type.pet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Constants;
import net.dungeonrealms.game.mastery.MetadataUtils;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.util.PetUtils;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.Entity;
import net.minecraft.server.v1_9_R2.EntityInsentient;
import net.minecraft.server.v1_9_R2.GenericAttributes;
import net.minecraft.server.v1_9_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.lang.reflect.Constructor;

/**
 * EnumPets - A registry of pets.
 * <p>
 * Redone April 22nd, 2017.
 *
 * @author Kneesnap
 */
@AllArgsConstructor
@Getter
public enum EnumPets {
    WOLF(WolfPet.class, EntityType.WOLF, Sound.ENTITY_WOLF_AMBIENT),
    ENDERMITE(EndermitePet.class, EntityType.ENDERMITE, Sound.ENTITY_ENDERMITE_AMBIENT),
    CAVE_SPIDER(SpiderPet.class, EntityType.CAVE_SPIDER, Sound.ENTITY_SPIDER_AMBIENT),
    BABY_ZOMBIE(ZombiePet.class, EntityType.ZOMBIE, Sound.ENTITY_ZOMBIE_AMBIENT),
    BABY_PIG_ZOMBIE(ZombiePigPet.class, EntityType.PIG_ZOMBIE, Sound.ENTITY_ZOMBIE_PIG_AMBIENT),
    OCELOT(OcelotPet.class, EntityType.OCELOT, Sound.ENTITY_CAT_AMBIENT),
    RABBIT(RabbitPet.class, EntityType.RABBIT, Sound.ENTITY_RABBIT_AMBIENT, .4F),
    CHICKEN(ChickenPet.class, EntityType.CHICKEN, Sound.ENTITY_CHICKEN_AMBIENT),
    BAT(BatPet.class, EntityType.BAT, Sound.ENTITY_BAT_TAKEOFF),
    SLIME(SlimePet.class, EntityType.SLIME, Sound.ENTITY_SLIME_SQUISH),
    MAGMA_CUBE(MagmaPet.class, EntityType.MAGMA_CUBE, Sound.ENTITY_MAGMACUBE_SQUISH),
    ENDERMAN(EndermanPet.class, EntityType.ENDERMAN, Sound.ENTITY_ENDERMEN_SCREAM, false),
    GUARDIAN(GuardianPet.class, EntityType.GUARDIAN, Sound.ENTITY_GUARDIAN_AMBIENT, .3F, false),
    BABY_SHEEP(BabySheepPet.class, EntityType.SHEEP, Sound.ENTITY_SHEEP_AMBIENT),
    RAINBOW_SHEEP(RainbowSheepPet.class, EntityType.SHEEP, Sound.ENTITY_SHEEP_AMBIENT, false),
    BETA_ZOMBIE(BetaZombie.class, EntityType.ZOMBIE, Sound.ENTITY_ZOMBIE_AMBIENT,.3F, false),

    // Event Pets:
    SILVERFISH(SilverfishPet.class, EntityType.SILVERFISH, Sound.ENTITY_SILVERFISH_AMBIENT, false),
    SNOWMAN(SnowmanPet.class, EntityType.SNOWMAN, Sound.ENTITY_SNOWMAN_AMBIENT, false), // Christmass
    INDEPENDENCE_CREEPER(CreeperPet.class, EntityType.CREEPER, Sound.ENTITY_CREEPER_PRIMED, false), //Fourth of July.

    // Special "Pets"
    STORAGE_MULE(null, EntityType.HORSE, null, .55F, null, false, false, true);

    private Class<? extends EntityInsentient> clazz;
    private EntityType entityType;
    private Sound sound;
    private float followSpeed;
    private String description;
    private boolean subGetsFree;
    private boolean showInGui; //Pets we haven't released yet, or are disabled.
    private boolean special;
    
    EnumPets(Class<? extends EntityInsentient> cls, EntityType t, Sound sound) {
        this(cls, t, sound, .45F);
    }

    EnumPets(Class<? extends EntityInsentient> cls, EntityType t, Sound sound, float followSpeed) {
        this(cls, t, sound, followSpeed, true);
    }
    
    EnumPets(Class<? extends EntityInsentient> cls, EntityType t, Sound sound, boolean subFree) {
        this(cls, t, sound, .45F, subFree, true);
    }

    EnumPets(Class<? extends EntityInsentient> cls, EntityType t, Sound sound, float followSpeed, boolean subFree) {
        this(cls, t, sound, followSpeed, subFree, true);
    }

    EnumPets(Class<? extends EntityInsentient> cls, EntityType t, String description, Sound sound, float followSpeed, boolean subFree, boolean showGUI) {
        this(cls, t, sound, followSpeed, description, subFree, showGUI, false);
    }

    EnumPets(Class<? extends EntityInsentient> cls, EntityType t, Sound sound, float followSpeed, boolean subFree, boolean showGUI) {
        this(cls, t, sound, followSpeed, null, subFree, showGUI, false);
    }

    public String getDisplayName() {
        return Utils.capitalizeWords(getName());
    }

    public String getName() {
        return name().replaceAll("_", " ");
    }

    public int getId() {
        return ordinal();
    }

    @SuppressWarnings("unchecked")
    public Class<? extends EntityInsentient> getNMSSuperClass() {
        return (Class<? extends EntityInsentient>) getClazz().getSuperclass();
    }
    
    /**
     * Get the mob entity id for registration.
     */
    @SuppressWarnings("deprecation")
	public int getEggShortData() {
    	return entityType.getTypeId();
    }

    /**
     * Does this represent a bukkit entity with no custom class?
     * Example: STORAGE_MULE
     *
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
        World world = ((CraftWorld) player.getWorld()).getHandle();
        Entity pet;

        // Construct entity.
        try {
            Constructor<? extends Entity> constructor;
            try {
                constructor = getClazz().getDeclaredConstructor(World.class, Player.class);
                pet = constructor.newInstance(world, player);
            } catch (NoSuchMethodException e) {
                pet = getClazz().getDeclaredConstructor(World.class).newInstance(world);
            }

            if (pet instanceof EntityInsentient) {
                EntityInsentient ie = (EntityInsentient) pet;
                ie.cM(); //Sets this entity as persistent. Equivalent to doing this.persistent = true; in the entity constructor.
                ie.l(false); //Sets this entity as unable to pickup items. Equivalent to doing this.canPickUpLoot in the constructor.
                ie.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(getFollowSpeed());
            }

            if (pet instanceof Ownable)
                ((Ownable) pet).setOwner(player);

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
            PetUtils.givePetAI(e, player, this);
            return pet;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to create pet " + name() + ".");
            player.sendMessage(ChatColor.RED + "There was an error loading your pet.");
            GameAPI.sendError("Failed to load " + player.getName() + "'s pet on {SERVER}.");
        }
        return null;
    }

    public static EnumPets getByEntityId(int id) {
        for (EnumPets ep : values())
            if (ep.getEggShortData() == id)
                return ep;
        return null;
    }
    public static EnumPets getById(int id) {
        for (EnumPets ep : values())
            if (ep.getId() == id)
                return ep;
        return null;
    }

    public static EnumPets getByName(String rawName) {
        if (rawName == null) return null;
        for (EnumPets ep : values())
            if (ep.getName().equalsIgnoreCase(rawName.replaceAll("_", "")) || ep.name().equalsIgnoreCase(rawName))
                return ep;
        return null;
    }
}
