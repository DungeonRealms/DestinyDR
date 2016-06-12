package net.dungeonrealms.game.guild.banner.menus.selectors;

import net.dungeonrealms.game.gui.GUIButtonClickEvent;
import net.dungeonrealms.game.gui.item.GUIButton;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.banner.AbstractMenu;
import net.dungeonrealms.game.guild.banner.menus.BannerCreatorMenu;
import net.dungeonrealms.game.miscellaneous.ColorConverter;
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

public class ColorSelectorMenu extends AbstractMenu {

    public ColorSelectorMenu(Player player, AbstractMenu from, Object input) {
        super("Select a color", 18, player.getUniqueId());
        setDestroyOnExit(true);

        for (DyeColor dye : DyeColor.values()) {
            GUIButton button = new GUIButton(Material.INK_SACK, dye.getDyeData()) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    event.getWhoClicked().closeInventory();
                    ItemStack currentBanner = GuildMechanics.getInstance().grabCurrentCreateInfo(player).getCurrentBanner();
                    BannerMeta bannerMeta = (BannerMeta) currentBanner.getItemMeta();

                    if (from instanceof PatternSelectorMenu && input instanceof PatternType)
                        bannerMeta.addPattern(new Pattern(dye, (PatternType) input));
                    else if (from instanceof BannerCreatorMenu) bannerMeta.setBaseColor(dye);
                    currentBanner.setItemMeta(bannerMeta);

                    GuildMechanics.getInstance().openGuildBannerCreator(event.getWhoClicked());
                }
            };
            button.setDisplayName(ColorConverter.dyeToChat(dye) + WordUtils.capitalizeFully(dye.name().replace("_", " ")));
            button.setLore(Collections.singletonList("&7Click here to select this color."));

            set(getSize(), button);
        }

    }

    @Override
    public void open(Player player) {
        player.openInventory(inventory);
    }

}
