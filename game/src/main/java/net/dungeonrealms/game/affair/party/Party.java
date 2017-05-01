package net.dungeonrealms.game.affair.party;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.affair.Affair;
import net.dungeonrealms.game.handler.HealthHandler;
import net.dungeonrealms.game.handler.ScoreboardHandler;
import net.dungeonrealms.game.mechanic.dungeons.Dungeon;
import net.dungeonrealms.game.mechanic.dungeons.DungeonManager;
import net.dungeonrealms.game.player.chat.Chat;
import net.dungeonrealms.game.player.chat.GameChat;
import net.dungeonrealms.game.player.json.JSONMessage;
import net.dungeonrealms.game.world.teleportation.TeleportLocation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Party - Get some balloons yo!
 * 
 * Redone on April 30th, 2017
 * @author Kneesnap
 */
public class Party {

    @Getter private Player owner;
    @Getter private List<Player> members;
    @Getter @Setter private boolean updateScoreboard = false;
    private Scoreboard scoreboard;
    @Getter private LootMode lootMode = LootMode.KEEP;
    
    private static final String PREFIX = ChatColor.LIGHT_PURPLE + "<" + ChatColor.BOLD + "P" + ChatColor.LIGHT_PURPLE + "> " + ChatColor.GRAY;
    private static final int MAX_PARTY_SIZE = 8;

    public Party(Player owner) {
    	this(owner, new ArrayList<>());
    }
    
    public Party(Player owner, List<Player> members) {
        this.owner = owner;
        this.members = members;
    }
    
    public boolean isOwner(Player player) {
    	return getOwner() == player;
    }
    
    public boolean isMember(Player player) {
    	return getAllMembers().contains(player);
    }
    
    public Scoreboard getScoreboard() {
    	if (this.scoreboard == null)
    		this.scoreboard = createScoreboard();
    	return this.scoreboard;
    }
    
    public Objective getObjective() {
    	Objective obj = getScoreboard().getObjective("party");
    	if (obj == null) {
    		obj = getScoreboard().registerNewObjective("party", "dummy");
    		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
    	}
    	return obj;
    }

    private Scoreboard createScoreboard() {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        ScoreboardHandler handler = ScoreboardHandler.getInstance();
        handler.setCurrentPlayerLevels(sb);
        handler.registerHealth(sb);
        return sb;
    }

    public void setLootMode(LootMode lootMode) {
        if (this.lootMode == lootMode) return;
        this.lootMode = lootMode;

        List<String> lore = Arrays.asList(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Party Loot Mode", "");
        for (String l : lootMode.getLore())
            lore.add(l.replace("{LEADER}", getOwner().getName()));

        JSONMessage message = new JSONMessage(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "<P> " +
                ChatColor.LIGHT_PURPLE + "Party Loot Mode ");
        message.addHoverText(lore, ChatColor.LIGHT_PURPLE + "changed to " + lootMode.getColor() + ChatColor.BOLD + lootMode.getName());
        message.addText(ChatColor.LIGHT_PURPLE + " by " + ChatColor.WHITE + ChatColor.BOLD + getOwner().getName());
        getAllMembers().forEach((pl) -> {
            message.sendToPlayer(pl);
            pl.sendMessage(ChatColor.GRAY + "Hover over the loot mode to view more info.");
            pl.playSound(pl.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1.4F);
        });
    }

    public List<Player> getAllMembers() {
        List<Player> pls = Arrays.asList(getOwner());
        pls.addAll(getMembers());
        return pls;
    }
    
    public void addMember(Player player) {
    	Affair.getInvitations().remove(player);
    	if (getAllMembers().size() >= MAX_PARTY_SIZE) {
    		player.sendMessage(ChatColor.RED + "The party is full!");
    		return;
    	}
    	
    	getMembers().add(player);
    	player.sendMessage(ChatColor.GREEN + "You have joined the party!");
    	Achievements.getInstance().giveAchievement(player.getUniqueId(), Achievements.EnumAchievements.PARTY_UP);
    }
    
    /**
     * Get the scoreboard display title
     */
    public String getDisplayTitle() {
    	return ChatColor.RED + "" + ChatColor.BOLD + "/ploot - " + getLootMode().getColor() + ChatColor.BOLD + getLootMode().getName();
    }
    
    /**
     * Updates the party scoreboard.
     */
    public void updateScoreboard() {
    	if (getOwner() == null || !getOwner().isOnline()) {
    		disband();
    		return;
    	}
        
    	String title = getDisplayTitle();
    	if (!getObjective().getDisplayName().equals(title))
    		getObjective().setDisplayName(title);
    	
    	for (Player p : getAllMembers()) {
    		// Update their health objective:
    		getObjective().getScore(p.getName()).setScore(HealthHandler.getHP(p));
    		
    		// Verify the scoreboard they see is this one:
    		if (p.getScoreboard() != getScoreboard())
    			p.setScoreboard(getScoreboard());
    	}
    }
    
    public void announce(String msg) {
    	String message = PREFIX + msg;
    	for (Player p : getAllMembers()) {
    		p.sendMessage(message);
    		GameAPI.runAsSpectators(p, s -> s.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "(AS " + p.getName() + ") " + message));
    	}
    }
    
