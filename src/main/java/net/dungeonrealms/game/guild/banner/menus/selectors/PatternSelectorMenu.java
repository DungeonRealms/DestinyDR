package net.dungeonrealms.game.guild.banner.menus.selectors;

import net.dungeonrealms.game.gui.item.GUIButton;
import net.dungeonrealms.game.gui.GUIButtonClickEvent;
import net.dungeonrealms.game.guild.banner.AbstractMenu;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * Class written by APOLLOSOFTWARE.IO on 6/11/2016
 */

public class PatternSelectorMenu extends AbstractMenu {

    public PatternSelectorMenu(Player player, Consumer<Pattern> callback) {
        super("Select a color", 63, player.getUniqueId());
        setDestroyOnExit(true);

        for (PatternType patternType : PatternType.values()) {


            // BANNER THAT WILL DISPLAY //
            ItemStack previewBanner = new ItemStack(Material.BANNER);
            BannerMeta bannerMeta = (BannerMeta) previewBanner.getItemMeta();
            bannerMeta.setBaseColor(DyeColor.WHITE);

            bannerMeta.addPattern(new Pattern(DyeColor.BLACK, patternType));
            previewBanner.setItemMeta(bannerMeta);

            GUIButton button = new GUIButton(previewBanner) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {

                    new ColorSelectorMenu(event.getWhoClicked(), color -> {
                        Bukkit.getScheduler().runTask(plugin, () -> callback.accept(new Pattern(color, patternType)));

                    }).open(event.getWhoClicked());
                }
            };

            button.setDisplayName(ChatColor.GREEN + WordUtils.capitalizeFully(patternType.name().replace("_", " ")));
            button.setLore(Collections.singletonList("&7Click here to select this pattern."));
            set(getSize(), button);
        }


    }


    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }


}
