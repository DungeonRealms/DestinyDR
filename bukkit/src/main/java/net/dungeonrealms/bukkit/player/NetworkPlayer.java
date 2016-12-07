package net.dungeonrealms.bukkit.player;

import net.dungeonrealms.bukkit.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by Evoltr on 12/7/2016.
 */
public class NetworkPlayer {

    private String uuid;
    private String name;
    private String ip;

    private Rank rank;
    private long loginTime;

    public NetworkPlayer(String uuid, String name, String ip, Rank rank) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.rank = rank;
        this.loginTime = System.currentTimeMillis();
    }

    public String getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return ip;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public void sendMessage(String msg) {
        getBukkitPlayer().sendMessage(msg);
    }

    public int getTimeOnline() {
        return (int) (System.currentTimeMillis() - loginTime) / 1000;
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayerExact(name);
    }
}
