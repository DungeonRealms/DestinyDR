package net.dungeonrealms.game.tab.column;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.game.handler.FriendHandler;
import net.dungeonrealms.game.tab.Column;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Class written by APOLLOSOFTWARE.IO on 8/5/2016
 */
public class FriendTabColumn extends Column {

    @Override
    public Column register() {
        for (int i = 0; i < 18; i++) {
            int cursor = i;
            variablesToRegister.add(new Variable("friends." + cursor) {
                @Override
                public String getReplacement(Player player) {
                    if (!DatabaseAPI.getInstance().PLAYERS.containsKey(player.getUniqueId())) return "";

                    List<String> friends = new CopyOnWriteArrayList<>(FriendHandler.getInstance().getFriendsList(player.getUniqueId()));

                    if (friends.size() == 0) {
                        switch (cursor) {
                            case 0:
                                return ChatColor.GRAY.toString() + "Type " + ChatColor.GREEN + "/add" + ChatColor.GRAY + " to add someone";
                            case 1:
                                return ChatColor.GRAY.toString() + "To your buddy list";

                        }
                        return "";
                    }

                    // MAKE SURE FRIENDS ARE ONLINE //
                    friends.stream().filter(name -> Bukkit.getPlayer(name) == null).forEach(friends::remove);

                    if (friends.isEmpty()) if (cursor == 0)
                        return ChatColor.RED + "No friends online";
                    else return "";
                    try {
                        if (friends.get(cursor) == null)
                            return "";
                    } catch (Exception ignored) {
                        return "";
                    }

                    return ChatColor.GREEN + friends.get(cursor);
                }
            });
        }
        return this;
    }
}