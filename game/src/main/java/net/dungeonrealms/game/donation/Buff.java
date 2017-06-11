package net.dungeonrealms.game.donation;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanic.data.EnumBuff;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.io.Serializable;

/**
 * Created by Alan on 7/28/2016.
 */
@Getter @Setter
public class Buff implements Serializable {
	private static final long serialVersionUID = 1L;
	
	protected EnumBuff type;
	protected int duration;
	protected float bonusAmount;
	protected String activatingPlayer;
	protected String fromServer;
	protected long timeUntilExpiry;
	
	public Buff(EnumBuff type, int duration, int power, String activatingPlayer, String fromServer) {
		this.type = type;
		this.duration = duration;
		this.bonusAmount = power;
		this.activatingPlayer = activatingPlayer;
		this.fromServer = fromServer;
	}
	
	/**
	 * Activate this buff.
	 */
	public void activate() {
		this.timeUntilExpiry = System.currentTimeMillis() + duration * 1000;
		String formattedTime = DurationFormatUtils.formatDurationWords(duration * 1000, true, true);
		Bukkit.getServer().broadcastMessage("");
		Bukkit.getServer().broadcastMessage(
				ChatColor.GOLD + "" + ChatColor.BOLD + ">> " + "(" + fromServer + ") " + ChatColor.RESET + activatingPlayer + ChatColor.GOLD
						+ " has just activated " + ChatColor.UNDERLINE + "+" + bonusAmount + "% " + ChatColor.stripColor(type.getItemName()) + ChatColor.GOLD
						+ " for " + formattedTime + " by using 'Global Level EXP Buff' from the store!");
		Bukkit.getServer().broadcastMessage("");
		
		Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 10f, 1f));
		Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), this::deactivate, duration * 20L);
	}
    
	/**
	 * Deactivate this buff.
	 */
	public void deactivate() {
		final DonationEffects de = DonationEffects.getInstance();
		de.getQueuedBuffs(type).remove(this);
		Buff nextBuff = de.getQueuedBuffs(type).poll();
		
		Bukkit.broadcastMessage(ChatColor.GOLD.toString() + ChatColor.BOLD + ">> " + ChatColor.GOLD + activatingPlayer + "'s " + ChatColor.GOLD.toString() + ChatColor.UNDERLINE
				+ "+" + bonusAmount + "% " + ChatColor.stripColor(type.getItemName()) + ChatColor.GOLD + " has expired.");
		
		if (nextBuff != null)
			nextBuff.activate();
		
		de.saveBuffData();
	}
	
	public String serialize() {
		return new Gson().toJson(this);
	}

	public static Buff deserialize(String serializedBuff) {
		return new Gson().fromJson(serializedBuff, Buff.class);
	}
}