package net.dungeonrealms.game.mechanic.dungeons;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.world.item.itemgenerator.ItemGenerator;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Varenglade Dungeon
 * <p>
 * Created April 29th, 2017.
 *
 * @author Kneesnap
 */
public class Varenglade extends Dungeon {

    @Getter
    @Setter
    public int dropped = 0;

    public Varenglade(List<Player> players) {
        super(DungeonType.VARENGLADE, players);
    }

    public static ItemStack getKey() {
        return ItemGenerator.getNamedItem("DOkey");
    }

    public static class VarengladeListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void monsterDeath(EntityDeathEvent event) {
            World w = event.getEntity().getWorld();
            if (!DungeonManager.isDungeon(w, DungeonType.VARENGLADE))
                return;

            Dungeon d = DungeonManager.getDungeon(w);
            Varenglade varenglade = (Varenglade) d;

            if (d == null || d.hasSpawned(BossType.BurickPriest) || ThreadLocalRandom.current().nextInt(10) > 7 || varenglade.getDropped() >= 10)
                return;


            Player killer = event.getEntity().getKiller();

            if (killer != null) {
                GameAPI.giveOrDropItem(killer, getKey());
            } else {
                w.dropItem(event.getEntity().getLocation().add(0, 1, 0), getKey());
            }
            varenglade.setDropped(varenglade.getDropped() + 1);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void playerWalk(PlayerMoveEvent event) {
            World w = event.getPlayer().getWorld();
            if (!DungeonManager.isDungeon(w, DungeonType.VARENGLADE))
                return;

            if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

            if (event.getTo().getY() >= 80) {
                event.getPlayer().teleport(w.getSpawnLocation());
            }
        }
    }
}
