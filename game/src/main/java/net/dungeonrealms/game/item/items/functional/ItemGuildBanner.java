package net.dungeonrealms.game.item.items.functional;

import lombok.Getter;
import lombok.Setter;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.guild.GuildDatabaseAPI;
import net.dungeonrealms.game.guild.token.GuildCreateToken;
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
	private GuildCreateToken guild;
	
	private String guildName;
	
	public ItemGuildBanner(GuildCreateToken guild) {
		super(ItemType.GUILD_BANNER);
		setGuild(guild);
	}
	
	public ItemGuildBanner(ItemStack item) {
		super(item);
		this.guildName = getTagString("guild");
	}
	
	@Override
	public void updateItem() {
		setTagString("guild", getGuild().getGuildName());
		super.updateItem();
	}

	@Override
	public void onClick(ItemClickEvent evt) {
		Player p = evt.getPlayer();
        p.getInventory().setItemInMainHand(p.getInventory().getHelmet());
        p.getInventory().setHelmet(getItem());
        
        String guildName = this.guildName.toLowerCase();

        GuildDatabaseAPI.get().doesGuildNameExist(guildName, exists -> {
            if (exists && GuildDatabaseAPI.get().getGuildOf(p.getUniqueId()).equals(guildName)) {
                Achievements.getInstance().giveAchievement(p.getUniqueId(), Achievements.EnumAchievements.GUILD_REPESENT);
                String motd = GuildDatabaseAPI.get().getMotdOf(guildName);

                if (!motd.isEmpty())
                    p.sendMessage(ChatColor.GRAY + "\"" + ChatColor.AQUA + motd + ChatColor.GRAY + "\"");
            }
        });
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
		return new ItemStack(Material.BANNER, 1, (short)15);
	}
	
}
