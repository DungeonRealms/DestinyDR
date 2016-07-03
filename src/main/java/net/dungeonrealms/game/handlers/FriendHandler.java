package net.dungeonrealms.game.handlers;

import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
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
                player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + itemStack.getItemMeta().getDisplayName().split("'")[0] + ChatColor.GREEN + " from your friends list!");
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
                player.sendMessage(ChatColor.GREEN + "You have successfully cancelled the pending request for " + ChatColor.BOLD + ChatColor.UNDERLINE + itemStack.getItemMeta().getDisplayName().split("'")[0] + ChatColor.GREEN + ".");
                PlayerMenus.openFriendInventory(player);
                break;
            case LEFT:
                //Add Friend
                player.closeInventory();
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PULL, EnumData.FRIEND_REQUSTS, tag.getString("info"), false);
                DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIENDS, friend.toString(), true);
                player.sendMessage(ChatColor.GREEN + "You have successfully added " + ChatColor.BOLD + ChatColor.UNDERLINE + itemStack.getItemMeta().getDisplayName().split("'")[0] + ChatColor.GREEN + ".");
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

        DatabaseAPI.getInstance().update(friend.getUniqueId(), EnumOperators.$PUSH, EnumData.FRIEND_REQUSTS, player.getUniqueId() + "," + (System.currentTimeMillis() / 1000L), true);
        player.sendMessage(ChatColor.GREEN + "Your friend request was successfully sent.");

        friend.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + ChatColor.UNDERLINE + player.getName() + ChatColor.GREEN + " sent you a friend request.");
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

}
