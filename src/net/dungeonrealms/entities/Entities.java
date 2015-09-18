package net.dungeonrealms.entities;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.types.*;
import net.dungeonrealms.enums.EnumEntityType;
import net.dungeonrealms.mastery.NMSUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class Entities {

    static Entities instance = null;
    public static HashMap<UUID, Entity> PLAYER_PETS = new HashMap<>();
    public static HashMap<UUID, Entity> PLAYER_MOUNTS = new HashMap<>();

    public static Entities getInstance() {
        if (instance == null) {
            return new Entities();
        }
        return instance;
    }

    public void startInitialization() {
        NMSUtils nmsUtils = new NMSUtils();

        nmsUtils.registerEntity("Pirate", 54, EntityZombie.class, EntityPirate.class);

        nmsUtils.registerEntity("PetCaveSpider", 59, EntityCaveSpider.class, CaveSpider.class);
        nmsUtils.registerEntity("PetBabyZombie", 54, EntityZombie.class, BabyZombie.class);
        nmsUtils.registerEntity("PetBabyZombiePig", 57, EntityPigZombie.class, BabyZombiePig.class);
        nmsUtils.registerEntity("PetWolf", 95, EntityWolf.class, Wolf.class);
        nmsUtils.registerEntity("PetChicken", 93, EntityChicken.class, Chicken.class);
        nmsUtils.registerEntity("PetOcelot", 98, EntityOcelot.class, Ocelot.class);
        nmsUtils.registerEntity("PetRabbit", 101, EntityRabbit.class, Rabbit.class);
        nmsUtils.registerEntity("PetSilverfish", 60, EntitySilverfish.class, Silverfish.class);
        nmsUtils.registerEntity("PetEndermite", 67, EntityEndermite.class, Endermite.class);
        nmsUtils.registerEntity("MountHorse", 100, EntityHorse.class, Horse.class);
    }

    public void registerEntityMetadata(Entity entity, EnumEntityType entityType, int entityTier, int level) {
        switch (entityType) {
            case PET: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "pet"));
                break;
            }
            case FRIENDLY_MOB: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "friendly"));
                entity.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), entityTier));
                entity.getBukkitEntity().setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
                break;
            }
            case MOUNT: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "mount"));
                break;
            }
            case HOSTILE_MOB: {
                entity.getBukkitEntity().setMetadata("type", new FixedMetadataValue(DungeonRealms.getInstance(), "hostile"));
                entity.getBukkitEntity().setMetadata("tier", new FixedMetadataValue(DungeonRealms.getInstance(), entityTier));
                entity.getBukkitEntity().setMetadata("level", new FixedMetadataValue(DungeonRealms.getInstance(), level));
                break;
            }
        }
    }
}
