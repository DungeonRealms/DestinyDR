package net.dungeonrealms.world;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Nick on 11/6/2015.
 */
public class RiftPortal implements GenericMechanic, Listener {

    static RiftPortal instance = null;

    public static RiftPortal getInstance() {
        if (instance == null) {
            instance = new RiftPortal();
        }
        return instance;
    }

    CopyOnWriteArrayList<Portal> _riftPortals = new CopyOnWriteArrayList<>();

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    @Override
    public void startInitialization() {

        DungeonRealms.getInstance().getConfig().getStringList("riftPortal").stream().forEach(portal -> {
            addPortal(Integer.valueOf(portal.split(",")[0]),
                    new Location(Bukkit.getWorlds().get(0),
                            Double.valueOf(portal.split(",")[1])
                            , Double.valueOf(portal.split(",")[2])
                            , Double.valueOf(portal.split(",")[3])));
        });


        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            System.out.println("Repeating Task");
            /*
            Tell player when they've entered the area of the rift portal.
             */
            Bukkit.getOnlinePlayers().stream().forEach(player -> {
                System.out.println("getOnlinePlayers() forEach");
                _riftPortals.stream().forEach(portal -> {
                    if (portal.getLocation().distanceSquared(player.getLocation()) < 175) {
                        System.out.println("Added Participating");
                        portal.addParticipating(player);
                        portal.loadRift();
                    }
                });
            });

            /*
            Remove players from participating list if they're out of range!
             */
            _riftPortals.stream().forEach(portal -> {

                /*
                actionBar
                 */
                portal.getParticipating().stream().forEach(player -> {
                    BountifulAPI.sendActionBar(player, ChatColor.GREEN + String.valueOf(portal.getMobs().size()) + " mobs left!");
                });

                /*
                Particles
                 */
                portal.invokeParticles();

                /*
                Check if is loaded etc...
                 */
                portal.getParticipating().stream().forEach(player -> {
                    if (portal.getLocation().distanceSquared(player.getLocation()) > 175) {
                        portal.removeParticipating(player);
                        if (portal.getParticipating().isEmpty()) {
                            if (portal.load) {
                                portal.load = false;
                            }
                        }
                    }
                });
            });


        }, 0, 10);

    }

    public void addPortal(int tier, Location riftPortal) {
        Portal p = new Portal(riftPortal, tier, new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>());
        p.loadRift();
        Utils.log.info("Added Portal Rift " + tier + " at location: " + riftPortal.getWorld() + " " + riftPortal.getX() + " " + riftPortal.getY() + " " + riftPortal.getZ());
        _riftPortals.add(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent event) {

    }

    @Override
    public void stopInvocation() {
        _riftPortals.stream().forEach(portal -> {
            portal.getMobs().stream().forEach(entity -> {
                if (entity.isAlive()) {
                    entity.getBukkitEntity().remove();
                }
            });
        });
    }


    class Portal {
        private Location _location;
        private int _tier;
        private CopyOnWriteArrayList<Player> _participating;
        private CopyOnWriteArrayList<Entity> _mobs;
        private boolean load = false;

        public Portal(Location _location, int _tier, CopyOnWriteArrayList<Player> _participating, CopyOnWriteArrayList<Entity> _mobs) {
            this._location = _location.add(0, 4, 0);
            this._tier = _tier;
            this._participating = _participating;
            this._mobs = _mobs;
        }

        public Location getLocation() {
            return _location;
        }

        public int getTier() {
            return _tier;
        }

        public List<Player> getParticipating() {
            return _participating;
        }

        public List<Entity> getMobs() {
            return _mobs;
        }

        public void invokeParticles() {
            System.out.println("invoke Particles");
            getCircle(getLocation(), 2, 20).stream().forEach(location -> {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.SMALL_SMOKE, location, 0, 0, 0, 0.2f, 70);
            });
        }

        public void loadRift() {
            if (!load) {
                load = true;
                for (int i = 0; i < _tier; i++) {
                    Entity entity = SpawningMechanics.getMob(((CraftWorld) getLocation().getWorld()).getHandle(), getTier(), EnumMonster.Bandit);
                    int level = Utils.getRandomFromTier(getTier(), "low");
                    MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, getTier(), level);
                    EntityStats.setMonsterRandomStats(entity, level, getTier());

                    entity.setLocation(getLocation().getX(), getLocation().getY(), getLocation().getZ(), 0f, 0f);
                    ((CraftWorld) getLocation().getWorld()).getHandle().addEntity(entity);

                    _mobs.add(entity);
                }
            } else {
                Utils.log.warning("Unable to load rift!");
            }
        }

        public void addParticipating(Player player) {
            if (!_participating.contains(player)) {
                _participating.add(player);
                player.sendMessage(ChatColor.GREEN + "You are now participating in rift portal!");
            }
        }

        public void removeParticipating(Player player) {
            if (_participating.contains(player)) {
                player.sendMessage(ChatColor.GREEN + "You have left the rift portal!");
                _participating.remove(player);
            }
        }
    }

    public ArrayList<Location> getCircle(Location center, double radius, int amount) {
        World world = center.getWorld();
        double increment = (2 * Math.PI) / amount;
        ArrayList<Location> locations = new ArrayList<Location>();
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            double x = center.getX() + (radius * Math.cos(angle));
            double z = center.getZ() + (radius * Math.sin(angle));
            locations.add(new Location(world, x, center.getY(), z));
        }
        return locations;
    }
}
