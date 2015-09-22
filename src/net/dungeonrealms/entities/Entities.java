package net.dungeonrealms.entities;

import java.util.HashMap;
import java.util.UUID;

import net.dungeonrealms.entities.types.monsters.EntityBandit;
import net.dungeonrealms.entities.types.monsters.EntityFireImp;
import net.dungeonrealms.entities.types.monsters.EntityPirate;
import net.dungeonrealms.entities.types.monsters.EntityRangedPirate;
import net.dungeonrealms.entities.types.mounts.Horse;
import net.dungeonrealms.entities.types.pets.BabyZombie;
import net.dungeonrealms.entities.types.pets.BabyZombiePig;
import net.dungeonrealms.entities.types.pets.CaveSpider;
import net.dungeonrealms.entities.types.pets.Chicken;
import net.dungeonrealms.entities.types.pets.Endermite;
import net.dungeonrealms.entities.types.pets.Ocelot;
import net.dungeonrealms.entities.types.pets.Rabbit;
import net.dungeonrealms.entities.types.pets.Silverfish;
import net.dungeonrealms.entities.types.pets.Wolf;
import net.dungeonrealms.mastery.NMSUtils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityCaveSpider;
import net.minecraft.server.v1_8_R3.EntityChicken;
import net.minecraft.server.v1_8_R3.EntityEndermite;
import net.minecraft.server.v1_8_R3.EntityHorse;
import net.minecraft.server.v1_8_R3.EntityOcelot;
import net.minecraft.server.v1_8_R3.EntityPigZombie;
import net.minecraft.server.v1_8_R3.EntityRabbit;
import net.minecraft.server.v1_8_R3.EntitySilverfish;
import net.minecraft.server.v1_8_R3.EntitySkeleton;
import net.minecraft.server.v1_8_R3.EntityWolf;
import net.minecraft.server.v1_8_R3.EntityZombie;

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
        nmsUtils.registerEntity("RangedPirate", 54, EntityZombie.class, EntityRangedPirate.class);
        nmsUtils.registerEntity("Fire Imp", 54, EntityZombie.class, EntityFireImp.class);
        nmsUtils.registerEntity("Bandit", 51, EntitySkeleton.class, EntityBandit.class);

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
}
