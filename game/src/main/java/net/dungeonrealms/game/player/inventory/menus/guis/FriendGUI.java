package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.common.game.database.sql.SQLDatabaseAPI;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.handler.FriendHandler;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Map;
import java.util.UUID;

public class FriendGUI extends GUIMenu {
    private boolean showFriends = false;

    public FriendGUI(Player player, GUIMenu previous, boolean showFriends) {
        super(player, 45, "Friend Management", previous);
        this.showFriends = showFriends;
    }

    @Override
    protected void setItems() {
        PlayerWrapper wrapper = PlayerWrapper.getPlayerWrapper(player);


        if (showFriends) {
            setItem(getSize() - 1, new GUIItem(ItemManager.createItem(Material.BARRIER, ChatColor.GREEN + "Back",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to go back!")).setClick(e -> {
                showFriends = false;
                clear();
                setItems();
            }));

            int slot = 9;
            for (Map.Entry<UUID, Integer> entry : wrapper.getFriendsList().entrySet()) {
                String name = SQLDatabaseAPI.getInstance().getUsernameFromAccountID(entry.getValue());

                setItem(slot, new GUIItem(ItemManager.createItem(Material.SKULL_ITEM, ChatColor.GREEN + name, (short) 3, ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to delete!")).setSkullOwner(name)
                        .setClick(e -> {
                            if (e.getClick() == ClickType.RIGHT) {
                                //Remove Pending request
                                player.closeInventory();
                                FriendHandler.getInstance().remove(player, wrapper, entry.getKey(), entry.getValue());
                                player.sendMessage(ChatColor.GREEN + "You have deleted " + ChatColor.BOLD + ChatColor.UNDERLINE + name + ChatColor.GREEN + " from your friends list!");
                                setItems();
                            }
                        }));

                if (slot >= 54) break;
                slot++;
            }
        } else {
            setItem(0, new GUIItem(ItemManager.createItem(Material.BOOK_AND_QUILL, ChatColor.GREEN + "Add Friend",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to add friend!")).setClick(e -> {
                player.sendMessage(ChatColor.GREEN + "Please enter the name of the player you would like to add...");
                Chat.listenForMessage(player, chat -> {
                    Player target = Bukkit.getPlayer(chat.getMessage());
                    if (target != null) {
                        FriendHandler.getInstance().sendRequest(player, target);
                        player.sendMessage(ChatColor.GREEN + "Friend request sent to " + ChatColor.BOLD + target.getName() + ChatColor.GREEN + ".");
                    } else {
                        player.sendMessage(ChatColor.RED + "Oops, I can't find that player!");
                    }
                }, p -> p.sendMessage(ChatColor.RED + "Action cancelled."));
            }));

            setItem(1, new GUIItem(ItemManager.createItem(Material.CHEST, ChatColor.GREEN + "View Friend",
                    ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to view friend!")).setClick(e -> {
                showFriends = true;
                clear();
                setItems();
            }));

            int slot = 9;
            for (Map.Entry<UUID, Integer> from : wrapper.getPendingFriends().entrySet()) {
                String name = SQLDatabaseAPI.getInstance().getUsernameFromUUID(from.getKey());
                setItem(slot, new GUIItem(ItemManager.createItem(Material.SKULL_ITEM, ChatColor.GREEN + name, (short) 3,
                        ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Left-Click " + ChatColor.GRAY + "to accept!",
                        ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "Right-Click " + ChatColor.GRAY + "to deny!")).setSkullOwner(name)
                        .setClick(e -> FriendHandler.getInstance().addOrRemove(player, e.getClick(), from.getKey(), name)));
                if (slot >= 44) break;
                slot++;
            }

            setItem(getSize() - 1, getBackButton());
        }
    }
}
