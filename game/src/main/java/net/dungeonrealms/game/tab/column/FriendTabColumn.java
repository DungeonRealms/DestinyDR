package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.database.player.PlayerToken;
import net.dungeonrealms.common.network.ShardInfo;
import net.dungeonrealms.common.network.bungeecord.BungeeServerTracker;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/5/2016
 */
public class FriendTabColumn extends Column {

    @Override
    public Column register() {
        try {
            for (int i = 0; i < 18; i++) {
                int cursor = i;
                variablesToRegister.add(new Variable("friends." + cursor) {
                    @Override
                    public String getReplacement(Player player) {
                        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                        if(wrapper == null) return "";



                        HashMap<UUID, Integer> friends = wrapper.getFriendsList();

                        if (friends.size() == 0) {
                            switch (cursor) {
                                case 0:
                                    return ChatColor.GRAY.toString() + "Type " + ChatColor.GREEN + "/add" + ChatColor.GRAY + " to add someone";
                                case 1:
                                    return ChatColor.GRAY.toString() + "To your buddy list";

                            }
                            return "";
                        }

                        List<String> onlineFriends = new CopyOnWriteArrayList<>();

                        // MAKE SURE FRIENDS ARE ONLINE //
                        friends.keySet().forEach(uuid -> {

                            String playerName = null;
                            ShardInfo shard = null;

                            Player friendPlayer = Bukkit.getPlayer(uuid);

                            if (friendPlayer == null) {

                                Optional<Tuple<PlayerToken, ShardInfo>> curInfo = BungeeServerTracker.grabPlayerInfo(uuid);
                                if (!curInfo.isPresent()) return;

                                PlayerToken playerInfo = curInfo.get().a();
                                if (playerInfo == null) return;

                                playerName = playerInfo.getName();
                                shard = curInfo.get().b();

                            } else {
                                shard = DungeonRealms.getShard();
                                playerName = Bukkit.getPlayer(uuid).getName();
                            }

                            onlineFriends.add(getFormat(playerName, shard));
                        });

                        if (onlineFriends.isEmpty()) if (cursor == 0)
                            return ChatColor.RED + "No friends online!";
                        else return "";
                        try {
                            if (onlineFriends.get(cursor) == null)
                                return "";
                        } catch (Exception ignored) {
                            return "";
                        }

                        return onlineFriends.get(cursor);
                    }
                });
            }
        } catch (NullPointerException ignored) {

        }
        return this;
    }
}