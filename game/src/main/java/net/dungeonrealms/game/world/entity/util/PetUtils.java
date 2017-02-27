package net.dungeonrealms.game.world.entity.util;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.donation.DonationEffects;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.world.entity.EntityMechanics;
import net.dungeonrealms.game.world.entity.EnumEntityType;
import net.dungeonrealms.game.world.entity.type.pet.*;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Kieran on 9/18/2015.
 */
public class PetUtils implements GenericMechanic{

    private static PetUtils instance = null;

    public static PetUtils getInstance() {
        if (instance == null) {
            return new PetUtils();
        }
        return instance;
    }

    private static Field gsa;
    private static Field goalSelector;
    private static Field targetSelector;

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
        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for (Map.Entry<UUID, Entity> entry : EntityMechanics.PLAYER_PETS.entrySet()) {
                Entity pet = entry.getValue();
                Player player = Bukkit.getPlayer(entry.getKey());
                org.bukkit.World world = pet.getBukkitEntity().getWorld();
                Location location = new Location(world, pet.lastX, pet.lastY, pet.lastZ);
                if (player.getLocation().distance(location) > 20) {
                    if (!(player.isFlying())) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> pet.getBukkitEntity().teleport(player), 0L);
                    }
                }
            }
        }, 100L, 100L);
    }

    @Override
    public void stopInvocation() {

    }

    /**
     *
     * @param e
     * @param toFollow
     * @since 1.0
     */
    public static void makePet(EntityLiving e, UUID toFollow, double speed, EnumPets petType) {
        try {
            if (e instanceof EntityInsentient) {
                if (petType != EnumPets.SLIME && petType != EnumPets.MAGMA_CUBE) {
                    PathfinderGoalSelector goal = (PathfinderGoalSelector) goalSelector.get(e);
                    goal.a(0, new PathfinderGoalFloat((EntityInsentient) e));
                    goal.a(1, new PathfinderGoalWalkToTile((EntityInsentient) e, toFollow, speed));
                } else {
                    PathfinderGoalSelector goal = (PathfinderGoalSelector) goalSelector.get(e);
                    goal.a(0, new PathfinderGoalFloat((EntityInsentient) e));
                    goal.a(1, new PathfinderGoalSlimeFollowOwner((EntityInsentient) e, toFollow, speed));
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
        private UUID p;
        private double speed;

        PathfinderGoalWalkToTile(EntityInsentient entity, UUID p, double speed) {
            this.entity = entity;
            this.p = p;
            this.speed = speed;
        }

        @Override
        public boolean a() {
            if (Bukkit.getPlayer(p) == null) {
                return path != null;
            }
            Location targetLocation = Bukkit.getPlayer(p).getLocation();

            this.entity.getNavigation();
            this.path = this.entity.getNavigation().a(targetLocation.getX() + 1, targetLocation.getY(), targetLocation.getZ() + 1);
            this.entity.getNavigation();
            if (this.path != null && this.entity.getBukkitEntity().getLocation().distanceSquared(targetLocation) >= 6) {
                this.c();
            }
            return this.path != null && this.entity.getBukkitEntity().getLocation().distanceSquared(targetLocation) >= 6;
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
        private UUID p;

        static {
            try {
                controllerRotate = EntitySlime.class.getDeclaredClasses()[0].getDeclaredMethod("a", float.class, boolean.class);
                controllerRotate.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        PathfinderGoalSlimeFollowOwner(EntityInsentient entity, UUID p, double speed) {
            this.entity = entity;
            this.speed = speed;
            this.p = p;
            this.a(3);
        }

        @Override
        public boolean a() {
            Player owner = Bukkit.getPlayer(p);
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
            Entity owner = ((CraftPlayer) Bukkit.getPlayer(p)).getHandle();
            this.entity.a(owner, 10.0F, 10.0F);
            if (this.entity.getBukkitEntity().getLocation().distanceSquared(owner.getBukkitEntity().getLocation()) >= 6) {
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
     *
     * @param uuid
     * @param petType
     * @param name
     * @since 1.0
     */
    public static void spawnPet(UUID uuid, String petType, String name) {
        Player player = Bukkit.getPlayer(uuid);
        if (!player.getWorld().equals(Bukkit.getWorlds().get(0))) {
            player.sendMessage(ChatColor.RED + "Your pet cannot be summoned in this world.");
            return;
        }
        World world = ((CraftWorld) player.getWorld()).getHandle();
        if (!GameAPI.isStringPet(petType)) {
            player.sendMessage("Uh oh... Something went wrong with your pet! Please inform a staff member! [PetType]");
            return;
        }
        ChatColor prefix = ChatColor.WHITE;
        if (Rank.isSubscriber(player)) {
            String rank = Rank.getInstance().getRank(player.getUniqueId());
            if (rank.equalsIgnoreCase("sub") || rank.equalsIgnoreCase("hiddenmod")) {
                prefix = ChatColor.GREEN;
            } else if (rank.equalsIgnoreCase("sub+")) {
                prefix = ChatColor.GOLD;
            } else if (rank.equalsIgnoreCase("sub++")) {
                prefix = ChatColor.YELLOW;
            }
        }
        if (Rank.isDev(player)) {
            prefix = ChatColor.AQUA;
        }
        name = prefix + name;
        EnumPets enumPets = EnumPets.getByName(petType.toUpperCase());
        switch (enumPets) {
            case CAVE_SPIDER:
                CaveSpider petCaveSpider = new CaveSpider(world, name, player.getUniqueId(), EnumEntityType.PET);
                petCaveSpider.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petCaveSpider, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petCaveSpider.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1F, 1F);
                makePet(petCaveSpider, player.getUniqueId(), 1.3D, EnumPets.CAVE_SPIDER);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petCaveSpider);
                player.closeInventory();
                break;
            case BABY_ZOMBIE:
                BabyZombie petBabyZombie = new BabyZombie(world, name, player.getUniqueId(), EnumEntityType.PET);
                petBabyZombie.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petBabyZombie, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petBabyZombie.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 1F, 1F);
                makePet(petBabyZombie, player.getUniqueId(), 1.0D, EnumPets.BABY_ZOMBIE);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petBabyZombie);
                player.closeInventory();
                break;
            case BABY_PIGZOMBIE:
                BabyZombiePig petBabyZombiePig = new BabyZombiePig(world, name, player.getUniqueId(), EnumEntityType.PET);
                petBabyZombiePig.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petBabyZombiePig, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petBabyZombiePig.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petBabyZombiePig.setBaby(true);
                petBabyZombiePig.angerLevel = 0;
                player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_PIG_AMBIENT, 1F, 1F);
                makePet(petBabyZombiePig, player.getUniqueId(), 1.0D, EnumPets.BABY_PIGZOMBIE);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petBabyZombiePig);
                player.closeInventory();
                break;
            case WOLF:
                Wolf petWolf = new Wolf(world, name, player.getUniqueId(), EnumEntityType.PET);
                petWolf.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petWolf, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petWolf.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petWolf.setAngry(false);
                petWolf.setTamed(true);
                petWolf.ageLocked = true;
                petWolf.setAge(0);
                petWolf.setHealth(petWolf.getMaxHealth());
                player.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1F, 1F);
                makePet(petWolf, player.getUniqueId(), 1.1D, EnumPets.WOLF);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petWolf);
                player.closeInventory();
                break;
            case CHICKEN:
                Chicken petChicken = new Chicken(world, name, player.getUniqueId(), EnumEntityType.PET);
                petChicken.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petChicken, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petChicken.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petChicken.setAge(-1);
                petChicken.ageLocked = true;
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_AMBIENT, 1F, 1F);
                makePet(petChicken, player.getUniqueId(), 1.1D, EnumPets.CHICKEN);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petChicken);
                player.closeInventory();
                break;
            case OCELOT:
                Ocelot petOcelot = new Ocelot(world, name, player.getUniqueId(), EnumEntityType.PET);
                petOcelot.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petOcelot, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petOcelot.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petOcelot.setAgeRaw(-24000);
                petOcelot.ageLocked = true;
                petOcelot.setTamed(true);
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1F, 1F);
                makePet(petOcelot, player.getUniqueId(), 1D, EnumPets.OCELOT);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petOcelot);
                player.closeInventory();
                break;
            case RABBIT:
                Rabbit petRabbit = new Rabbit(world, name, player.getUniqueId(), EnumEntityType.PET);
                petRabbit.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petRabbit, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petRabbit.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petRabbit.setAge(-1);
                petRabbit.ageLocked = true;
                player.playSound(player.getLocation(), Sound.ENTITY_RABBIT_AMBIENT, 1F, 1F);
                makePet(petRabbit, player.getUniqueId(), 0.9D, EnumPets.RABBIT);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petRabbit);
                player.closeInventory();
                break;
            case SILVERFISH:
                Silverfish petSilverfish = new Silverfish(world, name, player.getUniqueId(), EnumEntityType.PET);
                petSilverfish.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petSilverfish, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petSilverfish.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_SILVERFISH_AMBIENT, 1F, 1F);
                makePet(petSilverfish, player.getUniqueId(), 1.5D, EnumPets.SILVERFISH);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petSilverfish);
                player.closeInventory();
                break;
            case ENDERMITE:
                Endermite petEndermite = new Endermite(world, name, player.getUniqueId(), EnumEntityType.PET);
                petEndermite.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petEndermite, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petEndermite.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMITE_AMBIENT, 1F, 1F);
                makePet(petEndermite, player.getUniqueId(), 1.5D, EnumPets.ENDERMITE);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petEndermite);
                player.closeInventory();
                break;
            case SNOWMAN:
                Snowman petSnowman = new Snowman(world, name, player.getUniqueId(), EnumEntityType.PET);
                petSnowman.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petSnowman, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petSnowman.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_SNOWMAN_AMBIENT, 1F, 1F);
                makePet(petSnowman, player.getUniqueId(), 1.8D, EnumPets.SNOWMAN);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petSnowman);
                player.closeInventory();
                break;
            case BAT:
                Bat petBat = new Bat(world, name, player.getUniqueId(), EnumEntityType.PET);
                petBat.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petBat, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petBat.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 1F);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petBat);
                player.closeInventory();
                break;
            case SLIME:
                Slime petSlime = new Slime(world, name, player.getUniqueId(), EnumEntityType.PET);
                petSlime.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petSlime, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petSlime.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1F, 1F);
                makePet(petSlime, player.getUniqueId(), 1.25D, EnumPets.SLIME);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petSlime);
                player.closeInventory();
                break;
            case MAGMA_CUBE:
                MagmaCube petMagmaCube = new MagmaCube(world, name, player.getUniqueId(), EnumEntityType.PET);
                petMagmaCube.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                world.addEntity(petMagmaCube, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petMagmaCube.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                player.playSound(player.getLocation(), Sound.ENTITY_MAGMACUBE_SQUISH, 1F, 1F);
                makePet(petMagmaCube, player.getUniqueId(), 1.25D, EnumPets.MAGMA_CUBE);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petMagmaCube);
                player.closeInventory();
                break;
            case CREEPER_OF_INDEPENDENCE:
                Creeper petIndependenceCreeper = new Creeper(world, name, player.getUniqueId(), EnumEntityType.PET);
                petIndependenceCreeper.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petIndependenceCreeper.setPowered(true);
                world.addEntity(petIndependenceCreeper, CreatureSpawnEvent.SpawnReason.CUSTOM);
                petIndependenceCreeper.setLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), 0, 0);
                petIndependenceCreeper.setPowered(true);
                player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1F, 1F);
                makePet(petIndependenceCreeper, player.getUniqueId(), 1.25D, EnumPets.CREEPER_OF_INDEPENDENCE);
                EntityAPI.addPlayerPetList(player.getUniqueId(), petIndependenceCreeper);
                player.closeInventory();
                DonationEffects.getInstance().fireWorkCreepers.add(petIndependenceCreeper);
                break;
        }
    }
}
