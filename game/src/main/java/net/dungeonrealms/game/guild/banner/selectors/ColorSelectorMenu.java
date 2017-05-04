package net.dungeonrealms.game.guild.banner.selectors;

import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.common.game.menu.AbstractMenu;
import net.dungeonrealms.common.game.menu.gui.GUIButtonClickEvent;
import net.dungeonrealms.common.game.menu.item.GUIButton;
import net.dungeonrealms.game.guild.GuildMechanics;
import net.dungeonrealms.game.guild.banner.BannerCreatorMenu;
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

    public ColorSelectorMenu(Player player, AbstractMenu from, Object input, String guildName, String guildTag, String guildDisplayName, ItemStack banner) {
        super(DungeonRealms.getInstance(), "Select a color", 18, player.getUniqueId());
        setDestroyOnExit(true);

        for (DyeColor dye : DyeColor.values()) {
            GUIButton button = new GUIButton(Material.INK_SACK, dye.getDyeData()) {
                @Override
                public void action(GUIButtonClickEvent event) throws Exception {
                    event.getWhoClicked().closeInventory();
                    ItemStack currentBanner = banner;
                    BannerMeta bannerMeta = (BannerMeta) currentBanner.getItemMeta();

                    if (from instanceof PatternSelectorMenu && input instanceof PatternType)
                        bannerMeta.addPattern(new Pattern(dye, (PatternType) input));
                    else if (from instanceof BannerCreatorMenu) bannerMeta.setBaseColor(dye);
                    currentBanner.setItemMeta(bannerMeta);

                    GuildMechanics.getInstance().openGuildBannerCreator(event.getWhoClicked(),guildName, guildTag, guildDisplayName, banner);
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
