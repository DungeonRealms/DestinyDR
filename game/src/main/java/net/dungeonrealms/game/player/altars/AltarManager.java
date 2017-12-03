package net.dungeonrealms.game.player.altars;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticleEffect;
import net.dungeonrealms.game.player.cosmetics.particles.SpecialParticles;
import net.dungeonrealms.game.player.cosmetics.particles.impl.GroundHaloParticleEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Rar349 on 8/3/2017.
 */
public class AltarManager {

    public static List<SpecialParticleEffect> effects = new ArrayList<>();
    public static ConcurrentHashMap<Altars, Altar> currentlyUsingAltars = new ConcurrentHashMap<>();

    public static void initAltarManager() {
//        if (!DungeonRealms.isMaster())
//            return; // Don't load on live shards yet.

        for(Altars altar : Altars.values()) {
            for(int nodeIndex = 0; nodeIndex < altar.getNodeSize(); nodeIndex++) {
                Location nodeLoc = altar.getNode(nodeIndex);
                nodeLoc.getBlock().setType(Material.STONE_SLAB2);
                GroundHaloParticleEffect effect = new GroundHaloParticleEffect(nodeLoc.clone().add(0.5,0,0.5));
                effects.add(effect);
            }
        }

        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for(SpecialParticleEffect effect : effects) {
                effect.tick();
            }
        },1,1);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            for(Altar altar : currentlyUsingAltars.values()) {
                altar.tick();
            }
        },1,1);
    }

    public static Altars getAltarFromLocation(Location loc) {
        for(Altars altar : Altars.values()) {
            for(int nodeIndex = 0; nodeIndex < altar.getNodeSize(); nodeIndex++) {
                Location nodeLoc = altar.getNode(nodeIndex);
                if(nodeLoc.equals(loc)) return altar;
            }
        }

        return null;
    }

    public static Altar getAltar(Altars altar) {
        return currentlyUsingAltars.get(altar);
    }

    public static boolean isAltarInUse(Altars altar) {
        return getUsingPlayer(altar) != null;
    }

    public static Player getUsingPlayer(Altars altar) {
        for(Map.Entry<Altars, Altar> entry : currentlyUsingAltars.entrySet()) {
            if(!entry.getKey().equals(altar)) continue;
            Player online = entry.getValue().getUsing();
            if(online == null || !online.isOnline()) return null;
            return online;
        }

        return null;
    }

    public static Altar getAltar(Player player) {
        for(Altar altar : currentlyUsingAltars.values()) {
            if(altar.getUsing().equals(player)) return altar;
        }
        return null;
    }

    public static void handleLogout(Player player) {
        Altar altar = getAltar(player);
        if(altar == null) return;
        removeAltar(altar, true);
    }

    public static void removeAltar(Altar altar, boolean giveBackStuff) {
        Player player = altar.getUsing();
        if(giveBackStuff) {
            player.playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
            for (ItemStack stack : altar.getNodeStacks()) {
                if(stack == null) continue;
                player.getInventory().addItem(stack);
            }
        }

        for(Item item : altar.getNodeDisplayStacks()) {
            if(item == null) continue;
            item.remove();
        }

        System.out.println("Called remove altar: " + altar.getAltarType());
        currentlyUsingAltars.remove(altar.getAltarType());



    }
}
