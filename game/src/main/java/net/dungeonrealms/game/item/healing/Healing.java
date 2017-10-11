package net.dungeonrealms.game.item.healing;

import lombok.Getter;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Healing {

    protected static DecimalFormat format = new DecimalFormat("#,###.#");

    protected static Map<UUID, HealingMap> healingMap = new HashMap<>();

    public abstract boolean onAbilityUse(Player player, HealingAbility ability, ItemClickEvent event);

    @Getter
    public class HealingMap {
        private UUID uuid;
        private long time;

        public boolean canHeal(UUID uuid) {
            if (this.uuid == null) return true;
            if (!this.uuid.equals(uuid)) {
                //Need to check time.
                if (time <= System.currentTimeMillis()) {
                    this.uuid = null;
                    return true;
                }
                return false;
            }

            if (this.uuid.equals(uuid)) {
                return true;
            }

            return false;
        }

        public void heal(UUID healer) {
            this.uuid = healer;
            this.time = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        }
    }
}
