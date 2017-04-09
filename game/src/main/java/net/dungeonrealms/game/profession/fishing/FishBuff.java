package net.dungeonrealms.game.profession.fishing;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.game.mastery.AttributeList;
import net.dungeonrealms.game.mastery.GamePlayer;
import net.dungeonrealms.game.mechanic.data.FishingTier;
import net.dungeonrealms.game.profession.Fishing.EnumFish;
import net.dungeonrealms.game.profession.Fishing.FishBuffType;
import net.dungeonrealms.game.world.item.Item.AttributeType;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_9_R2.NBTTagCompound;

/**
 * Buffs that get put on fish.
 * 
 * Created April 8th, 2017.
 * @author Kneesnap
 */
public abstract class FishBuff {
	
	@Getter @Setter
	private FishingTier tier;
	
	@Getter @Setter
	private FishBuffType buffType;
	
	@Getter @Setter
	private int value;
	
	public FishBuff(NBTTagCompound tag, FishBuffType type) {
		this(FishingTier.values()[tag.getInt("itemTier") - 1], type);
		setValue(tag.getInt("fishVal"));
	}

	public FishBuff(FishingTier tier, FishBuffType type) {
		setBuffType(type);
		setTier(tier);
		generateVal();
	}
	
	public void save(NBTTagCompound tag) {
		tag.setInt("fishVal", getValue());
		tag.setString("buffType", getBuffType().name());
	}
	
	/**
	 * Applies this buff to the given player.
	 */
	public abstract void applyBuff(Player player);
	
	/**
	 * Gets the chances of getting this buff, by tier.
	 */
	protected abstract int[] getChances();
	
	/**
	 * Gets the prefixes for the item name.
	 */
	protected abstract String[] getNamePrefixes();
	
	/**
	 * Gets the durations.
	 */
	protected abstract int[] getDurations();
	
	/**
	 * Generates the value this uses.
	 */
	protected abstract void generateVal();
	
	/**
	 * Return the chance of getting this buff.
	 */
	public int getChance() {
		return getChances()[getTier().getTier() - 1];
	}
	
	public String getNamePrefix() {
		return getBuffType().getPrefix() != null ? getBuffType().getPrefix() + " " : "";
	}
	
	public String getNameSuffix() {
		return getNamePrefixes()[getTier().getTier() - 1] + " " + getBuffType().getBaseSuffix();
	}
	
	private String getBuffPrefix() {
		return getBuffType().getBuffPrefix();
	}
	
	private String getBuffSuffix() {
		return getBuffType().getBuffPrefix();
	}
	
	public int getDuration() {
		return getDurations() != null ?getDurations()[getTier().getTier() - 1] : -1;
	}
	
	/**
	 * Returns the name of this fish.
	 */
	public String getItemName(EnumFish type) {
		String prefix = getNamePrefix() != null ? getNamePrefix() + " " : "";
		String suffix = getNameSuffix() != null ? " of " + getNameSuffix() : "";
		return prefix + type.getName() + suffix;
	}
	
	/**
	 * Gets the lore string that describes this buff.
	 */
	public String getLore() {
		return ChatColor.RED + "" + getBuffPrefix() + getDuration() + getBuffSuffix() + " " + ChatColor.GRAY
				+ "(" + (getDuration() > 0 ? getDuration() + "s" : "instant") + ")";
	}
	
	protected void applyStatTemporarily(Player player, AttributeType stat) {
		String meta = "fish" + stat.getNBTName();
		if (player.hasMetadata(meta)) {
			player.sendMessage(ChatColor.RED + "You already have this buff active.");
			return;
		}
		
		final AttributeList add = new AttributeList();
		add.setStat(stat, getValue());
		GamePlayer gp = GameAPI.getGamePlayer(player);
		gp.getAttributes().addStats(add);
		
		player.setMetadata(meta, new FixedMetadataValue(DungeonRealms.getInstance(), true));
		player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "      " + ChatColor.GREEN + getValue() + ChatColor.BOLD
                + getBuffSuffix() + ChatColor.GREEN + " FROM FISH " + ChatColor.GRAY + " [" + getDuration() + "s]");
		
		Bukkit.getScheduler().runTaskLater(DungeonRealms.getInstance(), () -> {
			player.removeMetadata(meta, DungeonRealms.getInstance());
			gp.getAttributes().removeStats(add);
			player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "   " + getValue() + getBuffSuffix() + " " + ChatColor.RED + "FROM FISH " + ChatColor.RED + " " + ChatColor.UNDERLINE + "EXPIRED");
		}, getDuration() * 20);
	}
}
