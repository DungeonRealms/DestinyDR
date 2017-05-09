package net.dungeonrealms.game.player.inventory.menus;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements.*;
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
		PlayerWrapper pw = PlayerWrapper.getWrapper(getPlayer());
		List<EnumAchievements> playerAchievements = pw.getAchievements();
        addItem(createItem(Material.BARRIER, ChatColor.GREEN + "Back")).setOnClick((player, shop) -> false);

        for (EnumAchievements achievement : EnumAchievements.getByCategory(this.category)) {
        	if (achievement.isHide())
        		continue;
        	boolean has = playerAchievements.contains(achievement);
        	Material mat = has ? Material.SLIME_BALL : Material.MAGMA_CREAM;
        	ChatColor color = has ? ChatColor.GREEN : ChatColor.RED;
        	addItem(createItem(mat, color + achievement.getName(), "",
                ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage(),
                ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP", "",
                color + "" + ChatColor.BOLD + (has ? "Complete" : "Incomplete")));
        }
	}
}

	
