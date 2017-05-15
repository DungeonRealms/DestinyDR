package net.dungeonrealms.game.player.inventory.menus;

import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.achievements.Achievements.AchievementCategory;
import net.dungeonrealms.game.achievements.Achievements.EnumAchievements;
import net.dungeonrealms.game.player.inventory.ShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class AchievementMenu extends GUIMenu {

	private AchievementCategory category;
	
	public AchievementMenu(Player player) {
		this(player, AchievementCategory.values()[0],null);
	}

	public AchievementMenu(Player player, AchievementCategory ac, GUIMenu previous) {
		super(player, fitSize(ac.getNumberOfAchievements() + 1),"Achievements",previous);
		this.category = ac;
	}
	
	@Override
	protected void setItems() {
		PlayerWrapper pw = PlayerWrapper.getWrapper(getPlayer());
		Set<EnumAchievements> playerAchievements = pw.getAchievements();

		setItem(getSize() - 1, getBackButton());


		int slot = 0;
		for (EnumAchievements achievement : playerAchievements) {
			if (achievement.isHide())
				continue;
			if (achievement.getCategory() != category) continue;
			setItem(slot++, new GUIItem(Material.SLIME_BALL).setName(ChatColor.GREEN + ChatColor.BOLD.toString() + achievement.getName()).setLore("",ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage(),ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP", "", ChatColor.GREEN.toString() + ChatColor.BOLD + "COMPLETE"));
		}
		//Lets show the unlocked ones first. Gui looks like shit all random.
        for (EnumAchievements achievement : EnumAchievements.values()) {
        	if (achievement.isHide())
        		continue;
        	if(achievement.getCategory() != category) continue;
        	boolean has = playerAchievements.contains(achievement);
        	if(has) continue;
        	setItem(slot++, new GUIItem(Material.MAGMA_CREAM).setName(ChatColor.RED + ChatColor.BOLD.toString() + achievement.getName()).setLore("",ChatColor.GRAY.toString() + ChatColor.ITALIC + achievement.getMessage(),ChatColor.GRAY + "Reward : " + achievement.getReward() + " EXP","", ChatColor.RED + ChatColor.BOLD.toString() + "INCOMPLETE"));
        }
	}
}

	
