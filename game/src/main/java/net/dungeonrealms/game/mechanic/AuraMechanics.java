package net.dungeonrealms.game.mechanic;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.item.items.core.Aura;
import net.dungeonrealms.game.item.items.functional.ItemLootAura;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AuraMechanics implements GenericMechanic {
    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CARDINALS;
    }

    //    private static DecimalFormat format = new DecimalFormat("#.#");
    @Override
    public void startInitialization() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Aura aura : ItemLootAura.activeAuras) {

                    List<AtomicInteger> aur = aura.getSeconds();
                    if (aur.size() > 0) {

                        Iterator<AtomicInteger> iter = aur.iterator();
                        while (iter.hasNext()) {
                            AtomicInteger time = iter.next();
                            if (time.get() <= 1) {
                                iter.remove();
                            } else {
                                time.decrementAndGet();
                            }
                        }

                        if (aur.size() > 0)
                            aura.update();

                    } else {

                        ItemLootAura.activeAuras.remove(aura);
                        aura.remove();

                        Player owner = Bukkit.getPlayer(aura.getName());
                        if (owner != null)
                            owner.sendMessage(ChatColor.RED + "Your " + (int) aura.getMultiplier() + "% " + aura.getType().getName(true) + " Aura " + ChatColor.RED + "has expired.");

                        ParticleAPI.spawnParticle(Particle.FIREWORKS_SPARK, aura.getCenter(), 30, .5F, .01F);
                        aura.getCenter().getWorld().playSound(aura.getCenter(), Sound.BLOCK_LAVA_EXTINGUISH, 1, 1.4F);
                    }
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 20, 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Aura aura : ItemLootAura.activeAuras) {
                    //Play AOE around it.
                    aura.playParticle();
                }
            }
        }.runTaskTimerAsynchronously(DungeonRealms.getInstance(), 20, 1);
    }

    @Override
    public void stopInvocation() {

    }
}
