package net.dungeonrealms.game.handlers;

import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.database.type.EnumData;
import net.dungeonrealms.common.game.database.type.EnumOperators;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by Nick on 10/22/2015.
 */
@SuppressWarnings("unchecked")
public class FriendHandler {

    static FriendHandler instance = null;

    public static FriendHandler getInstance() {
        if (instance == null) {
            instance = new FriendHandler();
        }
        return instance;
    }

    /**
     * for "Friends" GUI.
     *
     * @param player
     * @param type
     * @param itemStack
     * @since 1.0
     */
    public void remove(Player player, ClickType type, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == null || !(itemStack.getType().equals(Material.SKULL_ITEM)))
            return;
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getTag();
        UUID friend = UUID.fromString(tag.getString("info"));

        switch (type) {
            case RIGHT:
                //Remove Pending request
                player.closeInventory();
                DatabaseAPI.getInstance().update(friend, EnumOperators.$PULL, EnumData.FRIENDS, player.getUniqueId().toString(), true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIENDS, friend.toString(), true);
                player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + itemStack.getItemMeta().getDisplayName().split("'")[0] + ChatColor.GREEN + " from your friends list!");
                PlayerMenus.openFriendInventory(player);

                break;
        }
    }

    /**
     * for "Friend Management" Gui.
     *
     * @param player
     * @param type
     * @param itemStack
     * @since 1.0
     */
    public void addOrRemove(Player player, ClickType type, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == null || !(itemStack.getType().equals(Material.SKULL_ITEM)))
            return;
        NBTTagCompound tag = CraftItemStack.asNMSCopy(itemStack).getTag();
        UUID friend = UUID.fromString(tag.getString("info").split(",")[0]);

        switch (type) {
            case RIGHT:
                //Remove Pending request
                player.closeInventory();
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, tag.getString("info"), true);
                player.sendMessage(ChatColor.GREEN + "You have successfully cancelled the pending request for " + ChatColor.BOLD + ChatColor.UNDERLINE + itemStack.getItemMeta().getDisplayName().split("'")[0] + ChatColor.GREEN + ".");
                PlayerMenus.openFriendInventory(player);
                break;
            case LEFT:
                //Add Friend
                player.closeInventory();
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, tag.getString("info"), true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIENDS, friend.toString(), true);
                String name = itemStack.getItemMeta().getDisplayName().split("'")[0];
                player.sendMessage(ChatColor.GREEN + "You have successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + ".");

                UUID uuid = UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(name));
                GameAPI.sendNetworkMessage("Friends", "accept:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + uuid.toString());
                FriendHandler.getInstance().acceptFriend(player.getUniqueId(), uuid, name);

                DatabaseAPI.getInstance().update(friend, EnumOperators.$PUSH, EnumData.FRIENDS, player.getUniqueId().toString(), true);
                break;
        }
    }

    /**
     * Send a friend request, ALREADY PERFORMS CHECKS.
     *
     * @param player The invoker.
     * @param friend Wanting to add.
     * @since 1.0
     */
    public void sendRequest(Player player, Player friend) {
        if (player.getDisplayName().equalsIgnoreCase(friend.getDisplayName())) {
            player.sendMessage(ChatColor.RED + "You cannot add yourself.");
            return;
        }
        if (areFriends(player, friend.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already friends with " + ChatColor.BOLD + ChatColor.UNDERLINE + friend.getDisplayName() + ChatColor.RED + ".");
            return;
        }

        DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUSTS, player.getUniqueId().toString(), true);
        player.sendMessage(ChatColor.GREEN + "Your friend request was successfully sent.");

        friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + player.getName() + ChatColor.GREEN + " sent you a friend request.");
        friend.sendMessage(ChatColor.GREEN + "Use /accept (player) to accept.");

    }

    /**
     * Send a friend request over network, NO CHECKS.
     */
    public void sendRequestOverNetwork(Player player, String uuid) {
        GameAPI.sendNetworkMessage("Friends", "request:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + uuid);
        player.sendMessage(ChatColor.GREEN + "Your friend request was successfully sent.");
    }


    /**
     * @param uuid
     * @return list off UUIDs as String.
     */
    public ArrayList<String> getFriendsList(UUID uuid) {
        return (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, uuid);

    }


    /**
     * Will check and determine if the players are friends or have a pending
     * friend request.
     *
     * @param player Main player
     * @param uuid   The other player
     * @return
     * @since 1.0
     */
    public boolean areFriends(Player player, UUID uuid) {
        if (player.getUniqueId().equals(uuid)) return true;

        ArrayList<String> friends = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIENDS, player.getUniqueId());

        if (friends.contains(uuid.toString())) {
            return true;
        }

        ArrayList<String> pendingRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, uuid);

        long pendingRequests = pendingRequest.stream().filter(s -> s.startsWith(uuid.toString())).count();

        return pendingRequests >= 1;
    }

    public static void sendMessageToFriend(Player player, String playerName, String finalMessage) {
        GameAPI.submitAsyncWithAsyncCallback(() -> {
            String testUUID = DatabaseAPI.getInstance().getUUIDFromName(playerName);
            if (testUUID.equals("")) {
                player.sendMessage(ChatColor.RED + "It seems this user has not played DungeonRealms before.");
                return "";
            }
            UUID uuid = UUID.fromString(testUUID);
            if (!FriendHandler.getInstance().areFriends(player, uuid) && !Rank.getInstance().isGM(Bukkit.getOfflinePlayer(uuid))) {
                if (!(Boolean) DatabaseAPI.getInstance().getData(EnumData.TOGGLE_RECEIVE_MESSAGE, uuid)) {
                    player.sendMessage(ChatColor.RED + "This user is only accepting messages from friends.");
                    return "";
                }
            }
            if (!((Boolean)DatabaseAPI.getInstance().getData(EnumData.IS_PLAYING, uuid))) {
                player.sendMessage(ChatColor.RED +"That user is not currently online.");
                return "";
            }
            try {
                return DatabaseAPI.getInstance().getFormattedShardName(uuid);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }, result -> {
            String receivingShard = null;
            try {
                receivingShard = result.get();
                if (receivingShard.equals("")) {
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            String toPlayerRank = Rank.getInstance().getRank(UUID.fromString(DatabaseAPI.getInstance().getUUIDFromName(playerName)));
            String fromPlayerRank = Rank.getInstance().getRank(player.getUniqueId());
            player.sendMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + "TO " + GameChat.getRankPrefix
                    (toPlayerRank) + GameChat.getName(playerName, toPlayerRank, true) + ChatColor.GRAY + " [" +
                    ChatColor.AQUA + receivingShard + ChatColor.GRAY + "]: " + ChatColor.WHITE + finalMessage);

            GameAPI.sendNetworkMessage("FriendMessage", player.getName(), playerName, (ChatColor.GRAY.toString() +
                    ChatColor.BOLD + "FROM " + GameChat.getRankPrefix(fromPlayerRank) + GameChat.getName(player, fromPlayerRank, true) +
                    ChatColor.GRAY + " [" + ChatColor.AQUA + receivingShard + ChatColor.GRAY + "]: " + ChatColor
                    .WHITE + finalMessage));
            GameAPI.sendNetworkMessage("BroadcastSound", Sound.ENTITY_EGG_THROW.toString(), "2f", "1.2f");
        });
    }

    public boolean isPendingFrom(UUID uuid, String name) {
        ArrayList<String> pendingRequest = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.FRIEND_REQUSTS, uuid);
        String friendUUID = DatabaseAPI.getInstance().getUUIDFromName(name);
        long pendingRequests = pendingRequest.stream().filter(s -> s.startsWith(friendUUID)).count();

        return pendingRequests >= 1;

    }

    public void acceptFriend(UUID uniqueId, UUID friend, String name) {

        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, friend.toString(), true);

        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$PUSH, EnumData.FRIENDS, friend.toString(), true);
        Bukkit.getPlayer(uniqueId).sendMessage(ChatColor.GREEN + "You have successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + ".");

    }
}
