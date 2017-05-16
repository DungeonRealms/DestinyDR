package net.dungeonrealms.game.player.inventory.menus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dungeonrealms.game.item.items.functional.ecash.ItemDPSDummy;
import net.dungeonrealms.game.mechanic.ParticleAPI;
import net.dungeonrealms.game.world.item.DamageAPI;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class DPSDummy {

    private Entity entity;
    private Location location;
    private UUID owner;
    private String ownerName;

    public void spawnHologram(Player player, int damage) {
        DamageAPI.createDamageHologram(player, entity.getLocation().clone().add(0, 1, 0), damage);
    }

    public void destroy() {
        ItemDPSDummy.dpsDummies.remove(entity);
        entity.remove();
        ParticleAPI.sendParticleToLocationAsync(ParticleAPI.ParticleEffect.CRIT, location.clone().add(0, 1, 0), .5F, .5F, .5F, 1F, 30);
    }
}
