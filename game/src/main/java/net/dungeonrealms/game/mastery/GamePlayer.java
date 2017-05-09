package net.dungeonrealms.game.mastery;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.handler.ProtectionHandler;
import net.dungeonrealms.game.player.combat.CombatLog;
import net.dungeonrealms.game.title.TitleAPI;

import org.bukkit.*;
import org.bukkit.entity.Player;

/**
 * Created by Nick on 10/19/2015.
 */
@Getter @Setter
public class GamePlayer {

    private boolean sharding = false;
    private Player player;
    
    // Game Master
    private boolean isInvulnerable;
    private boolean isTargettable;
    private boolean isStreamMode;

    private boolean isJailed;

    private boolean ableToDrop;
    private boolean ableToSuicide;
    private boolean ableToOpenInventory;

    private String lastMessager;

    // for forcefield
    private long pvpTaggedUntil;

	public GamePlayer(Player player) {
        this.player = player;
        GameAPI.GAMEPLAYERS.put(player.getName(), this);
        this.isTargettable = true;
        this.isInvulnerable = false;
        this.isStreamMode = false;
        this.lastMessager = null;
        this.pvpTaggedUntil = 0;
    }

    public boolean hasNewbieProtection() {
        return ProtectionHandler.getInstance().hasNewbieProtection(getPlayer());
    }

    public void setInvulnerable(boolean flag) {
        if (CombatLog.isInCombat(getPlayer())) CombatLog.removeFromCombat(getPlayer());
        if (CombatLog.inPVP(getPlayer())) CombatLog.removeFromPVP(getPlayer());
        isInvulnerable = flag;
    }

    public void setPvpTaggedUntil(long time) {
        if (!isPvPTagged())
            TitleAPI.sendActionBar(getPlayer(), ChatColor.RED + "PvP Tagged - " + ChatColor.BOLD + "10s", 4 * 20);
        this.pvpTaggedUntil = time;
    }

    public void setStreamMode(boolean flag) {
        isStreamMode = flag;
    }

    public boolean isPvPTagged() {
        return pvpTaggedUntil > 0 && pvpTaggedUntil > System.currentTimeMillis();
    }

    public int secsPvPTaggedLeft() {
        return isPvPTagged() ? 0 : (int) (pvpTaggedUntil - System.currentTimeMillis()) / 1000;
    }
}
