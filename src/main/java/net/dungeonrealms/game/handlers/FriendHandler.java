package net.dungeonrealms.game.handlers;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
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
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIENDS, friend.toString(), true);
                sendFriendMessage(player, ChatColor.GREEN + "You have deleted " + itemStack.getItemMeta().getDisplayName().split("'")[0] + " from your friends list!");
                PlayerMenus.openFriendInventory(player);
                DatabaseAPI.getInstance().update(friend, EnumOperators.$PULL, EnumData.FRIENDS, player.toString(), true);
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
                sendFriendMessage(player, ChatColor.GREEN + "You have successfully removed pending request for " + itemStack.getItemMeta().getDisplayName().split("'")[0]);
                PlayerMenus.openFriendInventory(player);
                break;
            case LEFT:
                //Add Friend
                player.closeInventory();
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, tag.getString("info"), true);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIENDS, friend.toString(), true);
                sendFriendMessage(player, ChatColor.GREEN + "You have successfully added " + ChatColor.AQUA + itemStack.getItemMeta().getDisplayName().split("'")[0]);
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
        if (areFriends(player, friend.getUniqueId())) return;

        DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUSTS, player.getUniqueId() + "," + (System.currentTimeMillis() / 1000L), true);
        sendFriendMessage(player, ChatColor.GREEN + "Friend request was successfully sent.");

        sendFriendMessage(friend, ChatColor.AQUA + player.getName() + ChatColor.GREEN + " sent you a friend request! Check your friend management UI to accept / deny!");
        //NetworkAPI.getInstance().sendNetworkMessage("player", "update", friend.getName());
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

    /**
     * simple method to format messages for future.
     *
     * @param player  Player
     * @param message Message
     * @since 1.0
     */
    public void sendFriendMessage(Player player, String message) {
        player.sendMessage(ChatColor.WHITE + "[" + ChatColor.GREEN.toString() + ChatColor.BOLD + "FRIENDS" + ChatColor.WHITE + "] " + ChatColor.RESET + message);
    }

}
