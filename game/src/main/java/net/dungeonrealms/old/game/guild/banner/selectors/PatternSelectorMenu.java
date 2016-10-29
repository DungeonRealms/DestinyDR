package net.dungeonrealms.old.game.guild.banner.selectors;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Collections;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public class PatternSelectorMenu extends AbstractMenu {

    public PatternSelectorMenu(Player player) {
        super(DungeonRealms.getInstance(), "Select a pattern", 54, player.getUniqueId());
        setDestroyOnExit(true);

        for (PatternType patternType : PatternType.values()) {

            // BANNER THAT WILL DISPLAY //
            ItemStack previewBanner = new ItemStack(Material.BANNER);
            BannerMeta previewMeta = (BannerMeta) previewBanner.getItemMeta();
            previewMeta.setBaseColor(DyeColor.WHITE);

            previewMeta.addPattern(new Pattern(DyeColor.BLACK, patternType));
            previewBanner.setItemMeta(previewMeta);

            GUIButton button = new GUIButton(previewBanner) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    event.getWhoClicked().closeInventory();
                    new ColorSelectorMenu(event.getWhoClicked(), PatternSelectorMenu.this, patternType).open(event.getWhoClicked());
                }
            };

            button.setDisplayName(ChatColor.GREEN + WordUtils.capitalizeFully(patternType.name().replace("_", " ")));
            button.setLore(Collections.singletonList("&7Click here to select this pattern."));
            set(getSize(), (button));
        }
    }


    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

}
