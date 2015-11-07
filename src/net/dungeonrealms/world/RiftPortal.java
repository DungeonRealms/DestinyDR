package net.dungeonrealms.world;

import com.connorlinfoot.bountifulapi.BountifulAPI;
import net.dungeonrealms.API;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.entities.EnumEntityType;
import net.dungeonrealms.entities.types.monsters.EnumMonster;
import net.dungeonrealms.entities.utils.EntityStats;
import net.dungeonrealms.mastery.MetadataUtils;
import net.dungeonrealms.mastery.Utils;
import net.dungeonrealms.mechanics.ParticleAPI;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.dungeonrealms.party.Party;
import net.dungeonrealms.spawning.SpawningMechanics;
import net.minecraft.server.v1_8_R3.Entity;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.*;

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
            /*
            Tell player when they've entered the area of the rift portal.
             */
            Bukkit.getOnlinePlayers().stream().forEach(player -> {
                _riftPortals.stream().forEach(portal -> {
                    if (portal.getLocation().distanceSquared(player.getLocation()) < 175) {
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
                Check unload
                 */
                portal.unloadRift();

                /*
                Particles
                 */
                portal.invokeParticles();

                portal.getMobs().forEach(entity -> {
                    if (entity == null || !entity.isAlive()) {
                        portal.getMobs().remove(entity);
                        if (portal.getMobs().isEmpty()) {
                            /*
                            Ending the rift, making sure players are
                            there.
                             */
                            portal.getParticipating().stream().forEach(player -> {
                                BountifulAPI.sendTitle(player, 1, 20 * 3, 1, "", ChatColor.LIGHT_PURPLE + "RIFT " + ChatColor.GREEN.toString() + ChatColor.BOLD + "COMPLETE");
                                for (int i = 0; i < 10; i++) {
                                    portal.getLocation().getWorld().dropItem(portal.getLocation().add(1, 1, 1), new ItemStack(Material.DIAMOND));
                                }
                                portal.unloadRift();
                            });
                        }
                    } else {
                        if (entity.getBukkitEntity().getLocation().distanceSquared(portal.getLocation()) > 150) {
                            entity.getBukkitEntity().teleport(portal.getLocation());
                        }
                    }
                });

                /*
                Check if is loaded etc...
                 */
                portal.getParticipating().stream().forEach(player -> {

                    BountifulAPI.sendActionBar(player, ChatColor.GREEN + String.valueOf(portal.getMobs().size()) + " mobs left!");

                    if (portal.getLocation().distanceSquared(player.getLocation()) > 175) {
                        portal.removeParticipating(player);
                        if (portal.getParticipating().isEmpty()) {
                            if (portal.load) {
                                portal.unloadRift();
                            }
                        }
                    }
                });
            });


        }, 0, 10);

    }

    public void addPortal(int tier, Location riftPortal) {
        Portal p = new Portal(riftPortal, tier, new CopyOnWriteArrayList<>(), new CopyOnWriteArrayList<>());
        Utils.log.info("Added Portal Rift " + tier + " at location: " + riftPortal.getWorld() + " " + riftPortal.getX() + " " + riftPortal.getY() + " " + riftPortal.getZ());
        _riftPortals.add(p);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(ChunkUnloadEvent event) {
        _riftPortals.stream().forEach(portal -> {
            if (event.getChunk().equals(portal.getLocation().getChunk())) {
                if (!portal.getParticipating().isEmpty()) {
                    event.setCancelled(true);
                } else {
                    portal.unloadRift();
                }
            }
        });
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
            loadScoreBoard();
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

        public void loadScoreBoard() {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
                if (!getParticipating().isEmpty()) {
                    getParticipating().stream().forEach(player -> {
                        if (!Party.getInstance().isInParty(player)) {
                            ScoreboardManager manager = Bukkit.getScoreboardManager();
                            Scoreboard board = manager.getNewScoreboard();
                            Objective objective = board.registerNewObjective("test", "dummy");
                            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                            objective.setDisplayName(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "RIFT PORTAL");

                            Score tier = objective.getScore(ChatColor.GREEN.toString() + ChatColor.BOLD + "Tier" + ChatColor.GRAY + ": " + ChatColor.RED + getTier());
                            tier.setScore(14);

                            Score reward = objective.getScore(ChatColor.GREEN.toString() + ChatColor.BOLD + "Reward" + ChatColor.GRAY + ": " + ChatColor.RED + "T3 Glyph");
                            reward.setScore(13);

                            Score blank = objective.getScore(ChatColor.BLACK + " ");
                            blank.setScore(12);

                            Score mobs = objective.getScore(ChatColor.BLUE + "Mobs -");
                            mobs.setScore(11);

                            getMobs().stream().forEach(entity -> {
                                Score mobName = objective.getScore(entity.getBukkitEntity().getCustomName());
                                mobName.setScore(-1);
                            });
                            player.setScoreboard(board);
                        }
                    });
                }
            }, 0, 35);
        }

        public void invokeParticles() {
            getCircle(getLocation(), 2, 40).stream().forEach(location -> {
                ParticleAPI.sendParticleToLocation(ParticleAPI.ParticleEffect.PORTAL, location, 0, 0, 0, 0.2f, 30);
            });
        }

        public void unloadRift() {
            if (getParticipating().isEmpty()) {
                if (!getMobs().isEmpty()) {
                    getMobs().stream().forEach(entity -> {
                        entity.getBukkitEntity().remove();
                        Utils.log.warning("Rift removing mob");
                    });
                } else {
                    getMobs().clear();
                }

                load = false;
            }
        }

        public void loadRift() {
            if (!load) {
                this.load = true;
                for (int i = 0; i < _tier; i++) {
                    Entity entity = SpawningMechanics.getMob(((CraftWorld) getLocation().getWorld()).getHandle(), getTier(), EnumMonster.Bandit);
                    int level = Utils.getRandomFromTier(getTier(), "low");
                    MetadataUtils.registerEntityMetadata(entity, EnumEntityType.HOSTILE_MOB, getTier(), level);
                    EntityStats.setMonsterRandomStats(entity, level, getTier());
                    _mobs.add(entity);
                }
                API.getNearbyPlayers(getLocation(), 40).stream().forEach(player -> {
                    player.sendMessage(ChatColor.GREEN + "A rift shard is appearing near you!");
                });
                spawnMobs();
            }
        }

        public void spawnMobs() {
            _mobs.forEach(entity -> {
                entity.setLocation(getLocation().getX(), getLocation().getY(), getLocation().getZ(), 0f, 0f);
                ((CraftWorld) getLocation().getWorld()).getHandle().addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
                entity.setLocation(getLocation().getX(), getLocation().getY(), getLocation().getZ(), 0f, 0f);
            });
        }

        public void addParticipating(Player player) {
            if (!_participating.contains(player)) {
                _participating.add(player);
                BountifulAPI.sendTitle(player, 1, 20 * 2, 1, "", ChatColor.GREEN + "Entered Rift Portal");
            }
        }

        public void removeParticipating(Player player) {
            if (_participating.contains(player)) {
                _participating.remove(player);
                BountifulAPI.sendTitle(player, 1, 20 * 2, 1, "", ChatColor.GREEN + "Left Rift Portal");
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
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
