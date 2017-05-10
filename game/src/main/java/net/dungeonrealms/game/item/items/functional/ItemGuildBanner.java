package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.guild.GuildWrapper;
import net.dungeonrealms.game.item.ItemType;
import net.dungeonrealms.game.item.ItemUsage;
import net.dungeonrealms.game.item.event.ItemClickEvent;
import net.dungeonrealms.game.item.event.ItemClickEvent.ItemClickListener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemGuildBanner extends FunctionalItem implements ItemClickListener {

	@Getter @Setter
	private GuildWrapper guild;
	
	public ItemGuildBanner(GuildWrapper guild) {
		super(ItemType.GUILD_BANNER);
		setGuild(guild);
		setUntradeable(true);
	}
	
	public ItemGuildBanner(ItemStack item) {
		super(item);
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player p = evt.getPlayer();
        p.getInventory().setItemInMainHand(p.getInventory().getHelmet());
        p.getInventory().setHelmet(getItem());
        Achievements.giveAchievement(p, EnumAchievements.GUILD_REPESENT);
	}

	@Override
	protected String getDisplayName() {
		return ChatColor.GREEN + getGuild().getDisplayName() + "'s Guild Banner";
	}

	@Override
	protected String[] getLore() {
		return new String[] { "Right-Click to equip." };
	}

	@Override
	protected ItemUsage[] getUsage() {
		return INTERACT_RIGHT_CLICK;
	}

	@Override
	protected ItemStack getStack() {
		return getGuild() != null && getGuild().getBanner() != null ? getGuild().getBanner() : new ItemStack(Material.BANNER, 1, (short)15);
	}
}
