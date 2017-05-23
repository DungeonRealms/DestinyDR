package net.dungeonrealms.game.player.duel;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import lombok.Getter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.database.PlayerGameStats.StatColumn;
import net.dungeonrealms.game.mastery.GamePlayer;

import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Chase on Nov 13, 2015
 */
public class DuelOffer {

    public UUID player1;
    public UUID player2;

    @Getter
    public Location centerPoint = null;
    public Location bannerLoc = null;
    private Hologram bannerHologram;
    public boolean canFight = false;

    public boolean cancelled = false;
    public boolean starting = false;
    public int timerID = -1;

    @Getter
    private Map<UUID, Integer> leaveAttempts = new HashMap<>();

    public DuelOffer(Player player, Player player2) {
        this.player1 = player.getUniqueId();
        this.player2 = player2.getUniqueId();
        startFight();

        bannerLoc = centerPoint;

        //Scan down till we get a non air block.
        int maxChecks = 10;
        while (bannerLoc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && bannerLoc.getY() > 0 && maxChecks > 0) {
            maxChecks--;
            bannerLoc.subtract(0, 1, 0);
        }

        Block block = bannerLoc.getBlock();
        if (block.isEmpty() && block.getRelative(BlockFace.DOWN).getType().isSolid()) {
            //Spawn the hologram and stuff if theres no problems.
            block.setType(Material.STANDING_BANNER);
            Banner banner = (Banner) block.getState();
            banner.setBaseColor(DyeColor.RED);
            banner.addPattern(new Pattern(DyeColor.BLACK, PatternType.SKULL));
            banner.update();

            this.bannerHologram = HologramsAPI.createHologram(DungeonRealms.getInstance(), block.getLocation().clone().add(.5, 2.5, .5));
            this.bannerHologram.appendTextLine(ChatColor.YELLOW + ChatColor.BOLD.toString() + "DUEL");
            
            this.bannerHologram.appendTextLine(PlayerWrapper.getWrapper(player).getChatName() +
            		ChatColor.YELLOW + " vs " + PlayerWrapper.getWrapper(player2).getChatName());
        }
    }

    public void endDuel(Player winner, Player loser) {
        canFight = false;
        cancelled = true;
        if (this.bannerLoc.getBlock().getType() == Material.STANDING_BANNER) {
            this.bannerLoc.getBlock().setType(Material.AIR);
        }

        if (this.bannerHologram != null && !this.bannerHologram.isDeleted()) {
            this.bannerHologram.delete();
        }

        if(winner != null && loser != null) {
            GamePlayer wGP = GameAPI.getGamePlayer(winner);
            PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(winner);
            GamePlayer lGP = GameAPI.getGamePlayer(loser);
            PlayerWrapper loserWrapper = PlayerWrapper.getPlayerWrapper(loser);
            if (wGP != null) {
                wGP.setPvpTaggedUntil(0);
                wrapper.getPlayerGameStats().addStat(StatColumn.DUELS_WON);
            }
            
            if (lGP != null) {
                lGP.setPvpTaggedUntil(0);
                loserWrapper.getPlayerGameStats().addStat(StatColumn.DUELS_LOST);
            }

            final String finalWinnerName = wrapper.getChatName();
            final String finalLoserName = loserWrapper.getChatName();

            GameAPI.getNearbyPlayers(winner.getLocation(), 100).forEach(player1 -> player1.sendMessage(finalWinnerName + ChatColor.GREEN + " has " + ChatColor.UNDERLINE + "DEFEATED" + ChatColor.RESET + " " + finalLoserName + ChatColor.GREEN + " in a duel!"));
        }
        DuelingMechanics.removeOffer(this);
    }

    public Player getPlayer1() {
        return Bukkit.getPlayer(player1);
    }

    public boolean isLeftPlayer(Player p) {
        return p.getUniqueId().toString().equalsIgnoreCase(player1.toString());
    }

    public Player getPlayer2() {
        return Bukkit.getPlayer(player2);
    }

    /**
     * @param logOut
     */
    public void handleLogOut(Player logOut) {
        Player winner = null;
        if (logOut.getUniqueId().toString().equalsIgnoreCase(player1.toString())) {
            winner = Bukkit.getPlayer(player2);
        } else {
            winner = Bukkit.getPlayer(player1);
        }

        Bukkit.getLogger().info("Player " + logOut.getName() + " has logged out in a duel.");
        endDuel(winner, logOut);
    }

    /**
     *
     */
    private void startFight() {
        if (starting) return;

        starting = true;
        centerPoint = this.getPlayer1().getLocation();
//        this.getPlayer2().teleport(centerPoint);
        this.getPlayer1().sendMessage(ChatColor.YELLOW + "Duel will begin in 10 seconds...");
        this.getPlayer2().sendMessage(ChatColor.YELLOW + "Duel will begin in 10 seconds...");


        Player player1 = getPlayer1();
        Player player2 = getPlayer2();

        new BukkitRunnable() {
            int timer = 10;

            public void run() {

                if (player1 == null || !player1.isOnline() || player2 == null || !player2.isOnline() || cancelled) {
                    cancel();
                    return;
                }

                timer--;
                if (timer > 0) {
                    player1.sendMessage(ChatColor.YELLOW.toString() + timer + "...");
                    player2.sendMessage(ChatColor.YELLOW.toString() + timer + "...");
                }

                if (timer <= 0) {
                    canFight = true;

                    player1.playSound(player1.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1.3F);
                    player2.playSound(player2.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1.3F);
                    player1.sendMessage(ChatColor.YELLOW + "Fight!");
                    player2.sendMessage(ChatColor.YELLOW + "Fight!");
                    cancel();
                }
            }
        }.runTaskTimer(DungeonRealms.getInstance(), 20, 20);
    }
}