    public void sendChat(Player pl, String message) {
    	String chat = PREFIX + GameChat.getName(pl, Rank.getInstance().getRank(pl.getUniqueId()), true) + ChatColor.GRAY + ": "
        		+ Chat.getInstance().checkForBannedWords(message);
    	
    	JSONMessage admin = Chat.applyShowItem(pl, ChatColor.RED + "" + ChatColor.BOLD + "(AS " + pl.getName() + " ) " + chat);
    	JSONMessage player = Chat.applyShowItem(pl, chat);
    	
    	for (Player p : getAllMembers()) {
    		player.sendToPlayer(p);
    		GameAPI.runAsSpectators(p, admin::sendToPlayer);
    	}
    }
    
    public void invite(Player invitor, Player receiver) {
    	if (getAllMembers().size() >= MAX_PARTY_SIZE) {
    		invitor.sendMessage(ChatColor.RED + "This party already has the max number of players!");
    		return;
    	}
    	
    	Affair.getInvitations().put(receiver, this);
    	receiver.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.UNDERLINE + invitor.getName() + ChatColor.GRAY
    			+ " has invited you to join their party! To accept, type " + ChatColor.LIGHT_PURPLE + "/pccept" + ChatColor.GRAY + " or to decline, type " + ChatColor.LIGHT_PURPLE + "/pdecline");
        invitor.sendMessage(ChatColor.GRAY + "You have invited " + ChatColor.LIGHT_PURPLE + receiver.getDisplayName() + ChatColor.GRAY + " to join your party.");
    }
    
    public void removePlayer(Player player, boolean kick) {
    	if (!isMember(player))
    		return;
    	
    	if (Affair.isPartyChat(player))
    		Affair.getPartyChat().remove(player.getUniqueId());
    	
    	if (isOwner(player)) {
    		disband();
    		return;
    	}
    	
    	getMembers().remove(player);
    	getScoreboard().resetScores(player.getName());
    	
    	player.sendMessage(ChatColor.RED + "You have " + (kick ? "been kicked from" : "left the") + " party.");
    	announce(player.getName() + " " + (kick ? "was kicked from" : "left") + " the party.");
    	player.setScoreboard(ScoreboardHandler.getInstance().mainScoreboard);
    	
    	if (DungeonManager.isDungeon(player))
    		player.teleport(TeleportLocation.CYRENNICA.getLocation());
    }
    
    public Dungeon getDungeon() {
    	for (Player p : getAllMembers())
    		if (DungeonManager.isDungeon(p))
    			return DungeonManager.getDungeon(p.getWorld());
    	return null;
    }
    
    public boolean isDungeon() {
    	return getDungeon() != null;
    }
    
    /**
     * Disband this party.
     */
    public void disband() {
    	for (Player player : getAllMembers()) {
    		// This won't get called recursively due to the dungeon removing the player from it.
    		if (DungeonManager.isDungeon(player))
    			DungeonManager.getDungeon(player.getWorld()).removePlayers(false);
    		
    		Affair.getPartyChat().remove(player.getUniqueId());
    		player.setScoreboard(ScoreboardHandler.getInstance().mainScoreboard);
            player.sendMessage(ChatColor.RED + "Your party has been disbanded.");
    	}
    	
    	Bukkit.getLogger().warning("[Party] Disbanded party owned by '" + getOwner().getName() + "'.");
    	Affair.getParties().remove(this);
    }
}
