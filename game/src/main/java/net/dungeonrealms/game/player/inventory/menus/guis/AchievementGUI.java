package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.game.achievements.Achievements;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.menus.AchievementMenu;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class AchievementGUI extends GUIMenu {
    public AchievementGUI(Player player, GUIMenu menu) {
        super(player, 18, "Achievements", menu);
    }

    @Override
    protected void setItems() {
        if (getPreviousGUI() != null)
            setItem(getSize() - 1, getBackButton());

        int i = 0;
        for (Achievements.AchievementCategory cat : Achievements.AchievementCategory.values()) {
            setItem(i++, new GUIItem(ItemManager.createItem(cat.getIcon(), ChatColor.GOLD + ChatColor.BOLD.toString() + cat.getName(),
                    "",
                    ChatColor.GRAY.toString() + "View achievements related to " + cat.getDescription() + ".")).setClick(e -> {
                player.closeInventory();
                AchievementMenu gui = new AchievementMenu(player, cat,this);
                gui.open(player,null);
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, .3F, 1.8F);
            }));
        }

    }
}
