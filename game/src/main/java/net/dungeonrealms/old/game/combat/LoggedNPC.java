package net.dungeonrealms.old.game.combat;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.old.game.world.entity.type.NPC;

import java.util.UUID;

/**
 * Created by Matthew E on 10/29/2016 at 12:48 PM.
 */
public class LoggedNPC {

    @Getter
    private NPC npc;

    @Getter
    @Setter
    private int health;
    @Getter
    private final String name;
    @Getter
    private final UUID uuid;

    public LoggedNPC(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.npc = null;
    }

    public boolean isDead() {
        return (health < 0);
    }
}
