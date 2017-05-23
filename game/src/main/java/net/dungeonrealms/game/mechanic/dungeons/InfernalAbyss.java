package net.dungeonrealms.game.mechanic.dungeons;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.entity.type.monster.type.EnumMonster;
import net.dungeonrealms.game.world.entity.util.EntityAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * The Infernal Abyss Dungeon
 * <p>
 * Created April 28th, 2017.
 *
 * @author Kneesnap
 */
@Getter
public class InfernalAbyss extends Dungeon {

    private int wither;

    public InfernalAbyss(List<Player> players) {
        super(DungeonType.THE_INFERNAL_ABYSS, players);
    }

    @Override
    protected void setupWorld(String worldName) {
        super.setupWorld(worldName);
        CraftWorld world = (CraftWorld) getWorld();
        world.setEnvironment(Environment.NETHER);
        world.getHandle().worldProvider.a(world.getHandle()); //<- Prevents an NMS crash, by initing the world.
    }

    @Override
    public void updateMob(Entity e) {
        if (e.getLocation().getY() < 90)
            returnToSpawner(e); //Return any mobs that fall out of the world.

        super.updateMob(e);
    }

    public void setWither(int w) {
        this.wither = w;
        if (w > 0) {
            getPlayers().forEach(p -> p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1, w * 20, true)));
        } else {
            getPlayers().forEach(p -> p.removePotionEffect(PotionEffectType.WITHER));
        }
    }

    public static class InfernalListener implements Listener {

        public InfernalListener() {

            // Spawn minions and leaves a trail of fire behind Infernal.
            Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> {
                for (Dungeon o : DungeonManager.getDungeons(DungeonType.THE_INFERNAL_ABYSS)) {
                    InfernalAbyss dungeon = (InfernalAbyss) o;

                    //Not spawned..
                    if (dungeon.getBoss() == null) continue;

                    Entity e = dungeon.getBoss().getBukkit();
                    if (e.isDead() || !e.isOnGround())
                        return;

                    Location l = e.getLocation();
                    if (l.getBlock().getType() == Material.AIR)
                        l.getBlock().setType(Material.FIRE);

                    if (Utils.randInt(20) == 0)
                        EntityAPI.spawnCustomMonster(l.clone().add(0, 2, 0), EnumMonster.MagmaCube, "low", 3, null);
                }
            }, 0L, 5L);

            // Handle wither effects.
            Bukkit.getScheduler().runTaskTimer(DungeonRealms.getInstance(), () -> DungeonManager.getDungeons(DungeonType.THE_INFERNAL_ABYSS).forEach(d -> {
                InfernalAbyss ab = (InfernalAbyss) d;
                int w = ab.getWither();
                if (w <= 0) {
                    //Check if they were inflicted by the command block.
                    for (Player pl : ab.getWorld().getPlayers()) {
                        if (pl.hasPotionEffect(PotionEffectType.WITHER)) {
                            //Activate?
                            PotionEffect effect = pl.getActivePotionEffects().stream().filter(e -> e.getType().getName().equals(PotionEffectType.WITHER.getName())).findFirst().orElse(null);
                            if (effect != null && effect.getDuration() > 20) {
                                //Get time in seconds?
                                Bukkit.getLogger().info("Setting wither duration to " + effect.getDuration() / 20);
                                ab.setWither(effect.getDuration() / 20);
                            }
                        }
                    }
                    return;
                }

                if (w == 30) {
                    ab.announce(ChatColor.RED + "" + ChatColor.BOLD + ">> " + ChatColor.RED + "You have " + ChatColor.UNDERLINE +
                            w + "s" + ChatColor.RED + " left until the inferno consumes you.");
                } else if (w == 1) { //Last second?
                    for (Player p : d.getPlayers()) {
                        HealthHandler.setHP(p, 1);
                        p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 2, 1.3F);
                    }
                    d.announce(ChatColor.RED + "" + ChatColor.BOLD + "You have been drained of nearly all your life by the power of the inferno.");
                }
                ab.setWither(--w);
            }), 200L, 20L);
        }

        private void destroyDebuff(Entity e) {
            InfernalAbyss dungeon = (InfernalAbyss) DungeonManager.getDungeon(e.getWorld());
            Block block = e.getLocation().clone().subtract(0, 1, 0).getBlock();
            ParticleAPI.spawnParticle(Particle.CRIT_MAGIC, block.getLocation().add(0, 1, 0), 50, 1F);

            if (block.getType() == Material.BEDROCK)
                block.setType(Material.AIR);
            e.remove();

            dungeon.setWither(90);
            dungeon.announce(ChatColor.YELLOW + "Debuff timer refreshed, " + ChatColor.UNDERLINE + " Your HP " + ChatColor.YELLOW + "will be inflicted in 90s unless another beacon is activated.");
            dungeon.getPlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 5F, 1.5F));
        }

        /**
         * Handles punching a debuff.
         */
        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void playerHitEnderCrystal(EntityDamageEvent event) {
            if (event.getEntity().getType() == EntityType.ENDER_CRYSTAL && DungeonManager.isDungeon(event.getEntity().getWorld(), DungeonType.THE_INFERNAL_ABYSS)) {
                event.setCancelled(true);
                destroyDebuff(event.getEntity());
            }
        }

        /**
         * Handles punching blocks to activate debuffs
         */
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockHit(PlayerInteractEvent event) {
            Block block = event.getClickedBlock();

            if (event.getAction() != Action.LEFT_CLICK_BLOCK || !DungeonManager.isDungeon(block.getWorld(), DungeonType.THE_INFERNAL_ABYSS) || (block.getType() != Material.BEDROCK && block.getType() != Material.FIRE))
                return;

            if (block.getType() == Material.BEDROCK) {
                Block up = block.getLocation().clone().add(0, 1, 0).getBlock();
                if (up.getType() == Material.FIRE) {
                    block = up; // We want to touch the fire.
                } else {
                    return; //We don't really want to touch bedrock.
                }
            }

            block.getWorld().getNearbyEntities(block.getLocation(), 3, 3, 3).stream().filter(e -> e instanceof EnderCrystal)
                    .forEach(this::destroyDebuff);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onMobDeath(EntityDeathEvent evt) {
            if (!DungeonManager.isDungeon(evt.getEntity().getWorld(), DungeonType.THE_INFERNAL_ABYSS))
                return;

            InfernalAbyss dungeon = (InfernalAbyss) DungeonManager.getDungeon(evt.getEntity().getWorld());
            Entity entity = evt.getEntity();
            String name = ChatColor.stripColor(EntityAPI.getCustomName(entity));
            ItemStack stack = null;

            if(entity.getType() == EntityType.ENDERMAN){
                if (name.equals("The Devastator")) {
                    stack = getKey("A");
                } else if (name.equals("The Annihilator")) {
                    stack = getKey("B");
                }
                ParticleAPI.spawnParticle(Particle.PORTAL, entity.getLocation().clone().add(0, 1, 0), .75F, 50, .06F);
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMEN_DEATH, 10, .7F);
            } else if (name.equals("Fire Lord Of The Abyss")) {
                stack = ItemGenerator.getNamedItem("firelord");
            } else if (name.equals("Ice Lord Of The Abyss")) {
                stack = ItemGenerator.getNamedItem("icelord");
            }

            Player killer = evt.getEntity().getKiller();

            if (stack != null) {
                if (killer != null && killer.getInventory().firstEmpty() != -1) {
                    killer.getInventory().addItem(stack);
                } else {
                    entity.getWorld().dropItemNaturally(entity.getLocation().clone().add(0, 1, 0), stack);
                }
            }else{
                Bukkit.getLogger().info("Stats were null!");
            }

            dungeon.setWither(0);
        }

        private ItemStack getKey(String k) {
            return ItemGenerator.getNamedItem("doorkey" + k.toLowerCase());
        }
    }
}
