package net.dungeonrealms.game.world.realms;

import net.md_5.bungee.api.ChatColor;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/21/2016
 */
public enum RealmState {

    DOWNLOADING("downloaded"),
    UPLOADING("uploaded"),
    CREATING("created"),
    OPENED("opened"),
    CLOSED("closed"),
    REMOVING("removed"),
    RESETTING("reset"),
    UPGRADING("upgraded");
    
    private final String statusBrief;
    
    RealmState(String status) {
    	this.statusBrief = status;
    }
    
    public String getStatusMessage() {
    	return ChatColor.RED + "Please wait your realm is being " + statusBrief + "...";
    }
}
