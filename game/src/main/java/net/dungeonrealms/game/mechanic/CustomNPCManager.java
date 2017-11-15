package net.dungeonrealms.game.mechanic;

import io.netty.util.internal.ConcurrentSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomNPCManager implements GenericMechanic {

    NPCRegistry registry;

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    @Override
    public void startInitialization() {
        registry = CitizensAPI.getNPCRegistry();
        Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(), () -> {
            if (registry != null) {
                Set<CustomNPC> toSpawn = new ConcurrentSet<>();
                toSpawn.addAll(Arrays.stream(CustomNPC.values()).collect(Collectors.toList()));

                for (Iterator<NPC> it = registry.iterator(); it.hasNext(); ) {
                    NPC entity = it.next();
                    if (toSpawn.isEmpty()) break;
                    if (entity != null && entity.getName() != null) {
                        for (CustomNPC spawn : toSpawn) {
                            if (spawn.getName().equalsIgnoreCase(ChatColor.stripColor(entity.getName()))) {
                                Bukkit.getLogger().info("Not force spawning " + spawn.getName());
                                toSpawn.remove(spawn);
                                if (entity instanceof SkinnableEntity && spawn.getSkinName() != null)
                                    ((SkinnableEntity) entity).setSkinName(spawn.getSkinName(), true);
                            }
                        }
                    }
                }

                if (toSpawn.size() > 0) {
                    for (CustomNPC custom : toSpawn) {
                        NPC npc = registry.createNPC(EntityType.PLAYER, custom.getName());
                        npc.spawn(custom.getLocation());
                        LookClose trait = new LookClose();
                        trait.lookClose(true);
                        npc.addTrait(trait);
                        if (npc instanceof SkinnableEntity && custom.getSkinName() != null)
                            ((SkinnableEntity) npc).setSkinName(custom.getSkinName(), true);

                        Bukkit.getLogger().info("Force creating " + custom.getName() + " NPC!");
                    }
                }
            }
        }, 20);
    }

    @Override
    public void stopInvocation() {

    }

    @Getter
    @AllArgsConstructor
    enum CustomNPC {
        SALES_MANAGER("Sales Manager", "milaac", new Location(null, -381.5, 83.1, 338.5));

        private String name, skinName;
        private Location location;

        public Location getLocation() {
            location.setWorld(GameAPI.getMainWorld());
            return location;
        }
    }
}
