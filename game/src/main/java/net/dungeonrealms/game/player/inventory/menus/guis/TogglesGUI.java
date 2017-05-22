package net.dungeonrealms.game.player.inventory.menus.guis;

import net.dungeonrealms.database.PlayerToggles;
import net.dungeonrealms.database.PlayerWrapper;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.player.inventory.menus.GUIItem;
import net.dungeonrealms.game.player.inventory.menus.GUIMenu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TogglesGUI extends GUIMenu {
    public TogglesGUI(Player player, GUIMenu prevous) {
        super(player, fitSize(PlayerToggles.Toggles.values().length + 1), "Toggles", prevous);
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void setItems() {
        int i = 0;
        PlayerWrapper wrapper = PlayerWrapper.getWrapper(player);
        for (PlayerToggles.Toggles t : PlayerToggles.Toggles.getToggles(player)) {
            if (!wrapper.getRank().isAtLeast(t.getMinRank()))
                continue;
            boolean toggle = wrapper.getToggles().getState(t);

            setItem(i++, new GUIItem(ItemManager.createItem(Material.INK_SACK, t.getDye(toggle).getDyeData(), (toggle ? ChatColor.GREEN : ChatColor.RED) + ChatColor.BOLD.toString() + t.getDisplayName()))
                    .setLore(ChatColor.GRAY + t.getDescription(), "", ChatColor.YELLOW + ChatColor.BOLD.toString() + "Click to " + (toggle ? ChatColor.RED + ChatColor.BOLD.toString() + "DISABLE" : ChatColor.GREEN + ChatColor.BOLD.toString() + "ENABLE"), ChatColor.YELLOW + "Or use /" + t.getCommand())
                    .setClick(e -> {
                        wrapper.getToggles().toggle(t);
                        setItems();
                    }));
        }

        if (getPreviousGUI() != null)
            setItem(getSize() - 1, getBackButton(ChatColor.GRAY + "Click to return to Profile Menu."));
    }
}
