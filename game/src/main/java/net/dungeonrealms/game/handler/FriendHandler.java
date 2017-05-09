package net.dungeonrealms.game.handler;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.sql.QueryType;
import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

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
                PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
                wrapper.getFriendsList().remove(friend);

                PlayerWrapper friendWrap = PlayerWrapper.getPlayerWrapper(friend);
                if (friendWrap != null) {
                    friendWrap.getFriendsList().remove(player.getUniqueId());
                }


                SQLDatabaseAPI.getInstance().executeBatch(updated -> {

                });
//                DatabaseAPI.getInstance().update(friend, EnumOperators.$PULL, EnumData.FRIENDS, player.getUniqueId().toString(), true);
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIENDS, friend.toString(), true);
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
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        switch (type) {
            case RIGHT:
                if(wrapper.getPendingFriends().containsKey(friend)) {
                    //Remove Pending request
                    player.closeInventory();
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUESTS, tag.getString("info"), true);
                    wrapper.getPendingFriends().remove(friend);
                    player.sendMessage(ChatColor.GREEN + "You have successfully cancelled the pending request for " + ChatColor.BOLD + ChatColor.UNDERLINE + itemStack.getItemMeta().getDisplayName().split("'")[0] + ChatColor.GREEN + ".");
                    PlayerMenus.openFriendInventory(player);
                }else{
                    player.sendMessage(ChatColor.RED + "That player has not sent you a friend request!");
                }
                break;
            case LEFT:
                //Add Friend
                player.closeInventory();
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUESTS, tag.getString("info"), true);
//                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIENDS, friend.toString(), true);
                String name = itemStack.getItemMeta().getDisplayName().split("'")[0];
                player.sendMessage(ChatColor.GREEN + "You have successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + ".");

                GameAPI.sendNetworkMessage("Friends", "accept:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + friend.toString() + "," + PlayerWrapper.getPlayerWrapper(player).getAccountID());

                acceptFriend(wrapper, friend, name);
//                DatabaseAPI.getInstance().update(friend, EnumOperators.$PUSH, EnumData.FRIENDS, player.getUniqueId().toString(), true);
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
    public void sendRequest(Player sender, Player receiver) {
    	sendRequest(sender, PlayerWrapper.getWrapper(sender).getAccountID(), receiver);
    }
    
    public void sendRequest(Player player, int accountID, Player friend) {
        if (player.getDisplayName().equalsIgnoreCase(friend.getDisplayName())) {
            player.sendMessage(ChatColor.RED + "You cannot add yourself.");
            return;
        }
        if (areFriends(player, friend.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already friends with " + ChatColor.BOLD + ChatColor.UNDERLINE + friend.getDisplayName() + ChatColor.RED + ".");
            return;
        }

        PlayerWrapper friendWrapper = PlayerWrapper.getPlayerWrapper(friend.getUniqueId());
        friendWrapper.getPendingFriends().put(player.getUniqueId(), accountID);
//        DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUESTS, player.getUniqueId().toString(), true);
        player.sendMessage(ChatColor.GREEN + "Your friend request was successfully sent.");

        friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + player.getName() + ChatColor.GREEN + " sent you a friend request.");
        friend.sendMessage(ChatColor.GREEN + "Use /accept (player) to accept.");

    }

    /**
     * Send a friend request over network, NO CHECKS.
     */
    public void sendRequestOverNetwork(Player player, String uuid, int accountID) {
        GameAPI.sendNetworkMessage("Friends", "request:" + " ," + player.getUniqueId().toString() + "," + player.getName() + "," + uuid + "," + accountID);
        player.sendMessage(ChatColor.GREEN + "Your friend request was successfully sent.");
    }


    /**
     * @param uuid
     * @return list off UUIDs as String.
     */
    public HashMap<UUID, Integer> getFriendsList(UUID uuid) {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(uuid);
        if (wrapper == null) return null;
        return wrapper.getFriendsList();

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

        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);
        if (wrapper == null) return false;
        HashMap<UUID, Integer> friends = wrapper.getFriendsList();

        if (friends.containsKey(uuid)) {
            return true;
        }

        if (wrapper.getPendingFriends().containsKey(uuid)) return true;

        return false;
    }


    public boolean isPendingFrom(PlayerWrapper wrapper, UUID other) {
        return wrapper.getPendingFriends().containsKey(other);
    }

    public void acceptFriend(PlayerWrapper wrapper, UUID friend, String name) {

        int friendID = SQLDatabaseAPI.getInstance().getAccountIdFromUUID(friend);
        wrapper.getPendingFriends().remove(friend);
        wrapper.getFriendsList().put(friend, friendID);

        PlayerWrapper pWrap = PlayerWrapper.getPlayerWrapper(friend);
        if (pWrap != null) {
            //Register this?
            pWrap.getFriendsList().put(wrapper.getUuid(), wrapper.getAccountID());
        }
        SQLDatabaseAPI.getInstance().executeBatch(rs -> Bukkit.getScheduler().scheduleSyncDelayedTask(DungeonRealms.getInstance(),
                () -> wrapper.getPlayer().sendMessage(ChatColor.GREEN + "You have successfully added " + ChatColor.BOLD +
                        ChatColor.UNDERLINE + name + ChatColor.GREEN + ".")),
                QueryType.INSERT_FRIENDS.getQuery(wrapper.getAccountID(), friendID, "friends", "friends"),
                QueryType.INSERT_FRIENDS.getQuery(friendID, wrapper.getAccountID(), "friends", "friends"));
        //DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$PULL, EnumData.FRIEND_REQUESTS, friend.toString(), true);
//        DatabaseAPI.getInstance().update(uniqueId, EnumOperators.$PUSH, EnumData.FRIENDS, friend.toString(), true);
//        wrapper.getPlayer().sendMessage(ChatColor.GREEN + "You have successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + ".");

    }
}
