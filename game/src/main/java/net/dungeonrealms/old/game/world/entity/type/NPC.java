package net.dungeonrealms.old.game.world.entity.type;

import net.dungeonrealms.DungeonRealms;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_9_R2.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.Field;

public abstract class NPC implements Listener {

    private static Field b;

    static {
        try {
            b = PathfinderGoalSelector.class.getDeclaredField("b");
            b.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private final String name;
    private final Location location;
    private final boolean visibleName;
    protected Villager villager;

    public NPC(final String name, final Location location, final boolean visibleName) {
        this.name = name;
        this.location = location;
        this.visibleName = visibleName;
        location.getChunk().load();
        Bukkit.getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public void loadNPC() {
        villager = (Villager) location.getWorld().spawnEntity(location.clone(), EntityType.VILLAGER);
        villager.setCustomName(name);
        villager.setCustomNameVisible(visibleName);
		villager.setRemoveWhenFarAway(false);
        EntityVillager entity = ((CraftVillager) villager).getHandle();
        try {
            Field goalSelector = EntityInsentient.class.getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);
            PathfinderGoalSelector goal = (PathfinderGoalSelector) goalSelector.get(entity);
            b.set(goal, new UnsafeList<>());
            for (int i = 0; i < 11; i++) {
                goal.a(i, new PathfinderGoalLookAtPlayer(entity, EntityPlayer.class, 20.0F));
            }

            Field invulnerable = Entity.class.getDeclaredField("invulnerable");
            invulnerable.setAccessible(true);
            invulnerable.setBoolean(entity, true);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().equals(villager)) {
            event.setCancelled(true);
            this.onClick(event.getPlayer());
        }
    }

    public void walkTo(Location location) {
        EntityVillager entity = ((CraftVillager) villager).getHandle();
        entity.getNavigation().a(location.getX(), location.getY(), location.getZ(), 0.5f);
    }

    public abstract void onClick(Player clicker);

}