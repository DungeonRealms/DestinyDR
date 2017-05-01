package net.dungeonrealms.game.item.items.functional.ecash;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;
import net.dungeonrealms.game.item.items.functional.FunctionalItem;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;

public class ItemGlobalMessager extends FunctionalItem implements ItemClickListener {

	public ItemGlobalMessager() {
		super(ItemType.GLOBAL_MESSAGER);
		setPermUntradeable(true);
	}
	
	public ItemGlobalMessager(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player player = evt.getPlayer();
		if (PunishAPI.isMuted(player.getUniqueId())) {
			player.sendMessage(PunishAPI.getMutedMessage(player.getUniqueId()));
            return;
        }

		player.sendMessage("");
		player.sendMessage(ChatColor.YELLOW + "Please enter the message you'd like to send to " + ChatColor.UNDERLINE + "all servers" + ChatColor.YELLOW
                + " -- think before you speak!");
		player.sendMessage(ChatColor.GRAY + "Type 'cancel' (no apostrophes) to cancel this and get your Global Messenger back.");
		player.sendMessage("");
        
		evt.setUsed(true);
		evt.setCancelled(false);
		
        Chat.listenForMessage(player, chat -> {
            if (chat.getMessage().equalsIgnoreCase("cancel") || chat.getMessage().equalsIgnoreCase("c")) {
            	player.getInventory().addItem(getItem());
            	player.sendMessage(ChatColor.RED + "Global Messenger - " + ChatColor.BOLD + "CANCELLED");
                return;
            }

            String msg = chat.getMessage();
            if (msg.contains(".com") || msg.contains(".net") || msg.contains(".org") || msg.contains("http://") || msg.contains("www.")) {
                if (!Rank.isDev(player)) {
                	player.getInventory().addItem(getItem());
                	player.sendMessage(ChatColor.RED + "No " + ChatColor.UNDERLINE + "URL's" + ChatColor.RED + " in your global messages please!");
                    return;
                }
            }

            final String fixedMessage = Chat.getInstance().checkForBannedWords(msg);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Alert");
            out.writeUTF(" \n" + ChatColor.GOLD.toString() + ChatColor.BOLD + ">>" + ChatColor.GOLD + " (" + DungeonRealms.getInstance().shardid + ") " + GameChat.getPreMessage(player) + ChatColor.GOLD + fixedMessage + "\n ");

            player.sendPluginMessage(DungeonRealms.getInstance(), "BungeeCord", out.toByteArray());
        }, p -> {
        	player.getInventory().addItem(getItem());
            p.sendMessage(ChatColor.RED + "Action cancelled.");
        });
	}
	
	@Override
	protected String getDisplayName() {
		return ChatColor.GOLD + "Global Messenger";
	}

	@Override
	protected String[] getLore() {
		return new String[] {
				ChatColor.GOLD + "Uses: " + ChatColor.GRAY + "1",
				"Sends a message to all players on " + ChatColor.UNDERLINE + "ALL SHARDS."};
	}
	
	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT;
	}

	@Override
	protected ItemStack getStack() {
		return new ItemStack(Material.FIREWORK);
	}
}
