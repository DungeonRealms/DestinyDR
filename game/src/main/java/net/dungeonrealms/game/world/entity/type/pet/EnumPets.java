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
    WOLF(WolfPet.class, EntityType.WOLF.getTypeId(), Sound.ENTITY_WOLF_AMBIENT, 1.1D),
    ENDERMITE(EndermitePet.class, EntityType.ENDERMITE.getTypeId(), Sound.ENTITY_ENDERMITE_AMBIENT, 1.5D),
    CAVE_SPIDER(SpiderPet.class, EntityType.CAVE_SPIDER.getTypeId(), Sound.ENTITY_SPIDER_AMBIENT, 1.3D),
    BABY_ZOMBIE(ZombiePet.class, EntityType.ZOMBIE.getTypeId(), Sound.ENTITY_ZOMBIE_AMBIENT, 1D),
    BABY_PIG_ZOMBIE(ZombiePigPet.class, EntityType.PIG_ZOMBIE.getTypeId(), Sound.ENTITY_ZOMBIE_PIG_AMBIENT, 1D),
    OCELOT(OcelotPet.class, EntityType.OCELOT.getTypeId(), Sound.ENTITY_CAT_AMBIENT, 1D),
    RABBIT(RabbitPet.class, EntityType.RABBIT.getTypeId(), Sound.ENTITY_RABBIT_AMBIENT, 1.5D), //.4F
    CHICKEN(ChickenPet.class, EntityType.CHICKEN.getTypeId(), Sound.ENTITY_CHICKEN_AMBIENT, 1.1D),
    BAT(BatPet.class, EntityType.BAT.getTypeId(), Sound.ENTITY_BAT_TAKEOFF, -1D),
    SLIME(SlimePet.class, EntityType.SLIME.getTypeId(), Sound.ENTITY_SLIME_SQUISH, 1.25D),
    MAGMA_CUBE(MagmaPet.class, EntityType.MAGMA_CUBE.getTypeId(), Sound.ENTITY_MAGMACUBE_SQUISH, 1.25D),
    ENDERMAN(EndermanPet.class, EntityType.ENDERMAN.getTypeId(), Sound.ENTITY_ENDERMEN_SCREAM, 1D, false), //.45F
    GUARDIAN(GuardianPet.class, EntityType.GUARDIAN.getTypeId(), Sound.ENTITY_GUARDIAN_AMBIENT, 1D, false), //.3F
    BABY_SHEEP(BabySheepPet.class, EntityType.SHEEP.getTypeId(), Sound.ENTITY_SHEEP_AMBIENT, 1D), //.45F
    RAINBOW_SHEEP(RainbowSheepPet.class, EntityType.SHEEP.getTypeId(), Sound.ENTITY_SHEEP_AMBIENT, 1.0D, false), //.45F
    BETA_ZOMBIE(BetaZombie.class, EntityType.ZOMBIE.getTypeId(), Sound.ENTITY_ZOMBIE_AMBIENT, 1D, false),

    // Event Pets:
    SILVERFISH(SilverfishPet.class, EntityType.SILVERFISH.getTypeId(), Sound.ENTITY_SILVERFISH_AMBIENT, 1.5D, false),
    SNOWMAN(SnowmanPet.class, EntityType.SNOWMAN.getTypeId(), Sound.ENTITY_SNOWMAN_AMBIENT, 1.8D, false), // Christmass
    INDEPENDENCE_CREEPER(CreeperPet.class, EntityType.CREEPER.getTypeId(), Sound.ENTITY_CREEPER_PRIMED, 1.25D, false), //Fourth of July.

    // Special "Pets"
    STORAGE_MULE(null, 64, null, 1.8D, null, false, false, true);

    private Class<? extends EntityInsentient> clazz;
    private int eggShortData;
    private Sound sound;
    private double followSpeed;
    @Getter
    private String description;
    private boolean subGetsFree;
    private boolean showInGui; //Pets we haven't released yet, or are disabled.
    private boolean special;

    EnumPets(Class<? extends EntityInsentient> cls, int eggData, Sound sound, double followSpeed) {
        this(cls, eggData, sound, followSpeed, true);
    }

    EnumPets(Class<? extends EntityInsentient> cls, int eggData, Sound sound, double followSpeed, boolean subFree) {
        this(cls, eggData, sound, followSpeed, subFree, true);
    }

    EnumPets(Class<? extends EntityInsentient> cls, int eggData, String description, Sound sound, double followSpeed, boolean subFree, boolean showGUI) {
        this(cls, eggData, sound, followSpeed, description, subFree, showGUI, false);
    }

    EnumPets(Class<? extends EntityInsentient> cls, int eggData, Sound sound, double followSpeed, boolean subFree, boolean showGUI) {
        this(cls, eggData, sound, followSpeed, null, subFree, showGUI, false);
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
            if (getFollowSpeed() >= 0)
                PetUtils.givePetAI(e, player, this);
            return pet;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().warning("Failed to create pet " + name() + ".");
            player.sendMessage(ChatColor.RED + "There was an error loading your pet.");
            if (!Constants.debug)
                GameAPI.sendNetworkMessage("DEVMessage", ChatColor.RED + "[ALERT] " + ChatColor.WHITE + "Failed to load " + player.getName() + "'s pet " + name() + ".");
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
