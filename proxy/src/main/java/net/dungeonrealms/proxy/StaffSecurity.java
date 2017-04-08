package net.dungeonrealms.proxy;

import java.util.ArrayList;
import java.util.UUID;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class StaffSecurity implements Listener {
	
	@Getter
	private static StaffSecurity instance = new StaffSecurity(); //Lowercase to make getInstance() abide to camelcase.
	
	private ArrayList<UUID> allowedPlayers = new ArrayList<UUID>();
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onServerMove(ServerConnectEvent event) {
		//If the player is leaving the lobby or does not have a set server (Just in case)
		if (event.getPlayer().getServer() != null && event.getPlayer().getServer().getInfo().getName().equals("Lobby")) {
			//If the player is not allowed to change shards yet.
			if (!this.allowedPlayers.contains(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				//There's no way to distinguish player from staff without hooking bungee up to Mongo.
				//To avoid this (and avoid dupes), the message will be generic.
				TextComponent notAllowed = new TextComponent("Please wait before connecting...");
				notAllowed.setColor(ChatColor.RED);
				event.getPlayer().sendMessage(notAllowed);
			}
		}
	}
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent evt){
		this.allowedPlayers.remove(evt.getPlayer().getUniqueId());
	}
	
	public void addAllowedPlayer(UUID uuid){
		if (!this.allowedPlayers.contains(uuid))
			this.allowedPlayers.add(uuid);
	}
}
