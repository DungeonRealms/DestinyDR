package net.dungeonrealms.vgame.anticheat.flag;

import com.google.common.collect.Maps;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.awt.SuperHandler;
import net.dungeonrealms.common.network.bungeecord.BungeeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Copyright © 2016 Matthew E Development - All Rights Reserved
 * You may NOT use, distribute and modify this code.
 * <p>
 * Created by Matthew E on 10/30/2016 at 12:24 PM.
 */
public class FlagHandler implements SuperHandler.Handler {

    public static HashMap<Player, Flag> flagMap;
    private static FlagHandler instance;

    public static FlagHandler getInstance() {
        return instance;
    }

    @Override
    public void prepare() {
        instance = this;
        this.flagMap = Maps.newHashMap();
    }

    public void flag(Player player, Hack hack) {
        if (!flagMap.containsKey(player)) {
            Flag flag = new Flag(player);
            flag.setFlightFlags(0);
            flag.setSpeedFlags(0);
            flagMap.put(player, flag);
        }
        switch (hack) {
            case SPEED:
                flagMap.get(player).addSpeedFlag();
                break;
            case FLIGHT:
                flagMap.get(player).addFightFlag();
                break;
            default:
                break;
        }
        Flag flag = flagMap.get(player);
        switch (flag.getFlightFlags()) {
            case 20:
            case 50:
            case 100:
            case 200:
            case 500:
            case 1000:
            case 2000:
                GameAPI.sendNetworkMessage("GMMessage", "&c<&lHACK&c> &c(" + DungeonRealms.getInstance().shardid + ") &7" + player.getName() + " flight &6LVL &c" + flag.getFlightFlags());
                break;
            default:
                break;
        }
        if (flag.getFlightFlags() > 2000) {
            kickPlayer(player, Hack.FLIGHT);
            return;
        }
        switch (flag.getSpeedFlags()) {
            case 20:
            case 50:
            case 100:
            case 200:
            case 500:
            case 1000:
            case 2000:
                GameAPI.sendNetworkMessage("GMMessage", "&c<&lHACK&c> &c(" + DungeonRealms.getInstance().shardid + ") &7" + player.getName() + " speed &6LVL &c" + flag.getSpeedFlags());
                break;
            default:
                break;
        }
        if (flag.getSpeedFlags() > 4000) {
            kickPlayer(player, Hack.SPEED);
            return;
        }
    }

    private void kickPlayer(Player player, Hack hack) {
        GameAPI.sendNetworkMessage("GMMessage", "&c<&lHACK&c> &c(" + DungeonRealms.getInstance().shardid + ") &7" + player.getName() + " kicked for " + hack.toString());
        BungeeUtils.sendNetworkMessage("BungeeCord", "KickPlayer", player.getName(), ChatColor.RED + "Kicked for " + hack.toString());
        flagMap.remove(player);
        return;
    }
}
