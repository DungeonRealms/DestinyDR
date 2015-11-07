package net.dungeonrealms.world;

import de.inventivegames.npc.NPC;
import de.inventivegames.npc.NPCLib;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.mechanics.generic.EnumPriority;
import net.dungeonrealms.mechanics.generic.GenericMechanic;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nick on 11/7/2015.
 */
public class Mercenary implements GenericMechanic {

    static Mercenary instance = null;

    public static Mercenary getInstance() {
        if (instance == null) {
            instance = new Mercenary();
        }
        return instance;
    }

    ConcurrentHashMap<Player, NPC> _mercenaries = new ConcurrentHashMap<>();

    public void invokeMercenary(Player player, int tier) {
        NPC npc = NPCLib.spawnPlayerNPC(player.getLocation(), ChatColor.RED + "T4" + " " + ChatColor.GREEN + "Mercenary", "Obama");

        npc.getBukkitEntity().getEquipment().setHelmet(new ItemStack(Material.GOLD_HELMET));
        npc.getBukkitEntity().getEquipment().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
        npc.getBukkitEntity().getEquipment().setLeggings(new ItemStack(Material.GOLD_LEGGINGS));
        npc.getBukkitEntity().getEquipment().setBoots(new ItemStack(Material.GOLD_BOOTS));
        npc.getBukkitEntity().getEquipment().setItemInHand(new ItemStack(Material.GOLD_SWORD));

        npc.teleport(player.getLocation());
        npc.lookAt(player.getEyeLocation());

        _mercenaries.put(player, npc);

    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.BISHOPS;
    }

    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DungeonRealms.getInstance(), () -> {
            _mercenaries.entrySet().stream().forEach(e -> {
                if (e.getValue().getBukkitEntity().getLocation().distanceSquared(e.getValue().getLocation()) > 140) {
                    e.getValue().teleport(e.getKey().getLocation());
                } else {
                    e.getValue().pathfindTo(e.getKey().getLocation().add(2, 0, 0));
                }
                e.getValue().lookAt(e.getKey().getEyeLocation());
            });
        }, 0, 20);
    }

    @Override
    public void stopInvocation() {
        _mercenaries.entrySet().stream().forEach(e -> {
            e.getValue().despawn();
        });
    }
}
