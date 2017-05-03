package net.dungeonrealms.game.player.inventory.menus;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.player.inventory.ShopMenu;

public class AchievementMenu extends ShopMenu {

	private AchievementCategory category;
	
	public AchievementMenu(Player player) {
		this(player, AchievementCategory.values()[0]);
	}

	public AchievementMenu(Player player, AchievementCategory ac) {
		super(player, ac.getName() + " Achievements", 4);
		this.category = ac;
	}
	
	@Override
	protected void setItems() {
        List<String> playerAchievements = (ArrayList<String>) DatabaseAPI.getInstance().getData(EnumData.ACHIEVEMENTS, getPlayer().getUniqueId());
        boolean noAchievements = playerAchievements == null || playerAchievements.size() == 0;
        addItem(createItem(Material.BARRIER, ChatColor.GREEN + "Back")).setOnClick((player, shop) -> false);

        for (Achievements.EnumAchievements achievement : Achievements.EnumAchievements.values()) {
        	if (!achievement.getMongoName().contains("." + category.getInternal() + "_") || achievement.getHide())
        		continue;
        	boolean has = !noAchievements && playerAchievements.contains(achievement.getMongoName());
        	Material mat = has ? Material.SLIME_BALL : Material.MAGMA_CREAM;
        	ChatColor color = has ? ChatColor.GREEN : ChatColor.RED;
        	addItem(createItem(mat, color + achievement.getName(), "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage()[0],
                ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP", "",
                color + "" + ChatColor.BOLD + (has ? "Complete" : "Incomplete")));
        }
	}
	
	@AllArgsConstructor @Getter
	public enum AchievementCategory {
		
		EXPLORE("Exploration", "explorer", "exploration", Material.MAP),
		SOCIAL("Social", "social", "socialization", Material.WRITTEN_BOOK),
		MONEY("Currency", "currency", "currency", Material.EMERALD),
		COMBAT("Combat", "combat", "combat", Material.GOLD_SWORD),
		REALM("Realm", "realm", "your realm", Material.NETHER_STAR),
		EVENT("Event", "event", "event participation", Material.GOLD_INGOT),
		CHARACTER("Character", "character", "character customization", Material.ARMOR_STAND);
		
		private String name;
		private String internal;
		private String description;
		private Material icon;
	}
}

	
