package net.dungeonrealms.game.world.spar;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * Created by Nick on 12/15/2015.
 */
public class Battle {

    private Player player1;
    private ArrayList<ItemStack> player1Bet;

    private Player player2;
    private ArrayList<ItemStack> player2Bet;

    private Location bannerLocation;

    private Spar.SparWorlds sparWorld;
    private String worldName;

    private SparArmor armorTier;
    private SparWeapon weaponTier;

    private long startTime = 0;
    private long time = 0;

    private ArrayList<Player> spectators;

    public Battle(Player player1, ArrayList<ItemStack> player1Bet, Player player2, ArrayList<ItemStack> player2Bet, Location bannerLocation, Spar.SparWorlds sparWorld, String worldName, SparArmor armorTier, SparWeapon weaponTier, long startTime, long time, ArrayList<Player> spectators) {
        this.player1 = player1;
        this.player1Bet = player1Bet;
        this.player2 = player2;
        this.player2Bet = player2Bet;
        this.bannerLocation = bannerLocation;
        this.sparWorld = sparWorld;
        this.worldName = worldName;
        this.armorTier = armorTier;
        this.weaponTier = weaponTier;
        this.startTime = startTime;
        this.time = time;
        this.spectators = spectators;
    }

    public Battle start() {
        new Spar().startBattle(this);
        return this;
    }

    public void incTime() {
        this.time += 1;
    }

    public Player getPlayer1() {
        return player1;
    }

    public ArrayList<ItemStack> getPlayer1Bet() {
        return player1Bet;
    }

    public Player getPlayer2() {
        return player2;
    }

    public ArrayList<ItemStack> getPlayer2Bet() {
        return player2Bet;
    }

    public Location getBannerLocation() {
        return bannerLocation;
    }

    public Spar.SparWorlds getSparWorld() {
        return sparWorld;
    }

    public String getWorldName() {
        return worldName;
    }

    public SparArmor getArmorTier() {
        return armorTier;
    }

    public SparWeapon getWeaponTier() {
        return weaponTier;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getTime() {
        return time;
    }

    public ArrayList<Player> getSpectators() {
        return spectators;
    }
}
